package com.blazemeter.api.explorer;

import com.blazemeter.api.logging.Logger;
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

    @org.junit.Test
    public void junitReport() throws Exception {
        Logger logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();

        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        String result = "junit";
        emul.addEmul(result);
        Master master = new Master(emul, "id", "name");
        assertEquals("junit", master.junitReport());
        emul.clean();


    }

    @org.junit.Test
    public void cistatus() throws Exception {
        Logger logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();

        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        JSONObject result = new JSONObject();
        JSONObject status = new JSONObject();
        status.put("masterId", "id");
        result.put("result", status);
        emul.addEmul(result.toString());
        Master master = new Master(emul, "id", "name");
        JSONObject cs = master.cistatus();
        assertTrue(cs.has("masterId"));
        assertTrue(cs.getString("masterId").equals("id"));
        emul.clean();

    }

    @org.junit.Test
    public void publictoken() throws Exception {
        Logger logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();

        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        JSONObject result = new JSONObject();
        JSONObject pt = new JSONObject();
        String t = "Mp4jkdOaFomK0jAFgrKOYGCx8BfLBjlG1fpPiVPidZibMgg5Ob";
        pt.put("publicToken", t);
        result.put("result", pt);
        emul.addEmul(result.toString());
        Master master = new Master(emul, "id", "name");
        String pr = emul.getAddress() + String.format("/app/?public-token=%s#/masters/%s/summary", t, master.getId());
        assertEquals(master.publicreport(), pr);
        emul.clean();

    }

    @org.junit.Test
    public void sessionIds() throws Exception {
        Logger logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();

        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        JSONObject result = new JSONObject();
        JSONObject sessions = new JSONObject();
        JSONArray ids = new JSONArray();
        JSONObject s = new JSONObject();
        s.put("id", "r-v3-585114ca535ed");
        ids.add(s);
        sessions.put("sessions", ids);
        result.put("result", sessions);
        emul.addEmul(result.toString());
        Master master = new Master(emul, "id", "name");
        List<String> sl = master.sessionIds();
        assertTrue(sl.size() == 1);
        assertEquals(sl.get(0), "r-v3-585114ca535ed");
        emul.clean();

    }

    @org.junit.Test
    public void stop() throws Exception {
        Logger logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();

        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        JSONObject result = new JSONObject();

        JSONArray stoparray = new JSONArray();
        JSONObject stopobject = new JSONObject();
        stopobject.put("session_id", "r-v3-559f984a467e3");
        stopobject.put("result", "shutdown command sent\n");
        stoparray.add(stopobject);
        result.put("result", stoparray);
        emul.addEmul(result.toString());
        Master master = new Master(emul, "id", "name");
        JSONArray stop = master.stop();
        assertTrue(stop.size() == 1);
        JSONObject sr = stop.getJSONObject(0);
        assertTrue(sr.has("session_id"));
        assertTrue(sr.has("result"));
        assertTrue(sr.get("session_id").equals("r-v3-559f984a467e3"));
        assertTrue(sr.get("result").equals("shutdown command sent\n"));
        emul.clean();

    }

    @org.junit.Test
    public void terminate() throws Exception {
        Logger logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();

        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        JSONObject result = new JSONObject();
        JSONArray terminateArray = new JSONArray();
        JSONObject terminateobject = new JSONObject();
        terminateobject.put("session_id", "r-v3-559f97d178870");
        terminateobject.put("result", true);
        terminateArray.add(terminateobject);
        result.put("result", terminateArray);
        emul.addEmul(result.toString());
        Master master = new Master(emul, "id", "name");
        JSONArray terminate = master.terminate();
        assertTrue(terminate.size() == 1);

        JSONObject tr = terminate.getJSONObject(0);
        assertTrue(tr.has("session_id"));
        assertTrue(tr.has("result"));
        assertTrue(tr.get("session_id").equals("r-v3-559f97d178870"));
        assertTrue(tr.getBoolean("result") == true);
        emul.clean();


    }

    @org.junit.Test
    public void status() throws Exception {
        Logger logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();

        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        JSONObject jo = new JSONObject();
        JSONObject result = new JSONObject();
        result.put("progress", 100);
        jo.put("result", result);

        emul.addEmul(jo.toString());
        Master master = new Master(emul, "id", "name");
        int status = master.status();
        assertTrue(status == 100);
        emul.clean();


    }

    @org.junit.Test
    public void summary() throws Exception {
        Logger logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();

        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        JSONObject jo = new JSONObject();
        JSONObject result = new JSONObject();
        JSONObject summaryemul = new JSONObject();
        summaryemul.put("first", 1437397105);
        summaryemul.put("last", 1437397406);
        summaryemul.put("min", 0);
        summaryemul.put("max", 177);
        summaryemul.put("tp90", 2);
        summaryemul.put("tp90", 2);
        summaryemul.put("failed", 1236);
        summaryemul.put("hits", 2482);
        summaryemul.put("avg", 1.47);
        JSONArray sumar = new JSONArray();
        sumar.add(summaryemul);
        result.put("summary", sumar);
        jo.put("result", result);
        emul.addEmul(jo.toString());
        Master master = new Master(emul, "id", "name");
        JSONObject summary = master.summary();
        assertTrue(summary.size() == 7);
        assertTrue(summary.getDouble("avg") == 1.47);
        assertTrue(summary.getInt("min") == 0);
        assertTrue(summary.getInt("max") == 177);
        assertTrue(summary.getInt("tp90") == 2);
        assertTrue(summary.getInt("errorPercentage") == 49);
        assertTrue(summary.getInt("hits") == 2482);
        assertTrue(summary.getDouble("avgthrpt") == 8.25);
        emul.clean();
    }

    @org.junit.Test
    public void funcReport() throws Exception {
        Logger logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();

        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        JSONObject jo = new JSONObject();
        JSONObject result = new JSONObject();
        JSONObject funcreportemul = new JSONObject();
        funcreportemul.put("testsCount", 1);
        funcreportemul.put("requestsCount", 1);
        funcreportemul.put("errorsCount", 1);
        funcreportemul.put("assertions", new JSONObject());
        funcreportemul.put("responseTime", new JSONObject());
        funcreportemul.put("isFailed", true);
        funcreportemul.put("failedCount", 1);
        funcreportemul.put("failedPercentage", 100);
        result.put("functionalSummary", funcreportemul);
        jo.put("result", result);
        emul.addEmul(jo.toString());
        Master master = new Master(emul, "id", "name");
        JSONObject funcReport = master.funcReport();
        assertTrue(funcReport.size()==8);
        emul.clean();

        result.remove("functionalSummary");
        jo.put("result", result);
        emul.addEmul(jo.toString());
        master = new Master(emul, "id", "name");
        funcReport = master.funcReport();
        assertTrue(funcReport.size()==0);
        emul.clean();

    }

    @org.junit.Test
    public void note() throws Exception {
        Logger logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();

        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        JSONObject jo = new JSONObject();
        JSONObject result = new JSONObject();
        result.put("note","valid");
        jo.put("result",result);
        emul.addEmul(jo.toString());
        Master master = new Master(emul, "id", "name");
        String note = master.note("valid");
        assertEquals(note,"valid");
        emul.clean();

    }

}
