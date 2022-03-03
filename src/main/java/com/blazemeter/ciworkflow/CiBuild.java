/**
 * Copyright 2018 BlazeMeter Inc.
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

import com.blazemeter.api.exception.InterruptRuntimeException;
import com.blazemeter.api.exception.ValidationException;
import com.blazemeter.api.explorer.Master;
import com.blazemeter.api.explorer.Workspace;
import com.blazemeter.api.explorer.test.AbstractTest;
import com.blazemeter.api.explorer.test.MultiTest;
import com.blazemeter.api.explorer.test.SingleTest;
import com.blazemeter.api.explorer.test.TestDetector;
import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.logging.UserNotifier;
import com.blazemeter.api.utils.BlazeMeterUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class CiBuild {

    protected final UserNotifier notifier;
    protected final Logger logger;

    protected final BlazeMeterUtils utils;
    protected final String testId;

    protected File mainTestFile;
    protected List<File> additionalTestFiles;

    protected final String properties;
    protected final String notes;
    protected final CiPostProcess ciPostProcess;

    protected String publicReport;

    protected AbstractTest currentTest;

    protected String workspaceId;

    protected String reportName;

    private final String FUNCTIONAL_GUI_TEST = "functionalGui";

    public CiBuild(BlazeMeterUtils utils, String testId, File mainTestFile, List<File> additionalTestFiles,
                   String properties, String notes, CiPostProcess ciPostProcess) {
        this(utils, testId, properties, notes, ciPostProcess);
        this.mainTestFile = mainTestFile;
        this.additionalTestFiles = additionalTestFiles;
    }

    public CiBuild(BlazeMeterUtils utils, String testId, String properties, String notes, CiPostProcess ciPostProcess) {
        this.utils = utils;
        this.testId = testId;
        this.properties = properties;
        this.notes = notes;
        this.ciPostProcess = ciPostProcess;
        this.notifier = utils.getNotifier();
        this.logger = utils.getLogger();
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
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
            if (master == null) {
                logger.error("Set Build status [FAILED].");
                notifier.notifyError("Set Build status [FAILED].");
                return BuildResult.FAILED;
            }
            return waitForFinishAndDoPostProcess(master);
        } catch (IOException e) {
            logger.error("Caught exception. Set Build status [FAILED]. Reason is: " + e.getMessage(), e);
            notifier.notifyError("Caught exception. Set Build status [FAILED]. Reason is: " + e.getMessage());
            return BuildResult.FAILED;
        } catch (InterruptedException | InterruptRuntimeException e) {
            logger.error("Caught exception. Set Build status [ABORTED]. Reason is: " + e.getMessage(), e);
            notifier.notifyError("Caught exception. Set Build status [ABORTED].");
            return BuildResult.ABORTED;
        }
    }

    protected BuildResult waitForFinishAndDoPostProcess(Master master) throws IOException {
        try {
            waitForFinish(master);
            return doPostProcess(master);
        } catch (InterruptedException | InterruptRuntimeException ex) {
            logger.warn("Caught InterruptedException, execute postProcess. " + ex.getMessage());
            boolean hasReports = interrupt(master);
            if (hasReports) {
                doPostProcess(master);
            }
            // because build has been aborted
            return BuildResult.ABORTED;
        }
    }


    /**
     * Start Test with 'testId' in BlazeMeter
     * and the Post properties and notes to Master
     *
     * @return Master of started Test
     */
    public Master start() throws IOException, InterruptedException {
        notifier.notifyInfo("CiBuild is started.");
        currentTest = TestDetector.detectTest(utils, testId);
        if (ciPostProcess != null) {
            ciPostProcess.setTest(currentTest.getId(), currentTest.getTestType());
        }
        if (currentTest == null) {
            logger.error("Failed to detect test type. Test with id=" + testId + " not found.");
            notifier.notifyError("Failed to detect test type. Test with id = " + testId + " not found.");
            return null;
        }

        updateTestFiles();
        notifier.notifyInfo(String.format("Start test id : %s, name : %s", currentTest.getId(), currentTest.getName()));
        return startTest(currentTest);
    }

    protected void updateTestFiles() throws IOException, InterruptedException {
        if (currentTest instanceof SingleTest && isSupportTestFiles(currentTest)) {
            SingleTest test = (SingleTest) currentTest;
            updateMainTestFile(test);
            updateAdditionalTestFiles(test);
            validateTestFiles(test);
        } else {
            if ((mainTestFile != null) || (additionalTestFiles != null && !additionalTestFiles.isEmpty())) {
                notifier.notifyWarning("Current test does not support uploading script files");
            }
        }
    }

    protected void validateTestFiles(SingleTest test) throws IOException, InterruptedException {
        List<String> fileNames = new ArrayList<>();

        if (mainTestFile != null) {
            fileNames.add(mainTestFile.getName());
        }

        if (additionalTestFiles != null && !additionalTestFiles.isEmpty()) {
            for (File file : additionalTestFiles) {
                fileNames.add(file.getName());
            }
        }

        if (!fileNames.isEmpty()) {
            test.validateFiles(fileNames);
            waitForValidations(test, fileNames);
        }
    }

    protected void waitForValidations(SingleTest test, List<String> fileNames) throws IOException, InterruptedException {
        boolean isValidationFinished = false;
        while (!isValidationFinished) {
            JSONArray validations = test.validations();
            isValidationFinished = checkFilesValidation(fileNames, validations);

            if (!isValidationFinished) {
                Thread.sleep(1000);
            }
        }
        logger.info(String.format("Validation for files %s finished successfully", Arrays.toString(fileNames.toArray(new String[0]))));
        notifier.notifyInfo(String.format("Validation for files %s finished successfully", Arrays.toString(fileNames.toArray(new String[0]))));
    }

    /**
     * @return true - if success, false - is not finished and throws RuntimeException if have validation errors
     */
    protected boolean checkFilesValidation(List<String> fileNames, JSONArray validations) {
        boolean isFinished = true;
        for (int i = 0; i < validations.size(); i++) {
            JSONObject object = validations.getJSONObject(i);
            String fileName = object.getString("fileName");
            if (fileNames.contains(fileName)) {
                boolean checkFile = checkFileValidation(fileName, object);
                if (!checkFile) {
                    logger.info(String.format("Validation for file=%s is not ready, will be repeat", fileName));
                    isFinished = false;
                }
            } else {
                logger.debug("Skipping " + object + ", because this file was not update");
            }
        }

        return isFinished;
    }

    protected boolean checkFileValidation(String fileName, JSONObject object) {
        int status = object.getInt("status");
        if (status >= 100) {
            JSONArray errors = object.getJSONArray("errors");
            if (!errors.isEmpty()) {
                notifier.notifyError(String.format("Validation error: file=%s; errors=%s", fileName, errors));
                logger.error(String.format("Validation error: file=%s; errors=%s", fileName, errors));
                throw new ValidationException(String.format("Validation error: file=%s; errors=%s", fileName, errors));
            }

            logger.info("File " + fileName + " successfully validated");
            return true;
        } else {
            logger.info("Validation for file=" + fileName + " is not ready. status=" + status);
            return false;
        }
    }

    protected boolean isSupportTestFiles(AbstractTest test) {
        return "jmeter".equals(test.getTestType()) ||
                "taurus".equals(test.getTestType()) ||
                "functionalApi".equals(test.getTestType());
    }

    protected void updateMainTestFile(SingleTest test) throws IOException {
        if (mainTestFile != null) {
            String filepath = FilenameUtils.normalize(mainTestFile.getAbsolutePath());
            logger.info("Update main test file: " + filepath);
            notifier.notifyInfo("Update main test file: " + filepath);
            test.uploadFile(mainTestFile);
            validateTestFileName(mainTestFile.getName());
            test.updateTestFilename(mainTestFile.getName());
        }
    }

    protected void validateTestFileName(String filename) {
        if (!filename.endsWith(".jmx") &&
                !filename.endsWith(".yml") &&
                !filename.endsWith(".yaml")) {
            notifier.notifyWarning("Unknown script type. Please, select 'Test type' in BlazeMeter web application");
        }
    }

    protected void updateAdditionalTestFiles(SingleTest test) throws IOException {
        if (additionalTestFiles != null && !additionalTestFiles.isEmpty()) {
            for (File file : additionalTestFiles) {
                String filepath = FilenameUtils.normalize(file.getAbsolutePath());
                logger.info("Upload additional test file: " + filepath);
                notifier.notifyInfo("Upload additional test file: " + filepath);
                test.uploadFile(file);
            }
        }
    }

    protected Master startTest(AbstractTest test) throws IOException, InterruptedException {
        Master master;
        if (!StringUtils.isBlank(properties) && test instanceof SingleTest) {
            notifier.notifyInfo("Sent properties: " + properties);
            master = test.startWithProperties(properties);
        } else {
            master = test.start();
        }

        Calendar startTime = Calendar.getInstance();
        startTime.setTimeInMillis(System.currentTimeMillis());
        notifier.notifyInfo("Test has been started successfully at " + startTime.getTime().toString() + ". Master id=" + master.getId());

        try {
            setReportName(master);

            if (test.getTestType().equals(FUNCTIONAL_GUI_TEST)) {
                String serverReport = master.getServerReport(workspaceId, test.getId());
                notifier.notifyInfo("Test report will be available at " + serverReport);
                waitForFinish(master);
            }

            master.setTestType(test.getTestType());
            generatePublicReport(master);

            skipInitState(master);

            if (!StringUtils.isBlank(properties) && test instanceof MultiTest) {
                notifier.notifyInfo("Sent properties: " + properties);
                master.postProperties(properties);
            }

            postNotes(master);
        } catch (InterruptedException | InterruptRuntimeException ex) {
            logger.warn("Interrupt master", ex);
            boolean hasReports = interrupt(master);
            if (hasReports) {
                doPostProcess(master);
            }
            throw new InterruptedException("Interrupt master");
        }
        return master;
    }


    protected void generatePublicReport(Master master) throws InterruptedException {
        try {
            publicReport = master.getPublicReport();
            notifier.notifyInfo("Public test report will be available at " + publicReport);
        } catch (InterruptedException | InterruptRuntimeException ex) {
            logger.warn("Interrupt while get public report", ex);
            throw ex;
        } catch (Exception ex) {
            logger.warn("Cannot get public token", ex);
            notifier.notifyWarning("Cannot get public token");
        }
    }

    protected void postNotes(Master master) throws InterruptedException {
        try {
            if (!StringUtils.isBlank(notes)) {
                notifier.notifyInfo("Sent notes: " + notes);
                master.postNotes(notes);
            }
        } catch (InterruptedException | InterruptRuntimeException ex) {
            logger.warn("Interrupt while post notes", ex);
            throw ex;
        } catch (Exception ex) {
            logger.warn("Cannot post notes", ex);
            notifier.notifyWarning("Cannot post notes");
        }
    }

    protected void setReportName(Master master) throws InterruptedException {
        try {
            if (!StringUtils.isBlank(reportName)) {
                notifier.notifyInfo("Setting report name : " + reportName);
                master.setReportName(reportName);
            }
        } catch (InterruptedException | InterruptRuntimeException ex) {
            logger.warn("Interrupt while setting report name", ex);
            throw ex;
        } catch (Exception ex) {
            logger.warn("Cannot set report name", ex);
            notifier.notifyWarning("Cannot set report name");
        }
    }

    /**
     * Skip INIT state.
     * It should be done before post notes and post session properties
     */
    protected void skipInitState(Master master) throws InterruptedException, IOException {
        int n = 1;
        long bzmCheckTimeout = BlazeMeterUtils.getCheckTimeout();
        while (n < 6) {
            try {
                Thread.sleep(bzmCheckTimeout);
                int statusCode = master.getStatus();
                if (statusCode > 0) {
                    break;
                }
            } catch (InterruptedException | InterruptRuntimeException ex) {
                logger.warn("Caught InterruptedException while skip InitState");
                throw ex;
            } catch (Exception e) {
                logger.warn("Failed to skip INIT state");
                return;
            } finally {
                n++;
            }
        }
    }


    /**
     * Interrupt Build.
     *
     * @return true - if build has reports.
     */
    public boolean interrupt(Master master) throws IOException {
        notifier.notifyWarning("Build has been interrupted");
        boolean hasReports = false;
        int statusCode = master.getStatus();
        if (statusCode < 100 && statusCode != 0) {
            master.terminate();
        }
        if (statusCode >= 100 || statusCode == -1 || statusCode == 0) {
            master.stop();
            hasReports = true;
        }
        return hasReports;
    }


    /**
     * Waits until test will be over on server
     * Master object corresponds to master session which is created after test was started.
     *
     * @throws InterruptedException IOException
     */
    public void waitForFinish(Master master) throws InterruptedException, IOException {
        long start = System.currentTimeMillis();
        long lastPrint = start;
        long bzmCheckTimeout = BlazeMeterUtils.getCheckTimeout();
        long bzmMinute = Long.parseLong(System.getProperty("bzm.minute", "60000"));
        while (true) {
            Thread.sleep(bzmCheckTimeout);
            if (master.getStatus() == 140) {
                return;
            }
            long now = System.currentTimeMillis();
            if (now - lastPrint > bzmMinute) {
                notifier.notifyInfo("Check if the test is still running. Time passed since start: " + ((now - start) / 1000 / 60) + " minutes.");
                lastPrint = now;
            }
            checkAborted();
        }
    }

    // TODO: is it really need?
    protected void checkAborted() throws InterruptedException {
        if (Thread.interrupted()) {
            logger.warn("Job was stopped by user");
            notifier.notifyError("Job was stopped by user");
            throw new InterruptedException("Job was stopped by user");
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

    public AbstractTest getCurrentTest() {
        return currentTest;
    }
}