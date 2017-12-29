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

import com.blazemeter.api.exception.UnexpectedResponseException;
import com.blazemeter.api.explorer.Master;
import com.blazemeter.api.explorer.MasterTest;
import com.blazemeter.api.explorer.SessionTest;
import com.blazemeter.api.explorer.test.SingleTestTest;
import com.blazemeter.api.logging.LoggerTest;
import com.blazemeter.api.logging.UserNotifierTest;
import com.blazemeter.api.utils.BlazeMeterUtilsEmul;
import net.sf.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_ADDRESS;
import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_DATA_ADDRESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CiBuildTest {
    @Before
    public void setUp() throws Exception {
        System.setProperty("bzm.checkTimeout", "1000");
        System.setProperty("bzm.minute", "1500");
    }

    @After
    public void tearDown() throws Exception {
        System.setProperty("bzm.checkTimeout", "10000");
        System.setProperty("bzm.minute", "60000");
    }

    @Test
    public void testWaitForFinish() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject result = new JSONObject();
        result.put("progress", 70);

        JSONObject response = new JSONObject();
        response.put("result", result);
        emul.addEmul(response.toString());

        result = new JSONObject();
        result.put("progress", 140);
        response.put("result", result);
        emul.addEmul(response.toString());

        Master master = new Master(emul, "id", "name");
        CiPostProcess postProcess = new CiPostProcess(false, false, "", "", "", notifier, logger);
        CiBuild ciBuild = new CiBuild(emul, "id", "", "", postProcess);
        ciBuild.waitForFinish(master);
        assertEquals(2, emul.getRequests().size());
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/id/status?events=false, tag=null}", emul.getRequests().get(0));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/id/status?events=false, tag=null}", emul.getRequests().get(1));
        assertEquals(363, logger.getLogs().length());
    }

    @Test
    public void testExecute() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        setEmulator(emul);
        CiPostProcess postProcess = new CiPostProcess(false, false, "", "", "", notifier, logger);
        CiBuild ciBuild = new CiBuild(emul, "testId", "1=2", "i", postProcess);
        ciBuild.execute();
        assertEquals(13, emul.getRequests().size());
        int i = 0;
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/tests/testId, tag=null}", emul.getRequests().get(i++));
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/tests/testId/start, tag=null}", emul.getRequests().get(i++));
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/masters/responseMasterId/public-token, tag=null}", emul.getRequests().get(i++));
        assertEquals("http://a.blazemeter.com/app/?public-token=x1x1x1x1x1x1x1x11x1x1x1#/masters/responseMasterId/summary",
                ciBuild.getPublicReport());
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/responseMasterId/status?events=false, tag=null}", emul.getRequests().get(i++));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/responseMasterId/status?events=false, tag=null}", emul.getRequests().get(i++));
        assertEquals("Request{method=PATCH, url=http://a.blazemeter.com/api/v4/masters/responseMasterId, tag=null}", emul.getRequests().get(i++));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/responseMasterId/sessions, tag=null}", emul.getRequests().get(i++));
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/sessions/r-v3-1234567890qwerty/properties?target=all, tag=null}", emul.getRequests().get(i++));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/responseMasterId/status?events=false, tag=null}", emul.getRequests().get(i++));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/responseMasterId/status?events=false, tag=null}", emul.getRequests().get(i++));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/responseMasterId/ci-status, tag=null}", emul.getRequests().get(i++));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/responseMasterId, tag=null}", emul.getRequests().get(i++));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/responseMasterId/reports/main/summary, tag=null}", emul.getRequests().get(i));
        String logs = logger.getLogs().toString();
        assertTrue(logs, logs.contains("Get Single Test id=testId"));
        assertTrue(logs, logs.contains("Start single test id=testId"));
        assertTrue(logs, logs.contains("Get link to public report for master id=responseMasterId"));
        assertTrue(logs, logs.contains("Post notes to master id=responseMasterId"));
        assertTrue(logs, logs.contains("Get list of sessions for master id=responseMasterId"));
        assertTrue(logs, logs.contains("Post properties to session id=r-v3-1234567890qwerty"));
        assertTrue(logs, logs.contains("Response: {\"result\":{\"progress\":70}}"));
        assertTrue(logs, logs.contains("Response: {\"result\":{\"progress\":140}}"));
        assertEquals(logs, 3116, logger.getLogs().length());
    }


    private void setEmulator(BlazeMeterUtilsEmul emul) {
        emul.addEmul(SingleTestTest.generateResponseGetSingleTest());
        emul.addEmul(SingleTestTest.generateResponseStartSingleTest());

        String token = "x1x1x1x1x1x1x1x11x1x1x1";
        emul.addEmul(MasterTest.generateResponseGetPublicToken(token));
        emul.addEmul(MasterTest.generateResponseGetStatus(0));
        emul.addEmul(MasterTest.generateResponseGetStatus(90));
        emul.addEmul(MasterTest.generateResponsePostNote());
        emul.addEmul(MasterTest.generateResponseGetSessions());
        emul.addEmul(SessionTest.generateResponsePostProperties());

        emul.addEmul(MasterTest.generateResponseGetStatus(70));
        emul.addEmul(MasterTest.generateResponseGetStatus(140));
        emul.addEmul(MasterTest.generateResponseGetCIStatus());
    }

    @Test
    public void testExecuteFailStart() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        CiPostProcess postProcess = new CiPostProcess(false, false, "", "", "", notifier, logger);
        CiBuild ciBuild = new CiBuild(emul, "id", "1=2", "", postProcess);
        BuildResult result = ciBuild.execute();
        assertEquals(BuildResult.FAILED, result);

        assertEquals(1, emul.getRequests().size());
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/tests/id, tag=null}", emul.getRequests().get(0));
        String logs = logger.getLogs().toString();
        assertEquals(logs, 272, logs.length());
        assertTrue(logs, logs.contains("Caught exception. Set Build status [FAILED]. Reason is:"));
    }

    @Test
    public void testGetters() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        CiPostProcess postProcess = new CiPostProcess(false, false, "", "", "", notifier, logger);
        CiBuild ciBuild = new CiBuild(emul, "id", "props", "notes", postProcess);

        assertEquals("id", ciBuild.getTestId());
        assertEquals(emul, ciBuild.getUtils());
        assertEquals("props", ciBuild.getProperties());
        assertEquals("notes", ciBuild.getNotes());
        assertNull(ciBuild.getPublicReport());
        ciBuild.setPublicReport("report");
        assertEquals("report", ciBuild.getPublicReport());

        assertNotNull(ciBuild.getCiPostProcess());
    }

    @Test
    public void testStartFailDetect() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(SingleTestTest.generateResponseGetSingleTest_NoSuchTest());
        emul.addEmul(SingleTestTest.generateResponseGetSingleTest_NoSuchTest());

        CiBuild ciBuild = new CiBuild(emul, "id", "props", "notes", null);
        Master master = ciBuild.start();
        assertNull(master);
        assertEquals(2, emul.getRequests().size());
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/tests/id, tag=null}", emul.getRequests().get(0));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/multi-tests/id, tag=null}", emul.getRequests().get(1));

        String logs = logger.getLogs().toString();
        assertEquals(logs, 705, logs.length());
        assertTrue(logs, logs.contains("Failed to detect test type. Test with id=id not found."));
    }

    @Test
    public void testExecuteFailDetect() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(SingleTestTest.generateResponseGetSingleTest_NoSuchTest());
        emul.addEmul(SingleTestTest.generateResponseGetSingleTest_NoSuchTest());

        CiBuild ciBuild = new CiBuild(emul, "id", "props", "notes", null);
        BuildResult result = ciBuild.execute();
        assertEquals(BuildResult.FAILED, result);
        assertEquals(2, emul.getRequests().size());

        String logs = logger.getLogs().toString();
        assertEquals(logs, 733, logs.length());
        assertTrue(logs, logs.contains("Set Build status [FAILED]."));
    }


    @Test
    public void testInterrupt() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        CiBuild ciBuild = new CiBuild(emul, "id", "props", "notes", null);
        Master master = new Master(emul, "id", "name");

        emul.addEmul(MasterTest.generateResponseGetStatus(70));
        emul.addEmul(MasterTest.generateResponseTerminateMaster());
        boolean interrupt = ciBuild.interrupt(master);
        assertFalse(interrupt);

        emul.addEmul(MasterTest.generateResponseGetStatus(110));
        emul.addEmul(MasterTest.generateResponseStopMaster());
        interrupt = ciBuild.interrupt(master);
        assertTrue(interrupt);
    }

    @Test
    public void testStartFailed() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        CiBuild ciBuild = new CiBuild(emul, "id", "props", "notes", null) {
            @Override
            public Master start() throws IOException {
                return null;
            }
        };
        BuildResult result = ciBuild.execute();

        assertEquals(BuildResult.FAILED, result);
        String logs = notifier.getLogs().toString();
        assertTrue(logs, logs.contains("Set Build status [FAILED]."));
    }

    @Test
    public void testSkipInitStateFailed() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        CiBuild ciBuild = new CiBuild(emul, "id", "props", "notes", null);
        Master master = new Master(emul, "id", "name");
        ciBuild.skipInitState(master);

        String logs = logger.getLogs().toString();
        assertTrue(logs, logs.contains("Failed to skip INIT state"));
    }
}
