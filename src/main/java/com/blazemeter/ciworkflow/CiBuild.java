/**
 * Copyright 2017 BlazeMeter Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazemeter.ciworkflow;

import com.blazemeter.api.explorer.Master;
import com.blazemeter.api.explorer.test.AbstractTest;
import com.blazemeter.api.logging.Logger;

import java.io.IOException;
import java.util.Calendar;

public class CiBuild {

    public final AbstractTest test;

    public final String properties;

    public final String notes;

    public final Logger logger;

    public final CiPostProcess ciPostProcess;

    public String pr;

    public CiBuild(AbstractTest test, String properties,
                   String notes,
                   boolean isDownloadJtl,
                   boolean isDownloadJunit, String junitPath,
                   String jtlPath, String workspaceDir,
                   Logger logger) {
        this.test = test;
        this.properties = properties;
        this.notes = notes;
        this.logger = logger;
        this.ciPostProcess = new CiPostProcess(isDownloadJtl, isDownloadJunit,
                junitPath, jtlPath, workspaceDir, logger);
    }

    /**
     * Executes ci build
     *
     * @return BuildResult
     */
    public BuildResult execute() {
        Master master = null;
        try {
            master = this.test.start();
            pr = master.getPublicReport();
            master.postNotes(this.notes);
            master.postProperties(this.properties);
            waitForFinish(master);
            return this.ciPostProcess.execute(master);
        } catch (InterruptedException ie) {
            try {
                return this.ciPostProcess.execute(master);
            } catch (InterruptedException e) {
                return BuildResult.ABORTED;
            }
        } catch (Exception e) {
            return BuildResult.FAILED;
        }
    }

    /**
     * Waits until test will be over on server
     *
     * @throws InterruptedException IOException
     */
    public void waitForFinish(Master master) throws InterruptedException, IOException {
        long lastPrint = 0;
        while (true) {
            Thread.sleep(10000);
            if (master.getStatus() == 140) {
                return;
            }
            long start = Calendar.getInstance().getTime().getTime();
            long now = Calendar.getInstance().getTime().getTime();
            long diffInSec = (now - start) / 1000;
            if (now - lastPrint > 60000) {
                logger.info("BlazeMeter test# , masterId # " + master.getId() + " running from " + start + " - for " + diffInSec + " seconds");
                lastPrint = now;
            }
            if (Thread.interrupted()) {
                logger.info("Job was stopped by user");
                throw new InterruptedException("Job was stopped by user");
            }
        }
    }

}
