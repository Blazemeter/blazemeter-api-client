package com.blazemeter.api.explorer;

import com.blazemeter.api.logging.LoggerTest;
import com.blazemeter.api.logging.UserNotifier;
import com.blazemeter.api.logging.UserNotifierTest;
import com.blazemeter.api.utils.BlazeMeterUtilsEmul;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.List;

import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_ADDRESS;
import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_DATA_ADDRESS;
import static org.junit.Assert.*;

public class MasterTest {

    public static final double DELTA = 0.001;

    @org.junit.Test
    public void testGetJUnitReport() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        String result = "junit";
        emul.addEmul(result);
        Master master = new Master(emul, "id", "name");

        assertEquals("junit", master.getJUnitReport());
    }

    @org.junit.Test
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
    }

    @org.junit.Test
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
    }

    @org.junit.Test
    public void testGetSessions() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();

        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject sessionId = new JSONObject();
        sessionId.put("id", "r-v3-1234567890qwerty");

        JSONArray ids = new JSONArray();
        ids.add(sessionId);

        JSONObject sessions = new JSONObject();
        sessions.put("sessions", ids);

        JSONObject result = new JSONObject();
        result.put("result", sessions);
        emul.addEmul(result.toString());

        Master master = new Master(emul, "id", "name");

        List<String> sessionsList = master.getSessions();
        assertEquals(1, sessionsList.size());
        assertEquals("r-v3-1234567890qwerty", sessionsList.get(0));
    }

    @org.junit.Test
    public void testStop() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();

        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        JSONObject result = new JSONObject();

        JSONObject stopObject = new JSONObject();
        stopObject.put("session_id", "r-v3-1234567890qwerty");
        stopObject.put("result", "shutdown command sent\n");

        JSONArray stopArray = new JSONArray();
        stopArray.add(stopObject);
        result.put("result", stopArray);
        emul.addEmul(result.toString());

        Master master = new Master(emul, "id", "name");

        JSONArray stop = master.stop();
        assertTrue(stop.size() == 1);

        JSONObject sr = stop.getJSONObject(0);
        assertTrue(sr.has("session_id"));
        assertTrue(sr.has("result"));
        assertEquals("r-v3-1234567890qwerty", sr.get("session_id"));
        assertEquals("shutdown command sent\n", sr.get("result"));
    }

    @org.junit.Test
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
    }

    @org.junit.Test
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
        emul.clean();
    }

    @org.junit.Test
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
    }

    @org.junit.Test
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

        result.remove("functionalSummary");
        response.put("result", result);
        emul.addEmul(response.toString());
        master = new Master(emul, "id", "name");

        funcReport = master.getFunctionalReport();
        assertEquals(0, funcReport.size());
    }

    @org.junit.Test
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
    }

}
