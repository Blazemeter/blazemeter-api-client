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

import com.blazemeter.api.explorer.Master;
import com.blazemeter.api.explorer.MasterTest;
import com.blazemeter.api.explorer.SessionTest;
import com.blazemeter.api.logging.LoggerTest;
import com.blazemeter.api.logging.UserNotifierTest;
import com.blazemeter.api.utils.BlazeMeterUtilsEmul;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_ADDRESS;
import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_DATA_ADDRESS;
import static org.junit.Assert.*;

public class CiPostProcessTest {

    @Test
    public void testDownloadSummaryFunc() {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        CiPostProcess ciPostProcess = new CiPostProcess(false, false, "", "", "", notifier, logger);
        Master master = new Master(emul, "id", "name");

        emul.addEmul(MasterTest.generateResponseGetFunctionalReport());

        JSONObject summary = ciPostProcess.downloadSummary(master);
        assertEquals(8, summary.size());
        assertEquals(1, summary.getInt("testsCount"));
        assertEquals(1, summary.getInt("requestsCount"));
        assertEquals(1, summary.getInt("errorsCount"));
    }

    @Test
    public void testUnzipJTL() {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        CiPostProcess ciPostProcess = new CiPostProcess(false, false, "re", "", System.getProperty("user.dir"), notifier, logger);
        try {
            File bzmZip = File.createTempFile("bzm_zip", ".zip");
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(bzmZip));
            out.putNextEntry(new ZipEntry("sample.jtl"));
            byte[] buffer = new byte[1024];
            Arrays.fill(buffer, (byte) 7);
            out.write(buffer);
            out.close();
            InputStream is = new FileInputStream(bzmZip);
            File reportDir = CiPostProcess.createTmpDir();
            ciPostProcess.unzipJTL(is, reportDir);
            File bmKpi = new File(reportDir, "bm-kpis.jtl");
            assertTrue(bmKpi.exists());
            bmKpi.delete();
            bzmZip.delete();
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }


    @Test
    public void testSaveJTL() {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        CiPostProcess ciPostProcess = new CiPostProcess(false, false, "", "", "", notifier, logger);
        emul.addEmul(MasterTest.generateResponseGetSessions());
        emul.addEmul(SessionTest.generateResponseGetJTLReport());
        try {
            Master master = new Master(emul, "id", "name");
            ciPostProcess.saveJTL(master);
            assertFalse(new File("bm-kpis.jtl").exists());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testDownloadSummaryAgr() {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        CiPostProcess ciPostProcess = new CiPostProcess(false, false, "", "", "", notifier, logger);
        Master master = new Master(emul, "id", "name");

        JSONObject result = new JSONObject();
        JSONObject response = new JSONObject();
        response.put("result", result);
        emul.addEmul(response.toString());
        emul.addEmul(MasterTest.generateResponseGetSummary());

        JSONObject summary = ciPostProcess.downloadSummary(master);
        assertEquals(7, summary.size());
        assertTrue(summary.has("hits"));
        assertEquals(2482, summary.getInt("hits"));
    }

    @Test
    public void testIsErrorsFailed() {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        CiPostProcess ciPostProcess = new CiPostProcess(false, false, "", "", "", notifier, logger);

        JSONArray errors = new JSONArray();
        JSONObject error = new JSONObject();

        error.put("code", 70404);
        errors.add(error);
        assertTrue(ciPostProcess.isErrorsFailed(errors));

        errors.clear();
        error.put("code", 0);
        errors.add(error);
        assertTrue(ciPostProcess.isErrorsFailed(errors));

        errors.clear();
        error.put("code", 111);
        errors.add(error);
        assertFalse(ciPostProcess.isErrorsFailed(errors));

        errors.clear();
        assertFalse(ciPostProcess.isErrorsFailed(errors));

        errors.clear();
        error.put("code", "xxx");
        errors.add(error);
        assertFalse(ciPostProcess.isErrorsFailed(errors));
        String logs = logger.getLogs().toString();
        assertTrue(logs, logs.contains("Failed get errors from json:"));
        assertTrue(notifier.getLogs().toString(), notifier.getLogs().toString().contains("Failed get errors from json:"));
    }

    @Test
    public void testValidatePerformanceCIStatusSuccess() {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseCIStatusSuccess());

        CiPostProcess ciPostProcess = new CiPostProcess(false, false, "", "", "", notifier, logger);
        BuildResult buildResult = ciPostProcess.validateCiStatus(JSONObject.fromObject(generateResponseCIStatusSuccess()));
        assertEquals(BuildResult.SUCCESS, buildResult);
    }

    public static String generateResponseCIStatusSuccess() {
        JSONArray errors = new JSONArray();
        JSONArray fails = new JSONArray();

        JSONObject result = new JSONObject();
        result.put("errors", errors);
        result.put("failures", fails);

        JSONObject response = new JSONObject();
        response.put("result", result);
        return response.toString();
    }

    @Test
    public void testValidatePerformanceCIStatusFailure() {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();

        CiPostProcess ciPostProcess = new CiPostProcess(false, false, "", "", "", notifier, logger);
        BuildResult buildResult = ciPostProcess.validateCiStatus(JSONObject.fromObject(generateResponseCIStatus_Failure_610000()).getJSONObject("result"));
        assertEquals(BuildResult.FAILED, buildResult);
    }

    public static String generateResponseCIStatus_Failure_610000() {
        JSONObject fail = new JSONObject();
        fail.put("code", 61000);

        JSONArray errors = new JSONArray();
        JSONArray failures = new JSONArray();
        failures.add(fail);

        JSONObject result = new JSONObject();
        result.put("errors", errors);
        result.put("failures", failures);
        JSONObject response = new JSONObject();
        response.put("result", result);
        return response.toString();
    }

    @Test
    public void testValidateFunctionalCIStatusSuccess() {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseFunctionalCIStatusSuccess());

        CiPostProcess ciPostProcess = new CiPostProcess(false, false, "", "", "", notifier, logger);
        BuildResult buildResult = ciPostProcess.validateFunctionalCiStatus(JSONObject.fromObject(generateResponseFunctionalCIStatusSuccess()).getJSONObject("result"));
        assertEquals(BuildResult.SUCCESS, buildResult);
    }

    public static String generateResponseFunctionalCIStatusSuccess() {
        JSONObject gridSummary = new JSONObject();
        gridSummary.put("definedStatus", "passed");

        JSONObject result = new JSONObject();
        result.put("gridSummary", gridSummary);

        JSONObject response = new JSONObject();
        response.put("result", result);
        return response.toString();
    }


    @Test
    public void testValidateFunctionalCIStatusFailure() {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();

        CiPostProcess ciPostProcess = new CiPostProcess(false, false, "", "", "", notifier, logger);
        BuildResult buildResult = ciPostProcess.validateFunctionalCiStatus(JSONObject.fromObject(generateFunctionalResponseCIStatus_Failure()).getJSONObject("result"));
        assertEquals(BuildResult.FAILED, buildResult);
    }

    public static String generateFunctionalResponseCIStatus_Failure() {
        JSONObject gridSummary = new JSONObject();
        gridSummary.put("definedStatus", "failed");

        JSONObject result = new JSONObject();
        result.put("gridSummary", gridSummary);

        JSONObject response = new JSONObject();
        response.put("result", result);
        return response.toString();
    }


    @Test
    public void testValidatePerformanceCIStatus70404Failure() {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        CiPostProcess ciPostProcess = new CiPostProcess(false, false, "", "", "", notifier, logger);
        BuildResult buildResult = ciPostProcess.validateCiStatus(JSONObject.fromObject(generateResponseCIStatus_Error_70404()).getJSONObject("result"));
        assertEquals(BuildResult.FAILED, buildResult);
    }

    public static String generateResponseCIStatus_Error_70404() {
        JSONObject error = new JSONObject();
        error.put("code", 70404);

        JSONArray errors = new JSONArray();
        errors.add(error);

        JSONArray failures = new JSONArray();

        JSONObject result = new JSONObject();
        result.put("errors", errors);
        result.put("failures", failures);

        JSONObject response = new JSONObject();
        response.put("result", result);
        return response.toString();
    }

    @Test
    public void testValidatePerformanceCIStatusError() {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        CiPostProcess ciPostProcess = new CiPostProcess(false, false, "", "", "", notifier, logger);
        BuildResult buildResult = ciPostProcess.validateCiStatus(JSONObject.fromObject(generateResponseCIStatus_Error_111()).getJSONObject("result"));
        assertEquals(BuildResult.ERROR, buildResult);
    }

    public static String generateResponseCIStatus_Error_111() {
        JSONObject error = new JSONObject();
        error.put("code", 111);

        JSONArray errors = new JSONArray();
        errors.add(error);

        JSONArray failures = new JSONArray();

        JSONObject result = new JSONObject();
        result.put("errors", errors);
        result.put("failures", failures);

        JSONObject response = new JSONObject();
        response.put("result", result);
        return response.toString();
    }

    @Test
    public void testGetters() {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        CiPostProcess ciPostProcess = new CiPostProcess(true, true, "jtl", "junit", "pwd", notifier, logger);

        assertTrue(ciPostProcess.isDownloadJtl());
        assertTrue(ciPostProcess.isDownloadJunit());
        assertEquals("junit", ciPostProcess.getJunitPath());
        assertEquals("jtl", ciPostProcess.getJtlPath());
        assertEquals("pwd", ciPostProcess.getWorkspaceDir());
    }


    @Test
    public void testFlow() {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseCIStatusSuccess());
        emul.addEmul(MasterTest.generateResponseGetHasDataStatus());
        emul.addEmul(MasterTest.generateResponseGetStatus(140));
        emul.addEmul("junit");
        emul.addEmul(MasterTest.generateResponseGetHasDataStatus());
        emul.addEmul(SessionTest.generateResponseGetJTLReport());
        emul.addEmul(MasterTest.generateResponseGetFunctionalReport());

        CiPostProcess ciPostProcess = new CiPostProcess(true, true, "jtl", "junit", "pwd", notifier, logger);
        ciPostProcess.setTest("testId", "testType");
        Master master = new Master(emul, "id", "name");
        BuildResult result = ciPostProcess.execute(master);
        assertEquals(BuildResult.SUCCESS, result);
        File junit = new File("./junit", "id.xml");
        junit.delete();
        junit.getParentFile().delete();
        assertFalse(junit.exists());
        assertFalse(junit.getParentFile().exists());
    }

    @Test
    public void testSaveJUnit() throws IOException {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        File junit = File.createTempFile("junit", ".xml");
        String name = junit.getName();
        name = name.substring(0, name.indexOf(".xml"));
        CiPostProcess ciPostProcess = new CiPostProcess(true, true, "jtl", junit.getParent(), junit.getParent(), notifier, logger);
        Master master = new Master(emul, name, "name");

        try {
            ciPostProcess.saveJunit(master);
            String logs = logger.getLogs().toString();
            assertTrue(logs, logs.contains("Failed to save junit report from master ="));
        } catch (Throwable ex) {
            fail(ex.getMessage());
        }
        logger.reset();

        emul.addEmul("junit");
        try {
            ciPostProcess.saveJunit(master);
        } catch (Throwable ex) {
            fail(ex.getMessage());
        }
        junit.delete();
        assertFalse(junit.exists());
    }

    @Test
    public void testSaveJTLFail() {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        CiPostProcess ciPostProcess = new CiPostProcess(true, true, "jtl", "junit", "", notifier, logger);
        Master master = new Master(emul, "id", "name");

        try {
            ciPostProcess.saveJTL(master);
            String logs = logger.getLogs().toString();
            assertTrue(logs, logs.contains("Unable to get JTL ZIP from"));
        } catch (Throwable ex) {
            fail(ex.getMessage());
        }

        logger.reset();
        emul.addEmul(MasterTest.generateResponseGetSessions());
        emul.addEmul(SessionTest.generateResponseGetJTLReport());
        ciPostProcess = new CiPostProcess(true, true, "jtl", "junit", "", notifier, logger) {
            @Override
            public boolean downloadAndUnzipJTL(URL url, File reportDir) {
                return false;
            }
        };
        try {
            ciPostProcess.saveJTL(master);
            String logs = logger.getLogs().toString();
            assertTrue(logs, logs.contains("Failed to download & unzip jtl-report from"));
        } catch (Throwable ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void testDownloadAndUnzipJTL() throws IOException {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();

        CiPostProcess ciPostProcess = new CiPostProcess(true, true, "jtl", "junit", "", notifier, logger) {
            @Override
            public void unzipJTL(InputStream inputStream, File reportDir) throws IOException {
                throw new IOException("ooops");
            }
        };

        boolean result = ciPostProcess.downloadAndUnzipJTL(new URL(BZM_ADDRESS), new File("1"));
        assertFalse(result);
        String logs = logger.getLogs().toString();
        assertTrue(logs, logs.contains("Unable to get JTL zip for url="));
    }

    @Test
    public void testDownloadSummary() {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        CiPostProcess ciPostProcess = new CiPostProcess(true, true, "jtl", "junit", "", notifier, logger);
        Master master = new Master(emul, "id", "name");

        JSONObject summary = ciPostProcess.downloadSummary(master);
        assertTrue(summary.isEmpty());
        String logs = logger.getLogs().toString();
        String notifiers = notifier.getLogs().toString();

        assertTrue(notifiers, notifiers.contains("Trying to get functional summary from server"));
        assertTrue(notifiers, notifiers.contains("Trying to get aggregate summary from server"));
        assertTrue(logs, logs.contains("Failed to get aggregate summary for master"));
        assertTrue(logs, logs.contains("Failed to get functional summary for master"));
    }

    @Test
    public void testHasReportsWhenSessionHasNoData() {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        CiPostProcess ciPostProcess = new CiPostProcess(true, true, "jtl", "junit", "", notifier, logger);
        Master master = new Master(emul, "id", "name");

        emul.addEmul(generateResponseCIStatus_Error_70404());
        BuildResult result = ciPostProcess.execute(master);
        assertEquals(BuildResult.FAILED, result);

        String logs = logger.getLogs().toString();
        assertFalse(logs, logs.contains("Get JUnit report for master"));
        assertFalse(logs, logs.contains("Get JTL report for session"));
        assertFalse(logs, logs.contains("Got functional report from server"));
        assertFalse(logs, logs.contains("Got aggregated report from server"));
        assertEquals(logs, 50, logs.length());
    }

    @Test
    public void testExecuteFailed() {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        CiPostProcess ciPostProcess = new CiPostProcess(true, true, "jtl", "junit", "", notifier, logger);
        Master master = new Master(emul, "id", "name");

        BuildResult result = ciPostProcess.execute(master);
        assertEquals(BuildResult.FAILED, result);
    }

    @Test
    public void testResolvePathUserNullPathNullWorkspace() {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        Master master = new Master(emul, "id", "name");

        setStandardFlow(emul);
        CiPostProcess postProcess = new CiPostProcess(true, true, null, null, null, notifier, logger);
        postProcess.setTest("testId", "testType");
        postProcess.execute(master);
        String notifiers = notifier.getLogs().toString();

        String jtlPath = notifiers.substring(notifiers.indexOf("Saving jtl report"), notifiers.indexOf("Trying to get functional summar"));
        System.out.println(jtlPath);
        assertTrue(notifiers, jtlPath.contains("/bzm_tmp"));
        assertTrue(notifiers, jtlPath.contains("/r-v3-1234567890qwerty"));
        assertFalse(notifiers, jtlPath.contains("/null/r-v3-1234567890qwerty"));

        String junitPath = notifiers.substring(notifiers.indexOf("Saving junit report"), notifiers.indexOf("Saving jtl"));
        System.out.println(junitPath);
        assertTrue(notifiers, junitPath.contains("/bzm_tmp"));
        assertTrue(notifiers, junitPath.contains("/id.xml"));
        assertFalse(notifiers, junitPath.contains("/null/id.xml"));
    }

    @Test
    public void testResolvePathUserNullPath() {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        Master master = new Master(emul, "id", "name");

        setStandardFlow(emul);
        String userWorkspace = System.getProperty("user.dir") + File.separator + "job/logs/100";
        CiPostProcess postProcess = new CiPostProcess(true, true, null, null, userWorkspace, notifier, logger);
        postProcess.setTest("testId", "testType");
        postProcess.execute(master);
        String notifiers = notifier.getLogs().toString();
        String jtlPath = notifiers.substring(notifiers.indexOf("Saving jtl report"), notifiers.indexOf("Trying to get functional summar"));
        System.out.println(jtlPath);
        assertTrue(notifiers, jtlPath.contains("job/logs/100/r-v3-1234567890qwerty"));
        assertFalse(notifiers, jtlPath.contains("job/logs/100/null/r-v3-1234567890qwerty"));

        String junitPath = notifiers.substring(notifiers.indexOf("Saving junit report"), notifiers.indexOf("Saving jtl"));
        System.out.println(junitPath);
        assertTrue(notifiers, junitPath.contains("job/logs/100/id.xml"));
        assertFalse(notifiers, junitPath.contains("job/logs/100/null/id.xml"));
    }


    @Test
    public void testResolvePathUserRelativePath() {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        Master master = new Master(emul, "id", "name");

        setStandardFlow(emul);
        String userWorkspace = System.getProperty("user.dir") + File.separator + "job/logs/100";
        CiPostProcess postProcess = new CiPostProcess(true, true, "relative1/jtl/rep", "relative2/junit/rep", userWorkspace, notifier, logger);
        postProcess.setTest("testId", "testType");
        postProcess.execute(master);
        String notifiers = notifier.getLogs().toString();

        String jtlPath = notifiers.substring(notifiers.indexOf("Saving jtl report"), notifiers.indexOf("Trying to get functional summar"));
        System.out.println(jtlPath);
        assertTrue(notifiers, jtlPath.contains("job/logs/100/relative1/jtl/rep/r-v3-1234567890qwerty"));

        String junitPath = notifiers.substring(notifiers.indexOf("Saving junit report"), notifiers.indexOf("Saving jtl"));
        System.out.println(junitPath);
        assertTrue(notifiers, junitPath.contains("job/logs/100/relative2/junit/rep/id.xml"));
    }

    @Test
    public void testResolvePathUserRelativePathNullWorkspace() {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        Master master = new Master(emul, "id", "name");

        setStandardFlow(emul);
        CiPostProcess postProcess = new CiPostProcess(true, true, "relative1/jtl/rep", "relative2/junit/rep", null, notifier, logger);
        postProcess.setTest("testId", "testType");
        postProcess.execute(master);
        String notifiers = notifier.getLogs().toString();

        String jtlPath = notifiers.substring(notifiers.indexOf("Saving jtl report"), notifiers.indexOf("Trying to get functional summar"));
        System.out.println(jtlPath);
        assertTrue(notifiers, jtlPath.contains("/bzm_tmp"));
        assertTrue(notifiers, jtlPath.contains("/relative1/jtl/rep/r-v3-1234567890qwerty"));

        String junitPath = notifiers.substring(notifiers.indexOf("Saving junit report"), notifiers.indexOf("Saving jtl"));
        System.out.println(junitPath);
        assertTrue(notifiers, junitPath.contains("/bzm_tmp"));
        assertTrue(notifiers, junitPath.contains("/relative2/junit/rep/id.xml"));
    }


    @Test
    public void testResolvePathUserAbsolutePath() throws IOException {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        Master master = new Master(emul, "id", "name");

        setStandardFlow(emul);
        String userWorkspace = System.getProperty("user.dir") + File.separator + "job/logs/100";

        File jtlDir = CiPostProcess.createTmpDir();
        File junitDir = CiPostProcess.createTmpDir();

        CiPostProcess postProcess = new CiPostProcess(true, true, jtlDir.getAbsolutePath(), junitDir.getAbsolutePath(), userWorkspace, notifier, logger);
        postProcess.setTest("testId", "testType");
        postProcess.execute(master);
        String notifiers = notifier.getLogs().toString();

        String jtlPath = notifiers.substring(notifiers.indexOf("Saving jtl report"), notifiers.indexOf("Trying to get functional summar"));
        System.out.println(jtlPath);
        assertTrue(notifiers, jtlPath.contains(jtlDir.getAbsolutePath() + File.separator + "r-v3-1234567890qwerty"));

        String junitPath = notifiers.substring(notifiers.indexOf("Saving junit report"), notifiers.indexOf("Saving jtl"));
        System.out.println(junitPath);
        assertTrue(notifiers, junitPath.contains(junitDir.getAbsolutePath() + File.separator + "id.xml"));
    }

    @Test
    public void testResolvePathUserAbsolutePathNullWorkspace() throws IOException {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        Master master = new Master(emul, "id", "name");

        setStandardFlow(emul);
        File jtlDir = CiPostProcess.createTmpDir();
        File junitDir = CiPostProcess.createTmpDir();

        CiPostProcess postProcess = new CiPostProcess(true, true, jtlDir.getAbsolutePath(), junitDir.getAbsolutePath(), null, notifier, logger);
        postProcess.setTest("testId", "testType");
        postProcess.execute(master);
        String notifiers = notifier.getLogs().toString();

        String jtlPath = notifiers.substring(notifiers.indexOf("Saving jtl report"), notifiers.indexOf("Trying to get functional summar"));
        System.out.println(jtlPath);
        assertTrue(notifiers, jtlPath.contains(jtlDir.getAbsolutePath() + File.separator + "r-v3-1234567890qwerty"));

        String junitPath = notifiers.substring(notifiers.indexOf("Saving junit report"), notifiers.indexOf("Saving jtl"));
        System.out.println(junitPath);
        assertTrue(notifiers, junitPath.contains(junitDir.getAbsolutePath() + File.separator + "id.xml"));
    }

    private void setStandardFlow(BlazeMeterUtilsEmul emul) {
        emul.addEmul(MasterTest.generateResponseGetPerformanceCIStatus());
        emul.addEmul(MasterTest.generateResponseGetHasDataStatus());
        emul.addEmul("junit report");
        emul.addEmul(MasterTest.generateResponseGetSessions());
        emul.addEmul(SessionTest.generateResponseGetJTLReport());
        emul.addEmul(MasterTest.generateResponseGetHasDataStatus());
        emul.addEmul(MasterTest.generateResponseGetFunctionalReport());
    }

    @Test
    public void testRepeatDownloading5Attempts() throws Exception {
        String val = System.getProperty("bzm.checkTimeout", "10000");
        try {
            System.setProperty("bzm.checkTimeout", "10");
            LoggerTest logger = new LoggerTest();
            UserNotifierTest notifier = new UserNotifierTest();
            BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
            CiPostProcess ciPostProcess = new CiPostProcess(false, false, "", "", "", notifier, logger);
            emul.addEmul(MasterTest.generateResponseGetSessions());
            emul.addEmul(SessionTest.generateResponseGetJTLReportNullZip());
            emul.addEmul(SessionTest.generateResponseGetJTLReportNullZip());
            emul.addEmul(SessionTest.generateResponseGetJTLReportNullZip());
            emul.addEmul(SessionTest.generateResponseGetJTLReportNullZip());
            emul.addEmul(SessionTest.generateResponseGetJTLReportNullZip());

            Master master = new Master(emul, "id", "name");
            ciPostProcess.saveJTL(master);

            String logs = notifier.getLogs().toString();
            assertTrue(logs, logs.contains("Failed to get JTL ZIP for session id"));
            assertEquals(6, emul.getRequests().size());
        } finally {
            System.setProperty("bzm.checkTimeout", val);
        }
    }


    @Test
    public void testEmptyJUnit() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        CiPostProcess ciPostProcess = new CiPostProcess(false, false, "", "", "", notifier, logger);
        emul.addEmul("");

        Master master = new Master(emul, "id", "name");
        ciPostProcess.saveJunit(master);

        String logs = notifier.getLogs().toString();
        assertTrue(logs, logs.contains("Got empty junit report from server"));
        assertEquals(1, emul.getRequests().size());
    }

    @Test
    public void testJTLPrivateLink() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul("http://hahaha111.url", BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(MasterTest.generateResponseGetSessions());
        emul.addEmul(SessionTest.generateResponseGetJTLReportWithRelativeUrl());

        File jtlDir = CiPostProcess.createTmpDir();

        CiPostProcess ciPostProcess = new CiPostProcess(false, false, jtlDir.getAbsolutePath(), "", "", emul);

        Master master = new Master(emul, "id", "name");
        ciPostProcess.saveJTL(master);


        String logs = logger.getLogs().toString();
        assertTrue(logs, logs.contains("Unable to get JTL zip for url=http://hahaha111.url/api/veeeersion/sssss?file=sessions/sessionID/jtls_and_more.zip : check server for test artifacts \r\n" +
                "hahaha111.url"));
    }

    @Test
    public void testUnzipJTLWithNestedFoldersAndFiles() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        InputStream report = CiPostProcessTest.class.getClassLoader().getResourceAsStream("report.zip");
        assertNotNull(report);
        File tmpDir = CiPostProcess.createTmpDir();

        try {
            CiPostProcess postProcess = new CiPostProcess(true, true, "", "", "", emul);
            postProcess.unzipJTL(report, tmpDir);

            File kpiJtlFile = new File(tmpDir, "report/nested/kpi.jtl");
            assertTrue(kpiJtlFile.exists());
            assertEquals(259, kpiJtlFile.length());

            File errorJtlFile = new File(tmpDir, "report/error.jtl");
            assertTrue(errorJtlFile.exists());
            assertEquals(533, errorJtlFile.length());
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        } finally {
            tmpDir.delete();
        }
    }

    @Test
    public void testValidateTestSuiteCiStatusSuccess() {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseTestSuiteCiStatusSuccess());

        CiPostProcess ciPostProcess = new CiPostProcess(false, false, "", "", "", notifier, logger);
        BuildResult buildResult = ciPostProcess.validateTestSuiteCiStatus(JSONObject.fromObject(generateResponseTestSuiteCiStatusSuccess()).getJSONObject("result"));
        assertEquals(BuildResult.SUCCESS, buildResult);
    }

    public static String generateResponseTestSuiteCiStatusSuccess() {

        JSONObject suiteSummary = new JSONObject();
        suiteSummary.put("definedStatus", "passed");

        JSONObject testSuiteSummary = new JSONObject();
        testSuiteSummary.put("suiteSummary",suiteSummary);

        JSONObject result = new JSONObject();
        result.put("testSuiteSummary", testSuiteSummary);

        JSONObject response = new JSONObject();
        response.put("result", result);

        return response.toString();
    }


    @Test
    public void testValidateTestSuiteCiStatusFailure() {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();

        CiPostProcess ciPostProcess = new CiPostProcess(false, false, "", "", "", notifier, logger);
        BuildResult buildResult = ciPostProcess.validateTestSuiteCiStatus(JSONObject.fromObject(generateResponseTestSuiteCiStatusFailure()).getJSONObject("result"));
        assertEquals(BuildResult.FAILED, buildResult);
    }

    public static String generateResponseTestSuiteCiStatusFailure() {

        JSONObject suiteSummary = new JSONObject();
        suiteSummary.put("definedStatus", "failed");

        JSONObject testSuiteSummary = new JSONObject();
        testSuiteSummary.put("suiteSummary",suiteSummary);

        JSONObject result = new JSONObject();
        result.put("testSuiteSummary", testSuiteSummary);

        JSONObject response = new JSONObject();
        response.put("result", result);

        return response.toString();
    }
}