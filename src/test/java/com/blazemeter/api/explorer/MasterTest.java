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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MasterTest {

    @org.junit.Test
    public void junitReport() throws Exception {
        Logger logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();

        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        String result = "junit";
        emul.addEmul(result);
        Master master = new Master(emul,"id","name");
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
        status.put("masterId","id");
        result.put("result",status);
        emul.addEmul(result.toString());
        Master master = new Master(emul,"id","name");
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
        pt.put("publicToken",t);
        result.put("result",pt);
        emul.addEmul(result.toString());
        Master master = new Master(emul,"id","name");
        String pr = emul.getAddress()+ String.format("/app/?public-token=%s#/masters/%s/summary",t,master.getId());
        assertEquals(master.publicreport(),pr);
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
        s.put("id","r-v3-585114ca535ed");
        ids.add(s);
        sessions.put("sessions",ids);
        result.put("result",sessions);
        emul.addEmul(result.toString());
        Master master = new Master(emul,"id","name");
        List<String> sl = master.sessionIds();
        assertTrue(sl.size()==1);
        assertEquals(sl.get(0),"r-v3-585114ca535ed");
        emul.clean();

    }
}
