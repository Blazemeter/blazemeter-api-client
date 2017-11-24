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
            master.waitForFinish();
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
}
