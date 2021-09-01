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

package com.blazemeter.api.explorer;

import com.blazemeter.api.logging.LoggerTest;
import com.blazemeter.api.logging.UserNotifier;
import com.blazemeter.api.logging.UserNotifierTest;
import com.blazemeter.api.utils.BlazeMeterUtilsEmul;
import com.blazemeter.api.utils.BlazeMeterUtilsSlowEmul;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_ADDRESS;
import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_DATA_ADDRESS;
import static org.junit.Assert.*;

public class MasterTest {

    public static final double DELTA = 0.001;

    @Test
    public void testGetJUnitReport() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        String result = "junit";
        emul.addEmul(result);
        Master master = new Master(emul, "id", "name");

        assertEquals("junit", master.getJUnitReport());
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/id/reports/thresholds?format=junit, tag=null}", emul.getRequests().get(0));
        String logs = logger.getLogs().toString();
        assertEquals(logs, 182, logs.length());
        assertTrue(logs, logs.contains("Get JUnit report for master id=id"));
    }

    @Test
    public void testGetPerformanceCIStatus() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseGetPerformanceCIStatus());

        Master master = new Master(emul, "id", "name");

        JSONObject ciStatus = master.getPerformanceCIStatus();
        assertTrue(ciStatus.has("masterId"));
        assertEquals("id", ciStatus.getString("masterId"));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/id/ci-status, tag=null}", emul.getRequests().get(0));
        String logs = logger.getLogs().toString();
        assertEquals(logs, 206, logs.length());
        assertTrue(logs, logs.contains("Get CI status for master id=id"));
    }

    public static String generateResponseGetPerformanceCIStatus() {
        JSONObject status = new JSONObject();
        status.put("masterId", "id");

        JSONArray errors = new JSONArray();
        JSONArray fails = new JSONArray();
        status.put("errors", errors);
        status.put("failures", fails);

        JSONObject result = new JSONObject();
        result.put("result", status);
        return result.toString();
    }


    @Test
    public void testGetFunctionalCIStatus() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseGetFunctionalCIStatus());

        Master master = new Master(emul, "id", "name");

        JSONObject ciStatus = master.getFunctionalCIStatus();
        assertTrue(ciStatus.has("masterId"));
        assertEquals("id", ciStatus.getString("masterId"));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/id, tag=null}", emul.getRequests().get(0));
        String logs = logger.getLogs().toString();
        assertEquals(logs, 211, logs.length());
        assertTrue(logs, logs.contains("Get CI status for master id=id"));
    }

    public static String generateResponseGetFunctionalCIStatus() {
        JSONObject status = new JSONObject();
        status.put("masterId", "id");

        JSONObject gridSummary = new JSONObject();
        gridSummary.put("definedStatus", "passed");
        status.put("gridSummary", gridSummary);

        JSONObject result = new JSONObject();
        result.put("result", status);
        return result.toString();
    }

    @Test
    public void testGetPublicToken() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        String token = "x1x1x1x1x1x1x1x11x1x1x1";
        emul.addEmul(generateResponseGetPublicToken(token));

        Master master = new Master(emul, "id", "name");

        String expectedUrl = emul.getAddress() + String.format("/app/?public-token=%s#/masters/%s/summary", token, master.getId());
        assertEquals(expectedUrl, master.getPublicReport());
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/masters/id/public-token, tag=null}", emul.getRequests().get(0));
        String logs = logger.getLogs().toString();
        assertEquals(logs, 220, logs.length());
        assertTrue(logs, logs.contains("Get link to public report for master id=id"));
    }

    public static String generateResponseGetAccountId() {
        JSONObject accId = new JSONObject();
        accId.put("accountId", "12345");

        JSONObject defaultProject = new JSONObject();
        defaultProject.put("defaultProject", accId);

        JSONObject result = new JSONObject();
        result.put("result", defaultProject);
        return result.toString();
    }

    public static String generateResponseGetProjectId() {
        JSONObject projectId = new JSONObject();
        projectId.put("projectId", "12345");

        JSONObject result = new JSONObject();
        result.put("result", projectId);
        return result.toString();
    }

    public static String generateResponseGetPublicToken(String token) {
        JSONObject publicToken = new JSONObject();
        publicToken.put("publicToken", token);

        JSONObject result = new JSONObject();
        result.put("result", publicToken);
        return result.toString();
    }

    @Test
    public void testGetSessions() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        emul.addEmul(generateResponseGetSessions());

        Master master = new Master(emul, "id", "name");

        List<Session> sessionsList = master.getSessions();
        assertEquals(1, sessionsList.size());
        assertEquals("11", sessionsList.get(0).getName());
        assertEquals("12", sessionsList.get(0).getUserId());
        assertEquals("13", sessionsList.get(0).getTestId());
        assertEquals("r-v3-1234567890qwerty", sessionsList.get(0).getId());
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/sessions?masterId=id, tag=null}", emul.getRequests().get(0));

        String logs = logger.getLogs().toString();
        assertEquals(logs, 242, logs.length());
        assertTrue(logs, logs.contains("Get list of sessions for master id=id"));
    }

    public static String generateResponseGetSessions() {
        JSONObject session = new JSONObject();
        session.put("id", "r-v3-1234567890qwerty");
        session.put("name", "11");
        session.put("userId", "12");
        session.put("testId", "13");

        JSONArray sessionsArray = new JSONArray();
        sessionsArray.add(session);

        JSONObject result = new JSONObject();
        result.put("result", sessionsArray);
        return result.toString();
    }

    @Test
    public void testStop() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseStopMaster());

        Master master = new Master(emul, "id", "name");

        JSONArray stop = master.stop();
        assertEquals(1, stop.size());

        JSONObject sr = stop.getJSONObject(0);
        assertTrue(sr.has("session_id"));
        assertTrue(sr.has("result"));
        assertEquals("r-v3-1234567890qwerty", sr.get("session_id"));
        assertEquals("shutdown command sent\n", sr.get("result"));
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/masters/id/stop, tag=null}", emul.getRequests().get(0));
        String logs = logger.getLogs().toString();
        assertEquals(logs, 221, logs.length());
        assertTrue(logs, logs.contains("Stop master id=id"));
    }

    public static String generateResponseStopMaster() {
        JSONObject stopObject = new JSONObject();
        stopObject.put("session_id", "r-v3-1234567890qwerty");
        stopObject.put("result", "shutdown command sent\n");

        JSONArray stopArray = new JSONArray();
        stopArray.add(stopObject);

        JSONObject result = new JSONObject();
        result.put("result", stopArray);
        return result.toString();
    }

    @Test
    public void testTerminate() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseTerminateMaster());
        Master master = new Master(emul, "id", "name");

        JSONArray terminateResponse = master.terminate();
        assertEquals(1, terminateResponse.size());

        JSONObject terminate = terminateResponse.getJSONObject(0);
        assertTrue(terminate.has("session_id"));
        assertTrue(terminate.has("result"));
        assertEquals("r-v3-1234567899qwerty", terminate.get("session_id"));
        assertTrue(terminate.getBoolean("result"));
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/masters/id/terminate, tag=null}", emul.getRequests().get(0));
        String logs = logger.getLogs().toString();
        assertEquals(logs, 210, logs.length());
        assertTrue(logs, logs.contains("Terminate master id=id"));
    }

    public static String generateResponseTerminateMaster() {
        JSONObject terminateObject = new JSONObject();
        terminateObject.put("session_id", "r-v3-1234567899qwerty");
        terminateObject.put("result", true);

        JSONArray terminateArray = new JSONArray();
        terminateArray.add(terminateObject);

        JSONObject result = new JSONObject();
        result.put("result", terminateArray);
        return result.toString();
    }

    @Test
    public void testGetStatus() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseGetStatus(100));

        Master master = new Master(emul, "id", "name");
        assertEquals(100, master.getStatus());
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/id/status?events=false, tag=null}", emul.getRequests().get(0));
        String logs = logger.getLogs().toString();
        assertEquals(logs, 182, logs.length());
        assertTrue(logs, logs.contains("Get master status id=id"));
    }

    public static String generateResponseGetStatus(int status) {
        JSONObject result = new JSONObject();
        result.put("progress", status);

        JSONObject response = new JSONObject();
        response.put("result", result);
        return response.toString();
    }

    @Test
    public void testGetSummary() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseGetSummary());

        Master master = new Master(emul, "id", "name");
        JSONObject summary = master.getSummary();

        assertEquals(7, summary.size());
        assertEquals(1.47, summary.getDouble("avg"), DELTA);
        assertEquals(0, summary.getInt("min"));
        assertEquals(177, summary.getInt("max"));
        assertEquals(2, summary.getInt("tp90"));
        assertEquals(49, summary.getInt("errorPercentage"));
        assertEquals(2482, summary.getInt("hits"));
        assertEquals(8.25, summary.getDouble("avgthrpt"), DELTA);

        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/id/reports/main/summary, tag=null}", emul.getRequests().get(0));
        String logs = logger.getLogs().toString();
        assertEquals(logs, 288, logs.length());
        assertTrue(logs, logs.contains("Get summary for master id=id"));
    }

    @Test
    public void testGetEmptySummary() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject result = new JSONObject();
        result.put("summary", new JSONArray());

        JSONObject response = new JSONObject();
        response.put("result", result);
        emul.addEmul(response.toString());

        Master master = new Master(emul, "id", "name");
        JSONObject summary = master.getSummary();
        assertTrue(summary.isEmpty());
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/id/reports/main/summary, tag=null}", emul.getRequests().get(0));
        String logs = logger.getLogs().toString();
        assertEquals(logs, 186, logs.length());
        assertTrue(logs, logs.contains("Get summary for master id=id"));
    }

    @Test
    public void getGetSummaryDivideBy0() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject summaryEmul = new JSONObject();
        summaryEmul.put("first", 1437397105);
        summaryEmul.put("last", 1437397105);
        summaryEmul.put("min", 0);
        summaryEmul.put("max", 177);
        summaryEmul.put("tp90", 2);
        summaryEmul.put("tp90", 2);
        summaryEmul.put("failed", 1236);
        summaryEmul.put("hits", 2482);
        summaryEmul.put("avg", 1.47);

        JSONArray sumArrray = new JSONArray();
        sumArrray.add(summaryEmul);

        JSONObject result = new JSONObject();
        result.put("summary", sumArrray);

        JSONObject response = new JSONObject();
        response.put("result", result);
        emul.addEmul(response.toString());

        Master master = new Master(emul, "id", "name");
        JSONObject summary = master.getSummary();
        assertEquals("{\"avg\":1.47,\"min\":0,\"max\":177,\"tp90\":2,\"errorPercentage\":49,\"hits\":2482,\"avgthrpt\":2482}", summary.toString());
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/id/reports/main/summary, tag=null}", emul.getRequests().get(0));
    }

    public static String generateResponseGetSummary() {
        JSONObject summaryEmul = new JSONObject();
        summaryEmul.put("first", 1437397105);
        summaryEmul.put("last", 1437397406);
        summaryEmul.put("min", 0);
        summaryEmul.put("max", 177);
        summaryEmul.put("tp90", 2);
        summaryEmul.put("tp90", 2);
        summaryEmul.put("failed", 1236);
        summaryEmul.put("hits", 2482);
        summaryEmul.put("avg", 1.47);

        JSONArray sumArrray = new JSONArray();
        sumArrray.add(summaryEmul);

        JSONObject result = new JSONObject();
        result.put("summary", sumArrray);

        JSONObject response = new JSONObject();
        response.put("result", result);
        return response.toString();
    }


    @Test
    public void testGetFunctionalReport() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseGetFunctionalReport());

        Master master = new Master(emul, "id", "name");

        JSONObject funcReport = master.getFunctionalReport();
        assertEquals(8, funcReport.size());
        emul.clean();
        logger.reset();

        JSONObject response = new JSONObject();
        response.put("result", new JSONObject());
        emul.addEmul(response.toString());
        master = new Master(emul, "id", "name");

        funcReport = master.getFunctionalReport();
        assertEquals(0, funcReport.size());
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/id, tag=null}", emul.getRequests().get(0));
        String logs = logger.getLogs().toString();
        assertEquals(logs, 163, logs.length());
        assertTrue(logs, logs.contains("Get functional report for master id=id"));
    }

    public static String generateResponseGetFunctionalReport() {
        JSONObject funcReportEmul = new JSONObject();
        funcReportEmul.put("testsCount", 1);
        funcReportEmul.put("requestsCount", 1);
        funcReportEmul.put("errorsCount", 1);
        funcReportEmul.put("assertions", new JSONObject());
        funcReportEmul.put("responseTime", new JSONObject());
        funcReportEmul.put("isFailed", true);
        funcReportEmul.put("failedCount", 1);
        funcReportEmul.put("failedPercentage", 100);

        JSONObject result = new JSONObject();
        result.put("functionalSummary", funcReportEmul);

        JSONObject response = new JSONObject();
        response.put("result", result);
        return response.toString();
    }

    @Test
    public void testPostNote() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponsePostNote());

        Master master = new Master(emul, "id", "name");

        String note = master.postNotes("valid\r\nmulti\r\nline");
        assertEquals("valid\r\n" +
                "multi\r\n" +
                "line", note);
        assertEquals("Request{method=PATCH, url=http://a.blazemeter.com/api/v4/masters/id, tag=null}", emul.getRequests().get(0));
        assertEquals("[text={\"note\":\"valid\\\\r\\\\nmulti\\\\r\\\\nline\"}]", emul.getRequestsBody().get(0));
        String logs = logger.getLogs().toString();
        assertEquals(logs, 184, logs.length());
        assertTrue(logs, logs.contains("Post notes to master id=id"));
    }

    @Test
    public void testPostNullNote() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        Master master = new Master(emul, "id", "name");

        String note = master.postNotes(null);
        assertEquals("", note);
        String logs = logger.getLogs().toString();
        assertEquals(logs, 33, logs.length());
        assertTrue(logs, logs.contains("Cannot send null or empty notes"));
    }

    public static String generateResponsePostNote() {
        JSONObject result = new JSONObject();
        result.put("note", "valid\r\n" +
                "multi\r\n" +
                "line");

        JSONObject response = new JSONObject();
        response.put("result", result);
        return response.toString();
    }

    @Test
    public void testFromJSON() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject object = new JSONObject();
        object.put("id", "masterId");
        object.put("name", "masterName");

        Master master = Master.fromJSON(emul, object);
        assertEquals("masterId", master.getId());
        assertEquals("masterName", master.getName());
    }

    @Test
    public void testPostProperties() throws InterruptedException, IOException {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseGetSessions());
        emul.addEmul(SessionTest.generateResponsePostProperties());

        Master master = new Master(emul, "id", "name");
        master.postProperties("1=2");
        assertEquals(2, emul.getRequests().size());
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/sessions?masterId=id, tag=null}", emul.getRequests().get(0));
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/sessions/r-v3-1234567890qwerty/properties?target=all, tag=null}", emul.getRequests().get(1));
        String logs = logger.getLogs().toString();
        assertEquals(logs, 526, logs.length());
        assertTrue(logs, logs.contains("Post properties to master id=id"));
        assertTrue(logs, logs.contains("Post properties to session id=r-v3-1234567890qwerty"));
    }

    @Test
    public void testPostPropertiesEmpty() throws InterruptedException, IOException {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseGetSessions());
        emul.addEmul(SessionTest.generateResponsePostProperties());

        Master master = new Master(emul, "id", "name");
        master.postProperties("");
        assertEquals(0, emul.getRequests().size());
        String logs = logger.getLogs().toString();
        assertEquals(logs, 52, logs.length());
        assertTrue(logs, logs.contains("Properties are empty, won't be sent to master = "));
    }

    @Test
    public void testPostPropertiesFailGetSessions() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        Master master = new Master(emul, "id", "name");
        master.postProperties("1=2");

        String logs = logger.getLogs().toString();
        assertTrue(logs, logs.contains("Failed to get sessions for master id=id"));
        assertEquals(logs, 246, logs.length());
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/sessions?masterId=id, tag=null}", emul.getRequests().get(0));
    }

    @Test
    public void testPostPropertiesFailPostPropToSessions() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseGetSessions());
        Master master = new Master(emul, "id", "name");
        master.postProperties("1=2");

        String logs = logger.getLogs().toString();
        assertTrue(logs, logs.contains("Failed to send properties for session id=r-v3-1234567890qwerty"));
        assertEquals(logs, 558, logs.length());

        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/sessions?masterId=id, tag=null}", emul.getRequests().get(0));
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/sessions/r-v3-1234567890qwerty/properties?target=all, tag=null}", emul.getRequests().get(1));
    }

    @Test
    public void testPostPropertiesInterrupt() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        final BlazeMeterUtilsSlowEmul emul = new BlazeMeterUtilsSlowEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseGetSessions());

        final Throwable ex[] = {null};
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    Master master = new Master(emul, "id", "name");
                    master.postProperties("1=2");
                } catch (Exception e) {
                    ex[0] = e;
                }
            }
        };
        t.start();
        t.interrupt();
        t.join();

        String logs = logger.getLogs().toString();
        assertTrue(logs, logs.contains("Interrupt while post properties"));
        assertNotNull(ex[0]);
        assertEquals("Interrupt while post properties", ex[0].getMessage());
    }

    @Test
    public void testPostPropertiesInterrupt2() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        final BlazeMeterUtilsSlowEmul emul = new BlazeMeterUtilsSlowEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseGetSessions());

        final Throwable ex[] = {null};
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    Session session = new Session(emul, "id", "sesssion_name", "usetId", "test_id", "signature");
                    List<Session> sessions = new ArrayList<>();
                    sessions.add(session);
                    Master master = new Master(emul, "id", "name");
                    master.postProperties("1=2", sessions);
                } catch (Exception e) {
                    ex[0] = e;
                }
            }
        };
        t.start();
        t.interrupt();
        t.join();

        String logs = logger.getLogs().toString();
        assertTrue(logs, logs.contains("Interrupt while post properties to session"));
        assertNotNull(ex[0]);
        assertEquals("Interrupt while post properties to session", ex[0].getMessage());
    }
}