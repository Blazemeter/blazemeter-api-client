package com.blazemeter.api.explorer;

import com.blazemeter.api.logging.LoggerTest;
import com.blazemeter.api.logging.UserNotifier;
import com.blazemeter.api.logging.UserNotifierTest;
import com.blazemeter.api.utils.BlazeMeterUtilsEmul;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Test;

import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_ADDRESS;
import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_DATA_ADDRESS;
import static org.junit.Assert.*;

public class SessionTest {

    @Test
    public void testPostProperties() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject property = new JSONObject();
        property.put("key", "url");
        property.put("value", "google.com");

        JSONArray properties = new JSONArray();
        properties.add(property);

        JSONObject result = new JSONObject();
        result.put("result", property);
        emul.addEmul(result.toString());

        Session session = new Session(emul, "id", "name", "userId", "testId", "sign");
        session.postProperties(properties);
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/sessions/id/properties?target=all, tag=null}", emul.getRequests().get(0));
        assertEquals("Post properties to session id=id\r\n" +
                        "Simulating request: Request{method=POST, url=http://a.blazemeter.com/api/v4/sessions/id/properties?target=all, tag=null}\r\n" +
                        "Response: {\"result\":{\"key\":\"url\",\"value\":\"google.com\"}}\r\n",
                logger.getLogs().toString());
    }

    @Test
    public void testGetJTLReport() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject dataUrl = new JSONObject();
        dataUrl.put("dataUrl", "dataUrl");
        dataUrl.put("title", "Zip");

        JSONArray data = new JSONArray();
        data.add(dataUrl);

        JSONObject result = new JSONObject();
        result.put("data", data);

        JSONObject response = new JSONObject();
        response.put("result", result);
        emul.addEmul(response.toString());

        Session session = new Session(emul, "id", "name", "userId", "testId", "sign");
        String url = session.getJTLReport();
        assertEquals("dataUrl", url);
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/sessions/id/reports/logs, tag=null}", emul.getRequests().get(0));
        assertEquals("Get JTL report for session id=id\r\n" +
                        "Simulating request: Request{method=GET, url=http://a.blazemeter.com/api/v4/sessions/id/reports/logs, tag=null}\r\n" +
                        "Response: {\"result\":{\"data\":[{\"dataUrl\":\"dataUrl\",\"title\":\"Zip\"}]}}\r\n",
                logger.getLogs().toString());
    }

    @Test
    public void testTerminateExternal() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul("{}");
        Session session = new Session(emul, "id", "name", "userId", "testId", "sign");
        session.terminateExternal();
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/sessions/id/terminate-external, tag=null}", emul.getRequests().get(0));
        assertEquals("Terminate external session id=id\r\n" +
                        "Simulating request: Request{method=POST, url=http://a.blazemeter.com/api/v4/sessions/id/terminate-external, tag=null}\r\n" +
                        "Response: {}\r\n",
                logger.getLogs().toString());
    }

    @Test
    public void testSendData() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject data = new JSONObject();
        data.put("data", "Hello, World!");

        emul.addEmul("{\"result\":{\"session\":{\"statusCode\":15}}}");

        Session session = new Session(emul, "sessionId", "sessionName", "userId", "testId", "testSignature");
        session.sendData(data);

        assertEquals("Request{method=POST, url=http://data.blazemeter.com/submit.php?session_id=sessionId&signature=testSignature&test_id=testId&user_id=userId&pq=0&target=labels_bulk&update=1, tag=null}", emul.getRequests().get(0));
        assertEquals("Send data to session id=sessionId\r\n" +
                        "Sending active test data: {\"data\":\"Hello, World!\"}\r\n" +
                        "Simulating request: Request{method=POST, url=http://data.blazemeter.com/submit.php?session_id=sessionId&signature=testSignature&test_id=testId&user_id=userId&pq=0&target=labels_bulk&update=1, tag=null}\r\n" +
                        "Response: {\"result\":{\"session\":{\"statusCode\":15}}}\r\n",
                logger.getLogs().toString());
    }

    @Test
    public void testFromJSON() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();

        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        JSONObject object = new JSONObject();
        object.put("id", "sessionId");
        object.put("name", "sessionName");
        object.put("userId", "userId");
        Session session = Session.fromJSON(emul, "testId", "signature", object);
        assertEquals("sessionId", session.getId());
        assertEquals("sessionName", session.getName());
        assertEquals("userId", session.getUserId());
        assertEquals("testId", session.getTestId());
        assertEquals("signature", session.getSignature());

    }
}
