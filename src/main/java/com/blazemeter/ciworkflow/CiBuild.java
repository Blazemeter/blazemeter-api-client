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
import com.blazemeter.api.explorer.test.TestDetector;
import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.logging.UserNotifier;
import com.blazemeter.api.utils.BlazeMeterUtils;

import java.io.IOException;

public class CiBuild {

    protected final UserNotifier notifier;
    protected final Logger logger;

    protected final BlazeMeterUtils utils;
    protected final String testId;

    protected final String properties;
    protected final String notes;
    protected final CiPostProcess ciPostProcess;

    protected String publicReport;

    public CiBuild(BlazeMeterUtils utils, String testId, String properties, String notes, CiPostProcess ciPostProcess) {
        this.utils = utils;
        this.testId = testId;
        this.properties = properties;
        this.notes = notes;
        this.ciPostProcess = ciPostProcess;
        this.notifier = utils.getNotifier();
        this.logger = utils.getLogger();
    }

    /**
     * Describes the common workflow of all CI plugins.
     * Executes ci build
     *
     * @return BuildResult
     */
    public BuildResult execute() {
        try {
            Master master = start();
            return waitForFinishAndDoPostProcess(master);
        } catch (IOException e) {
            logger.error("Caught exception. Set Build status [FAILED]. Reason is: " + e.getMessage(), e);
            notifier.notifyError("Caught exception. Set Build status [FAILED]. Reason is: " + e.getMessage());
            return BuildResult.FAILED;
        }
    }

    private BuildResult waitForFinishAndDoPostProcess(Master master) throws IOException {
        try {
            waitForFinish(master);
            return doPostProcess(master);
        } catch (InterruptedException ex) {
            logger.warn("Caught InterruptedException, execute postProcess. " + ex.getMessage());
            doPostProcess(master);
            // because build has been aborted
            return BuildResult.ABORTED;
        }
    }


    /**
     * Start Test with 'testId' in BlazeMeter
     * and the Post properties and notes to Master
     * @return Master of started Test
     */
    public Master start() throws IOException {
        notifier.notifyInfo("CiBuild is started.");
        AbstractTest test = TestDetector.detectTest(utils, testId);
        if (test == null) {
            logger.error("Failed to detect test type. Test with id = " + testId + " not found.");
            notifier.notifyError("Failed to detect test type. Test with id=" + testId + " not found.");
            return null;
        }

        notifier.notifyInfo(String.format("Start test id : %s, name : %s", test.getId(), test.getName()));
        return startTest(test);
    }

    private Master startTest(AbstractTest test) throws IOException {
        Master master = test.start();
        notifier.notifyInfo("Test has been started successfully. Master id=" + master.getId());

        publicReport = master.getPublicReport();
        notifier.notifyInfo("Test report will be available at " + publicReport);

        master.postNotes(notes);
        master.postProperties(properties);
        return master;
    }


    /**
     * Waits until test will be over on server
     * Master object corresponds to master session which is created after test was started.
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
                notifier.notifyInfo("BlazeMeter test# , masterId # " + master.getId() + " running from " + start + " - for " + diffInSec + " seconds");
                lastPrint = now;
            }
            if (Thread.interrupted()) {
                logger.warn("Job was stopped by user");
                notifier.notifyError("Job was stopped by user");
                throw new InterruptedException("Job was stopped by user");
            }
        }
    }

    /**
     * Run Post process action on Master
     */
    public BuildResult doPostProcess(Master master) {
        return ciPostProcess.execute(master);
    }

    public BlazeMeterUtils getUtils() {
        return utils;
    }

    public String getTestId() {
        return testId;
    }

    public String getProperties() {
        return properties;
    }

    public String getNotes() {
        return notes;
    }

    public CiPostProcess getCiPostProcess() {
        return ciPostProcess;
    }

    public String getPublicReport() {
        return publicReport;
    }

    public void setPublicReport(String publicReport) {
        this.publicReport = publicReport;
    }
}
