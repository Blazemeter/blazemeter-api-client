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
import com.blazemeter.api.logging.UserNotifier;

import java.io.IOException;

public class CiBuild {

    public final AbstractTest test;

    public final String properties;

    public final String notes;

    public final UserNotifier notifier;

    public final Logger logger;

    public final CiPostProcess ciPostProcess;

    public String publicReport;

    public CiBuild(AbstractTest test, String properties, String notes,
                   boolean isDownloadJtl, boolean isDownloadJunit,
                   String junitPath, String jtlPath, String workspaceDir) {
        this.test = test;
        this.properties = properties;
        this.notes = notes;
        this.notifier = this.test.getUtils().getNotifier();
        this.logger = this.test.getUtils().getLogger();
        this.ciPostProcess = new CiPostProcess(isDownloadJtl, isDownloadJunit,
                junitPath, jtlPath, workspaceDir, notifier);
    }

    /**
     * Executes ci build
     *
     * @return BuildResult
     */
    public BuildResult execute() {
        Master master = null;
        try {
            notifier.notifyAbout("CiBuild is started.");
            notifier.notifyAbout("TestId = " + test.getId());
            notifier.notifyAbout("TestName = " + test.getName());
            master = test.start();
            publicReport = master.getPublicReport();
            master.postNotes(notes);
            master.postProperties(properties);
            waitForFinish(master);
            return this.ciPostProcess.execute(master);
        } catch (InterruptedException ie) {
            try {
                logger.warn("Caught InterruptedException, execute postProcess. " + ie.getMessage());
                return this.ciPostProcess.execute(master);
            } catch (InterruptedException e) {
                return BuildResult.ABORTED;
            }
        } catch (IOException e) {
            logger.error("Caught exception. Set Build status [FAILED]. Reason is: " + e.getMessage(), e);
            notifier.notifyAbout("Caught exception. Set Build status [FAILED]. Reason is: " + e.getMessage());
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
        long start = System.currentTimeMillis();
        long bzmCheckTimeout = Long.parseLong(System.getProperty("bzm.checkTimeout", "10000"));
        long bzmMinute = Long.parseLong(System.getProperty("bzm.minute", "60000"));
        while (true) {
            Thread.sleep(bzmCheckTimeout);
            if (master.getStatus() == 140) {
                return;
            }
            long now = System.currentTimeMillis();
            long diffInSec = (now - start) / 1000;
            if (now - lastPrint > bzmMinute) {
                notifier.notifyAbout("BlazeMeter test# , masterId # " + master.getId() + " running from " + start + " - for " + diffInSec + " seconds");
                lastPrint = now;
            }
            if (Thread.interrupted()) {
                logger.warn("Job was stopped by user");
                notifier.notifyAbout("Job was stopped by user");
                throw new InterruptedException("Job was stopped by user");
            }
        }
    }

}
