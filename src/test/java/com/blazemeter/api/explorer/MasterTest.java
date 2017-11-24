package com.blazemeter.api.explorer;

import com.blazemeter.api.logging.LoggerTest;
import com.blazemeter.api.logging.UserNotifier;
import com.blazemeter.api.logging.UserNotifierTest;
import com.blazemeter.api.utils.BlazeMeterUtilsEmul;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Test;

import java.util.List;

import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_ADDRESS;
import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_DATA_ADDRESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
        assertEquals("Get JUnit report for master id=id\r\n" +
                        "Simulating request: Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/id/reports/thresholds?format=junit, tag=null}\r\n" +
                        "Response: junit\r\n",
                logger.getLogs().toString());
    }

    @Test
    public void testGetCIStatus() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject status = new JSONObject();
        status.put("masterId", "id");

        JSONObject result = new JSONObject();
        result.put("result", status);
        emul.addEmul(result.toString());

        Master master = new Master(emul, "id", "name");

        JSONObject ciStatus = master.getCIStatus();
        assertTrue(ciStatus.has("masterId"));
        assertEquals("id", ciStatus.getString("masterId"));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/id/ci-status, tag=null}", emul.getRequests().get(0));
        assertEquals("Get CI status for master id=id\r\n" +
                        "Simulating request: Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/id/ci-status, tag=null}\r\n" +
                        "Response: {\"result\":{\"masterId\":\"id\"}}\r\n",
                logger.getLogs().toString());
    }

    @Test
    public void testGetPublicToken() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        String token = "x1x1x1x1x1x1x1x11x1x1x1";
        JSONObject publicToken = new JSONObject();
        publicToken.put("publicToken", token);

        JSONObject result = new JSONObject();
        result.put("result", publicToken);
        emul.addEmul(result.toString());

        Master master = new Master(emul, "id", "name");

        String expectedUrl = emul.getAddress() + String.format("/app/?public-token=%s#/masters/%s/summary", token, master.getId());
        assertEquals(expectedUrl, master.getPublicReport());
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/masters/id/public-token, tag=null}", emul.getRequests().get(0));
        assertEquals("Get link to public report for master id=id\r\n" +
                        "Simulating request: Request{method=POST, url=http://a.blazemeter.com/api/v4/masters/id/public-token, tag=null}\r\n" +
                        "Response: {\"result\":{\"publicToken\":\"x1x1x1x1x1x1x1x11x1x1x1\"}}\r\n",
                logger.getLogs().toString());
    }

    @Test
    public void testGetSessions() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject session = new JSONObject();
        session.put("id", "r-v3-1234567890qwerty");
        session.put("name", "11");
        session.put("userId", "12");
        session.put("testId", "13");

        JSONArray sessionsArray = new JSONArray();
        sessionsArray.add(session);

        JSONObject sessions = new JSONObject();
        sessions.put("sessions", sessionsArray);

        JSONObject result = new JSONObject();
        result.put("result", sessions);
        emul.addEmul(result.toString());

        Master master = new Master(emul, "id", "name");

        List<Session> sessionsList = master.getSessions();
        assertEquals(1, sessionsList.size());
        assertEquals("11", sessionsList.get(0).getName());
        assertEquals("12", sessionsList.get(0).getUserId());
        assertEquals("13", sessionsList.get(0).getTestId());
        assertEquals("r-v3-1234567890qwerty", sessionsList.get(0).getId());
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/id/sessions, tag=null}", emul.getRequests().get(0));
        assertEquals(254, logger.getLogs().toString().length());
    }

    @Test
    public void testStop() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject stopObject = new JSONObject();
        stopObject.put("session_id", "r-v3-1234567890qwerty");
        stopObject.put("result", "shutdown command sent\n");

        JSONArray stopArray = new JSONArray();
        stopArray.add(stopObject);

        JSONObject result = new JSONObject();
        result.put("result", stopArray);
        emul.addEmul(result.toString());

        Master master = new Master(emul, "id", "name");

        JSONArray stop = master.stop();
        assertEquals(1, stop.size());

        JSONObject sr = stop.getJSONObject(0);
        assertTrue(sr.has("session_id"));
        assertTrue(sr.has("result"));
        assertEquals("r-v3-1234567890qwerty", sr.get("session_id"));
        assertEquals("shutdown command sent\n", sr.get("result"));
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/masters/id/stop, tag=null}", emul.getRequests().get(0));
        assertEquals("Stop master id=id\r\n" +
                        "Simulating request: Request{method=POST, url=http://a.blazemeter.com/api/v4/masters/id/stop, tag=null}\r\n" +
                        "Response: {\"result\":[{\"session_id\":\"r-v3-1234567890qwerty\",\"result\":\"shutdown command sent\\n\"}]}\r\n",
                logger.getLogs().toString());
    }

    @Test
    public void testTerminate() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject terminateObject = new JSONObject();
        terminateObject.put("session_id", "r-v3-1234567899qwerty");
        terminateObject.put("result", true);

        JSONArray terminateArray = new JSONArray();
        terminateArray.add(terminateObject);

        JSONObject result = new JSONObject();
        result.put("result", terminateArray);
        emul.addEmul(result.toString());
        Master master = new Master(emul, "id", "name");

        JSONArray terminateResponse = master.terminate();
        assertEquals(1, terminateResponse.size());

        JSONObject terminate = terminateResponse.getJSONObject(0);
        assertTrue(terminate.has("session_id"));
        assertTrue(terminate.has("result"));
        assertEquals("r-v3-1234567899qwerty", terminate.get("session_id"));
        assertTrue(terminate.getBoolean("result"));
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/masters/id/terminate, tag=null}", emul.getRequests().get(0));
        assertEquals("Terminate master id=id\r\n" +
                        "Simulating request: Request{method=POST, url=http://a.blazemeter.com/api/v4/masters/id/terminate, tag=null}\r\n" +
                        "Response: {\"result\":[{\"session_id\":\"r-v3-1234567899qwerty\",\"result\":true}]}\r\n",
                logger.getLogs().toString());
    }

    @Test
    public void testGetStatus() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject result = new JSONObject();
        result.put("progress", 100);

        JSONObject response = new JSONObject();
        response.put("result", result);
        emul.addEmul(response.toString());

        Master master = new Master(emul, "id", "name");
        assertEquals(100, master.getStatus());
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/id/status?events=false, tag=null}", emul.getRequests().get(0));
        assertEquals("Get master status id=id\r\n" +
                        "Simulating request: Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/id/status?events=false, tag=null}\r\n" +
                        "Response: {\"result\":{\"progress\":100}}\r\n",
                logger.getLogs().toString());
    }

    @Test
    public void testGetSummary() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

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
        emul.addEmul(response.toString());

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
        assertEquals("Get summary for master id=id\r\n" +
                        "Simulating request: Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/id/reports/main/summary, tag=null}\r\n" +
                        "Response: {\"result\":{\"summary\":[{\"first\":1437397105,\"last\":1437397406,\"min\":0,\"max\":177,\"tp90\":2,\"failed\":1236,\"hits\":2482,\"avg\":1.47}]}}\r\n",
                logger.getLogs().toString());
    }

    @Test
    public void testGetFunctionalReport() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

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
        emul.addEmul(response.toString());

        Master master = new Master(emul, "id", "name");

        JSONObject funcReport = master.getFunctionalReport();
        assertEquals(8, funcReport.size());
        emul.clean();
        logger.reset();

        result.remove("functionalSummary");
        response.put("result", result);
        emul.addEmul(response.toString());
        master = new Master(emul, "id", "name");

        funcReport = master.getFunctionalReport();
        assertEquals(0, funcReport.size());
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/id, tag=null}", emul.getRequests().get(0));
        assertEquals("Get functional report for master id=id\r\n" +
                        "Simulating request: Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/id, tag=null}\r\n" +
                        "Response: {\"result\":{}}\r\n",
                logger.getLogs().toString());
    }

    @Test
    public void testPostNote() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject result = new JSONObject();
        result.put("note", "valid");

        JSONObject response = new JSONObject();
        response.put("result", result);
        emul.addEmul(response.toString());

        Master master = new Master(emul, "id", "name");

        String note = master.postNotes("valid");
        assertEquals(note, "valid");
        assertEquals("Request{method=PATCH, url=http://a.blazemeter.com/api/v4/masters/id, tag=null}", emul.getRequests().get(0));
        assertEquals("Post notes to master id=id\r\n" +
                        "Simulating request: Request{method=PATCH, url=http://a.blazemeter.com/api/v4/masters/id, tag=null}\r\n" +
                        "Response: {\"result\":{\"note\":\"valid\"}}\r\n",
                logger.getLogs().toString());
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
    public void postProperties() {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);


        JSONObject session = new JSONObject();
        session.put("id", "r-v3-1234567890qwerty");
        session.put("name", "11");
        session.put("userId", "12");
        session.put("testId", "13");

        JSONArray sessionsa = new JSONArray();
        sessionsa.add(session);

        JSONObject sessions = new JSONObject();
        sessions.put("sessions", sessionsa);

        JSONObject result = new JSONObject();
        result.put("result", sessions);
        emul.addEmul(result.toString());


        JSONArray properties = new JSONArray();
        JSONObject property = new JSONObject();
        property.put("1", "2");
        properties.add(property);

        Master master = new Master(emul, "id", "name");
        master.postProperties("1=2");
        assertEquals(2, emul.getRequests().size());
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/id/sessions, tag=null}", emul.getRequests().get(0));
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/sessions/r-v3-1234567890qwerty/properties?target=all, tag=null}", emul.getRequests().get(1));
        assertEquals(511, logger.getLogs().toString().length());
    }

    @Test
    public void convertProperties() {
        try {
            JSONArray array = Master.convertProperties("1=2,3=4");
            assertEquals(2, array.size());
            assertEquals(2, array.getJSONObject(0).size());
            assertEquals("1", array.getJSONObject(0).getString("key"));
            assertEquals("2", array.getJSONObject(0).getString("value"));
            assertEquals(2, array.getJSONObject(1).size());
            assertEquals("3", array.getJSONObject(1).getString("key"));
            assertEquals("4", array.getJSONObject(1).getString("value"));
        } catch (Exception e) {
            fail("Failed to convert properties to JSONArray");
        }
    }
}
