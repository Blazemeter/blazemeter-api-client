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
import com.blazemeter.api.explorer.MasterTest;
import com.blazemeter.api.explorer.SessionTest;
import com.blazemeter.api.logging.LoggerTest;
import com.blazemeter.api.logging.UserNotifierTest;
import com.blazemeter.api.utils.BlazeMeterUtilsEmul;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_ADDRESS;
import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_DATA_ADDRESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
    public void testCreateJunitFileException() {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        CiPostProcess ciPostProcess = new CiPostProcess(false, false, "", "", "", notifier, logger);
        try {
            ciPostProcess.createJunitFile("", "./junit");
            File junit = new File("./junit");
            assertTrue(junit.exists());
            junit.delete();
            assertFalse(junit.exists());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testCreateJunitFileSuccess() {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        CiPostProcess ciPostProcess = new CiPostProcess(false, false, "", "", "", notifier, logger);
        try {
            ciPostProcess.createJunitFile("./junit", "./junit111");
            File junit = new File("./junit");
            assertTrue(junit.exists());
            junit.delete();
            assertFalse(junit.exists());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testUnzipJTL() {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        CiPostProcess ciPostProcess = new CiPostProcess(false, false, "", "", "", notifier, logger);
        try {
            File sampleJtl = new File("sample.jtl");
            sampleJtl.createNewFile();
            FileInputStream in = new FileInputStream(sampleJtl);
            File bzmZip = new File("bzm.zip");
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(bzmZip));
            out.putNextEntry(new ZipEntry("sample.jtl"));
            // buffer size
            byte[] b = new byte[1024];
            int count;
            while ((count = in.read(b)) > 0) {
                out.write(b, 0, count);
            }
            out.close();
            in.close();
            InputStream is = new FileInputStream(bzmZip);
            ciPostProcess.unzipJTL(is);
            File bmKpi = new File("bm-kpis.jtl");
            assertTrue(bmKpi.exists());
            bmKpi.delete();
            bzmZip.delete();
        } catch (Exception e) {
            fail();
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
    }

    @Test
    public void testValidateCIStatusSuccess() {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseCIStatusSuccess());

        CiPostProcess ciPostProcess = new CiPostProcess(false, false, "", "", "", notifier, logger);
        Master master = new Master(emul, "id", "name");
        BuildResult buildResult = ciPostProcess.validateCiStatus(master);
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
    public void testValidateCIStatusFailure() {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseCIStatus_Failure_610000());

        CiPostProcess ciPostProcess = new CiPostProcess(false, false, "", "", "", notifier, logger);
        Master master = new Master(emul, "id", "name");
        BuildResult buildResult = ciPostProcess.validateCiStatus(master);
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
    public void testValidateCIStatus70404Failure() {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseCIStatus_Error_70404());

        CiPostProcess ciPostProcess = new CiPostProcess(false, false, "", "", "", notifier, logger);
        Master master = new Master(emul, "id", "name");
        BuildResult buildResult = ciPostProcess.validateCiStatus(master);
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
    public void testValidateCIStatusError() {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseCIStatus_Error_111());

        CiPostProcess ciPostProcess = new CiPostProcess(false, false, "", "", "", notifier, logger);
        Master master = new Master(emul, "id", "name");
        BuildResult buildResult = ciPostProcess.validateCiStatus(master);
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
    public void testGetters() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        CiPostProcess ciPostProcess = new CiPostProcess(true, true, "junit", "jtl", "pwd", notifier, logger);

        assertTrue(ciPostProcess.isDownloadJtl());
        assertTrue(ciPostProcess.isDownloadJunit());
        assertEquals("junit", ciPostProcess.getJunitPath());
        assertEquals("jtl", ciPostProcess.getJtlPath());
        assertEquals("pwd", ciPostProcess.getWorkspaceDir());
    }
}
