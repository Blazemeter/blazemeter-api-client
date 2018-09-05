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

package com.blazemeter.api.explorer.test;

import com.blazemeter.api.explorer.Master;
import com.blazemeter.api.explorer.Session;
import com.blazemeter.api.logging.LoggerTest;
import com.blazemeter.api.logging.UserNotifier;
import com.blazemeter.api.logging.UserNotifierTest;
import com.blazemeter.api.utils.BlazeMeterUtilsEmul;
import net.sf.json.JSONObject;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_ADDRESS;
import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_DATA_ADDRESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MultiTestTest {

    @Test
    public void testStart() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject response = new JSONObject();
        response.put("result", generateResponseStartMultiTest());

        emul.addEmul(response.toString());

        MultiTest test = new MultiTest(emul, "testId", "testName", "multi");
        Master master = test.start();

        assertEquals(1, emul.getRequests().size());
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/multi-tests/testId/start, tag=null}", emul.getRequests().get(0));
        checkTest(test);
        String logs = logger.getLogs().toString();
        assertEquals(logs, 217, logs.length());
        assertTrue(logs, logs.contains("Start multi test id=testId"));
        assertEquals("responseMasterId", master.getId());
        assertEquals("multi", test.getTestType());
    }

    @Test
    public void testStartWithProperties() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject response = new JSONObject();
        response.put("result", generateResponseStartMultiTest());

        emul.addEmul(response.toString());

        MultiTest test = new MultiTest(emul, "testId", "testName", "multi") {
            @Override
            protected JSONObject sendStartTestWithBody(String uri, String body) throws IOException {
                assertEquals(body, "{\"data\":{\"configuration\":{\"plugins\":{\"remoteControl\":[{\"key\":\"command_property\",\"value\":\"fsdfsd\"},{\"key\":\"command_property2\",\"value\":\"fsdfsd22\"}]},\"enableJMeterProperties\":true}}}");
                return super.sendStartTestWithBody(uri, body);
            }
        };
        Master master = test.startWithProperties("command_property=fsdfsd,command_property2=fsdfsd22");

        assertEquals(1, emul.getRequests().size());
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/multi-tests/testId/start, tag=null}", emul.getRequests().get(0));
        checkTest(test);
        String logs = logger.getLogs().toString();
        assertEquals(logs, 217, logs.length());
        assertTrue(logs, logs.contains("Start multi test id=testId"));
        assertEquals("responseMasterId", master.getId());
        assertEquals("multi", test.getTestType());
        assertTrue(emul.getRequestsBody().get(0).contains("size=179"));
    }

    @Test
    public void testStartExternal() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        MultiTest test = new MultiTest(emul, "testId", "testName", "multi");
        try {
            test.startExternal();
            fail("Cannot start external this test type");
        } catch (UnsupportedOperationException ex) {
            assertEquals("Start external is not supported for multi test type id=testId", ex.getMessage());
            assertEquals("Start external is not supported for multi test type id=testId\r\n", logger.getLogs().toString());
        }
    }

    @Test
    public void testUpdate() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        MultiTest test = new MultiTest(emul, "testId", "testName", "multi");
        try {
            test.update("");
            fail("Cannot update this test type");
        } catch (UnsupportedOperationException ex) {
            assertEquals("Update is not supported for multi test type id=testId", ex.getMessage());
            assertEquals("Update is not supported for multi test type id=testId\r\n", logger.getLogs().toString());
        }
    }

    @Test
    public void testUploadFile() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        MultiTest test = new MultiTest(emul, "testId", "testName", "multi");
        try {
            test.uploadFile(new File("."));
            fail("Cannot upload file to this test type");
        } catch (UnsupportedOperationException ex) {
            assertEquals("Upload file is not supported for multi test type id=testId", ex.getMessage());
            assertEquals("Upload file is not supported for multi test type id=testId\r\n", logger.getLogs().toString());
        }
    }

    @Test
    public void testValidate() throws IOException {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        MultiTest test = new MultiTest(emul, "testId", "testName", "multi");
        try {
            test.validate(".");
            fail("Cannot validate this test type");
        } catch (UnsupportedOperationException ex) {
            assertEquals("Validate is not supported for multi test type id=testId", ex.getMessage());
            assertEquals("Validate is not supported for multi test type id=testId\r\n", logger.getLogs().toString());
        }
    }

    @Test
    public void testValidations() throws IOException {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        MultiTest test = new MultiTest(emul, "testId", "testName", "multi");
        try {
            test.validations();
            fail("Cannot get validations this test type");
        } catch (UnsupportedOperationException ex) {
            assertEquals("Validations is not supported for multi test type id=testId", ex.getMessage());
            assertEquals("Validations is not supported for multi test type id=testId\r\n", logger.getLogs().toString());
        }
    }

    public static String generateResponseStartMultiTest() {
        JSONObject masterResponse = new JSONObject();
        masterResponse.put("id", "responseMasterId");
        masterResponse.put("name", "responseMasterName");
        return masterResponse.toString();
    }


    private void checkTest(MultiTest test) {
        Master master = test.getMaster();
        assertEquals("responseMasterId", master.getId());
        assertEquals("responseMasterName", master.getName());
        assertEquals(Session.UNDEFINED, test.getSignature());
    }

    @Test
    public void testGetMultiTest() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        emul.addEmul(generateResponseGetMultiTest());

        MultiTest test = MultiTest.getMultiTest(emul, "testId");
        assertEquals("testId", test.getId());
        assertEquals("Multi_testName", test.getName());
        assertEquals("multi", test.getTestType());

        assertEquals(1, emul.getRequests().size());
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/multi-tests/testId, tag=null}", emul.getRequests().get(0));

        String logs = logger.getLogs().toString();
        assertEquals(logs, 219, logs.length());
        assertTrue(logs, logs.contains("Get Multi Test id=testId"));
    }

    public static String generateResponseGetMultiTest() {
        return generateResponseGetMultiTest("multi");
    }

    public static String generateResponseGetMultiTest(String testType) {
        JSONObject result = new JSONObject();
        result.put("id", "testId");
        result.put("name", "Multi_testName");
        result.put("collectionType", testType);

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
        object.put("id", "testId");
        object.put("name", "testName");
        object.put("collectionType", "multi");

        MultiTest test = MultiTest.fromJSON(emul, object);
        assertEquals("testId", test.getId());
        assertEquals("testName", test.getName());
        assertEquals("multi", test.getTestType());
    }
}