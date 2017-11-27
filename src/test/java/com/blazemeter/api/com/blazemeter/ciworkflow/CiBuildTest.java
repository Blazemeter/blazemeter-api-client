package com.blazemeter.api.com.blazemeter.ciworkflow;

import com.blazemeter.api.explorer.Master;
import com.blazemeter.api.explorer.test.AbstractTest;
import com.blazemeter.api.explorer.test.SingleTest;
import com.blazemeter.api.logging.LoggerTest;
import com.blazemeter.api.logging.UserNotifier;
import com.blazemeter.api.logging.UserNotifierTest;
import com.blazemeter.api.utils.BlazeMeterUtilsEmul;
import com.blazemeter.ciworkflow.CiBuild;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Test;

import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_ADDRESS;
import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_DATA_ADDRESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CiBuildTest {

    @Test
    public void testWaitForFinish() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        JSONObject result = new JSONObject();
        JSONObject response = new JSONObject();

        result.put("progress", 70);
        response.put("result", result);
        emul.addEmul(response.toString());

        result = new JSONObject();
        result.put("progress", 140);

        response.put("result", result);
        emul.addEmul(response.toString());

        AbstractTest test = new SingleTest(emul, "id", "name", "http");
        Master master = new Master(emul, "id", "name");
        CiBuild ciBuild = new CiBuild(test, "", "",
                false, false,
                "", "", "", logger);
        ciBuild.waitForFinish(master);
        assertEquals(2, emul.getRequests().size());
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/id/status?events=false, tag=null}", emul.getRequests().get(0));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/id/status?events=false, tag=null}", emul.getRequests().get(1));
        assertEquals(441, logger.getLogs().length());
    }

    @Test
    public void testExecute() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject testResponse = new JSONObject();
        testResponse.put("id", "responseTestId");
        testResponse.put("name", "responseTestName");

        JSONObject masterResponse = new JSONObject();
        masterResponse.put("id", "responseMasterId");
        masterResponse.put("name", "responseMasterName");
        JSONObject response = new JSONObject();
        response.put("result", masterResponse);


        emul.addEmul(response.toString());

        String token = "x1x1x1x1x1x1x1x11x1x1x1";
        JSONObject publicToken = new JSONObject();
        publicToken.put("publicToken", token);

        JSONObject result = new JSONObject();
        result.put("result", publicToken);
        emul.addEmul(result.toString());


        result = new JSONObject();
        result.put("note", "valid");

        response = new JSONObject();
        response.put("result", result);
        emul.addEmul(response.toString());

        JSONObject session = new JSONObject();
        session.put("id", "r-v3-1234567890qwerty");
        session.put("name", "11");
        session.put("userId", "12");
        session.put("testId", "13");

        JSONArray sessionsa = new JSONArray();
        sessionsa.add(session);

        JSONObject sessions = new JSONObject();
        sessions.put("sessions", sessionsa);

        result = new JSONObject();
        result.put("result", sessions);
        emul.addEmul(result.toString());


        result = new JSONObject();
        result.put("key", "URL");
        result.put("value", "www.google.com");
        response.put("result", result);
        emul.addEmul(response.toString());


        /*
        {
    "api_version": 4,
    "error": null,
    "result": {
        "key": "URL",
        "value": "www.google.com"
    }
}
         */


        result = new JSONObject();
        response = new JSONObject();

        result.put("progress", 70);
        response.put("result", result);
        emul.addEmul(response.toString());

        result = new JSONObject();
        result.put("progress", 140);

        response.put("result", result);
        emul.addEmul(response.toString());


        AbstractTest test = new SingleTest(emul, "id", "name", "http");
        CiBuild ciBuild = new CiBuild(test, "1=2", "",
                false, false,
                "", "", "", logger);
        ciBuild.execute();
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/tests/id/start, tag=null}", emul.getRequests().get(0));
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/masters/responseMasterId/public-token, tag=null}", emul.getRequests().get(1));
        assertEquals("http://a.blazemeter.com/app/?public-token=x1x1x1x1x1x1x1x11x1x1x1#/masters/responseMasterId/summary",
                ciBuild.pr);
        assertEquals("Request{method=PATCH, url=http://a.blazemeter.com/api/v4/masters/responseMasterId, tag=null}", emul.getRequests().get(2));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/responseMasterId/sessions, tag=null}", emul.getRequests().get(3));
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/sessions/r-v3-1234567890qwerty/properties?target=all, tag=null}", emul.getRequests().get(4));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/responseMasterId/status?events=false, tag=null}", emul.getRequests().get(5));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/responseMasterId/status?events=false, tag=null}", emul.getRequests().get(6));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/responseMasterId/ci-status, tag=null}", emul.getRequests().get(7));
        assertTrue(logger.getLogs().toString().contains("Simulating request: Request{method=PATCH, url=http://a.blazemeter.com/api/v4/masters/responseMasterId, tag=null}"));
        assertEquals(9, emul.getRequests().size());
        //        assertEquals(440, logger.getLogs().length());
    }

}
