package com.blazemeter.api.com.blazemeter.ciworkflow;

import com.blazemeter.api.explorer.Master;
import com.blazemeter.api.explorer.test.AbstractTest;
import com.blazemeter.api.explorer.test.SingleTest;
import com.blazemeter.api.logging.LoggerTest;
import com.blazemeter.api.logging.UserNotifier;
import com.blazemeter.api.logging.UserNotifierTest;
import com.blazemeter.api.utils.BlazeMeterUtilsEmul;
import com.blazemeter.ciworkflow.CiBuild;
import net.sf.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;

import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_ADDRESS;
import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_DATA_ADDRESS;
import static org.junit.Assert.assertEquals;

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
        assertEquals(440, logger.getLogs().length());
    }

    @Ignore
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
        masterResponse.put("signature", "responseSignature");

        JSONObject result = new JSONObject();
        result.put("master", masterResponse);
        JSONObject response = new JSONObject();
        response.put("result", masterResponse);


        emul.addEmul(response.toString());
        AbstractTest test = new SingleTest(emul, "id", "name", "http");
        CiBuild ciBuild = new CiBuild(test, "", "",
                false, false,
                "", "", "", logger);
        ciBuild.execute();
        assertEquals(2, emul.getRequests().size());
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/id/status?events=false, tag=null}", emul.getRequests().get(0));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/id/status?events=false, tag=null}", emul.getRequests().get(1));
        assertEquals(440, logger.getLogs().length());
    }

}
