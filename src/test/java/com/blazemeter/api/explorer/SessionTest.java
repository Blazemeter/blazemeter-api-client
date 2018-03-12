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
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Test;

import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_ADDRESS;
import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_DATA_ADDRESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SessionTest {

    @Test
    public void testPostProperties() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponsePostProperties());

        Session session = new Session(emul, "id", "name", "userId", "testId", "sign");
        session.postProperties("key=url,value=google.com");
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/sessions/id/properties?target=all, tag=null}", emul.getRequests().get(0));
        String logs = logger.getLogs().toString();
        assertEquals(logs, 213, logs.length());
        assertTrue(logs, logs.contains("Post properties to session id=id"));
    }

    @Test
    public void testPostPropertiesEmpty() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponsePostProperties());

        Session session = new Session(emul, "id", "name", "userId", "testId", "sign");
        session.postProperties("");
        String logs = logger.getLogs().toString();
        assertEquals(0, emul.getRequests().size());
        assertEquals(logs, 53, logs.length());
        assertTrue(logs, logs.contains("Properties are empty, won't be sent to session = "));
    }

    public static String generateResponsePostProperties() {
        JSONObject property = new JSONObject();
        property.put("key", "url");
        property.put("value", "google.com");

        JSONObject result = new JSONObject();
        result.put("result", property);
        return result.toString();
    }

    @Test
    public void testGetJTLReport() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseGetJTLReport());

        Session session = new Session(emul, "id", "name", "userId", "testId", "sign");
        String url = session.getJTLReport();
        assertEquals("http://a.blazemeter.com/dataURL", url);
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/sessions/id/reports/logs, tag=null}", emul.getRequests().get(0));
        String logs = logger.getLogs().toString();
        assertEquals(logs, 244, logs.length());
        assertTrue(logs, logs.contains("Get JTL report for session id=id"));
    }

    public static String generateResponseGetJTLReportWithRelativeUrl() {
        JSONObject dataUrl = new JSONObject();
        dataUrl.put("dataUrl", "/api/veeeersion/sssss?file=sessions/sessionID/jtls_and_more.zip");
        dataUrl.put("filename", "1.zip");

        JSONArray data = new JSONArray();
        data.add(dataUrl);

        JSONObject result = new JSONObject();
        result.put("data", data);

        JSONObject response = new JSONObject();
        response.put("result", result);
        return response.toString();
    }

    public static String generateResponseGetJTLReport() {
        JSONObject dataUrl = new JSONObject();
        dataUrl.put("dataUrl", "http://a.blazemeter.com/dataURL");
        dataUrl.put("filename", "1.zip");

        JSONArray data = new JSONArray();
        data.add(dataUrl);

        JSONObject result = new JSONObject();
        result.put("data", data);

        JSONObject response = new JSONObject();
        response.put("result", result);
        return response.toString();
    }

    public static String generateResponseGetJTLReportNullZip() {
        JSONObject dataUrl = new JSONObject();
        dataUrl.put("dataUrl", "z");
        dataUrl.put("filename", "x");

        JSONArray data = new JSONArray();
        data.add(dataUrl);

        JSONObject result = new JSONObject();
        result.put("data", data);

        JSONObject response = new JSONObject();
        response.put("result", result);
        return response.toString();
    }

    @Test
    public void testGetJTLReportReturnNull() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseGetJTLReturnNull());

        Session session = new Session(emul, "id", "name", "userId", "testId", "sign");
        String url = session.getJTLReport();
        assertNull(url);
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/sessions/id/reports/logs, tag=null}", emul.getRequests().get(0));
        String logs = logger.getLogs().toString();
        assertEquals(logs, 180, logs.length());
        assertTrue(logs, logs.contains("Get JTL report for session id=id"));
    }

    public static String generateResponseGetJTLReturnNull() {
        JSONObject result = new JSONObject();
        result.put("data", new JSONArray());

        JSONObject response = new JSONObject();
        response.put("result", result);
        return response.toString();
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
        String logs = logger.getLogs().toString();
        assertEquals(logs, 167, logs.length());
        assertTrue(logs, logs.contains("Terminate external session id=id"));
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
        String logs = logger.getLogs().toString();
        assertEquals(logs, 342, logs.length());
        assertTrue(logs, logs.contains("Send data to session id=sessionId"));
        assertTrue(logs, logs.contains("Sending active test data: {\"data\":\"Hello, World!\"}"));
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


    @Test
    public void testFromJSONNoSign() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject object = new JSONObject();
        object.put("id", "id");
        object.put("name", "name");
        object.put("userId", "userId");
        object.put("testId", "testId");


        Session session = Session.fromJSON(emul, object);
        assertEquals("id", session.getId());
        assertEquals("name", session.getName());
        assertEquals("userId", session.getUserId());
        assertEquals("testId", session.getTestId());
        assertEquals(Session.UNDEFINED, session.getSignature());
    }

    @Test
    public void convertProperties() {
        try {
            JSONArray array = Session.convertProperties("1=2,3=4");
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
