package com.blazemeter.ciworkflow;

import com.blazemeter.api.explorer.Master;
import com.blazemeter.api.explorer.MasterTest;
import com.blazemeter.api.explorer.SessionTest;
import com.blazemeter.api.explorer.test.AbstractTest;
import com.blazemeter.api.explorer.test.SingleTest;
import com.blazemeter.api.explorer.test.SingleTestTest;
import com.blazemeter.api.logging.LoggerTest;
import com.blazemeter.api.logging.UserNotifier;
import com.blazemeter.api.logging.UserNotifierTest;
import com.blazemeter.api.utils.BlazeMeterUtilsEmul;
import net.sf.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_ADDRESS;
import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_DATA_ADDRESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
        UserNotifier notifier = new UserNotifierTest();
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

        AbstractTest test = new SingleTest(emul, "id", "name", "http");
        Master master = new Master(emul, "id", "name");
        CiBuild ciBuild = new CiBuild(test, "", "",
                false, false,
                "", "", "");
        ciBuild.waitForFinish(master);
        assertEquals(2, emul.getRequests().size());
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/id/status?events=false, tag=null}", emul.getRequests().get(0));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/id/status?events=false, tag=null}", emul.getRequests().get(1));
        assertEquals(363, logger.getLogs().length());
    }

    @Test
    public void testExecute() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        setEmulator(emul);
        AbstractTest test = new SingleTest(emul, "id", "name", "http");
        CiBuild ciBuild = new CiBuild(test, "1=2", "",
                false, false,
                "", "", "");
        ciBuild.execute();
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/tests/id/start, tag=null}", emul.getRequests().get(0));
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/masters/responseMasterId/public-token, tag=null}", emul.getRequests().get(1));
        assertEquals("http://a.blazemeter.com/app/?public-token=x1x1x1x1x1x1x1x11x1x1x1#/masters/responseMasterId/summary",
                ciBuild.getPublicReport());
        assertEquals("Request{method=PATCH, url=http://a.blazemeter.com/api/v4/masters/responseMasterId, tag=null}", emul.getRequests().get(2));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/responseMasterId/sessions, tag=null}", emul.getRequests().get(3));
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/sessions/r-v3-1234567890qwerty/properties?target=all, tag=null}", emul.getRequests().get(4));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/responseMasterId/status?events=false, tag=null}", emul.getRequests().get(5));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/responseMasterId/status?events=false, tag=null}", emul.getRequests().get(6));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/responseMasterId/ci-status, tag=null}", emul.getRequests().get(7));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/responseMasterId, tag=null}", emul.getRequests().get(8));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/responseMasterId/reports/main/summary, tag=null}", emul.getRequests().get(9));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/responseMasterId, tag=null}", emul.getRequests().get(10));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/responseMasterId/reports/main/summary, tag=null}", emul.getRequests().get(11));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/responseMasterId, tag=null}", emul.getRequests().get(12));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/responseMasterId/reports/main/summary, tag=null}", emul.getRequests().get(13));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/responseMasterId, tag=null}", emul.getRequests().get(14));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/responseMasterId/reports/main/summary, tag=null}", emul.getRequests().get(15));
        String logs = logger.getLogs().toString();
        assertTrue(logs.contains("Start single test id=id"));
        assertTrue(logs.contains("Get link to public report for master id=responseMasterId"));
        assertTrue(logs.contains("Post notes to master id=responseMasterId"));
        assertTrue(logs.contains("Get list of sessions for master id=responseMasterId"));
        assertTrue(logs.contains("Post properties to session id=r-v3-1234567890qwerty"));
        assertTrue(logs.contains("Response: {\"result\":{\"progress\":70}}"));
        assertTrue(logs.contains("Response: {\"result\":{\"progress\":140}}"));
        assertEquals(16, emul.getRequests().size());
        assertEquals(logger.getLogs().toString(), 3226, logger.getLogs().length());
    }


    private void setEmulator(BlazeMeterUtilsEmul emul) {
        emul.addEmul(SingleTestTest.generateResponseStartSingleTest());

        String token = "x1x1x1x1x1x1x1x11x1x1x1";
        emul.addEmul(MasterTest.generateResponseGetPublicToken(token));
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
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        AbstractTest test = new SingleTest(emul, "id", "name", "http");
        CiBuild ciBuild = new CiBuild(test, "1=2", "",
                false, false,
                "", "", "");
        BuildResult result = ciBuild.execute();
        assertEquals(BuildResult.FAILED, result);
        assertEquals(1, emul.getRequests().size());
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/tests/id/start, tag=null}", emul.getRequests().get(0));
        String logs = logger.getLogs().toString();
        assertEquals(logs, 234, logs.length());
        assertTrue(logs, logs.contains("Caught exception. Set Build status [FAILED]. Reason is:"));
    }
}
