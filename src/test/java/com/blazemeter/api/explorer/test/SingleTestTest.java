/**
 * Copyright 2017 BlazeMeter Inc.
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

import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_ADDRESS;
import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_DATA_ADDRESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SingleTestTest {

    @Test
    public void testStart() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseStartSingleTest());

        SingleTest test = new SingleTest(emul, "testId", "testName", "http");
        Master master = test.start();

        assertEquals(1, emul.getRequests().size());
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/tests/testId/start, tag=null}", emul.getRequests().get(0));
        checkTest(test);
        String logs = logger.getLogs().toString();
        assertEquals(logs, 212, logs.length());
        assertTrue(logs, logs.contains("Start single test id=testId"));
        assertEquals("responseMasterId", master.getId());
        assertEquals("http", test.getTestType());
    }

    @Test
    public void testStartWithProperties() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseStartSingleTest());

        SingleTest test = new SingleTest(emul, "testId", "testName", "http");
        Master master = test.startWithProperties("1");

        assertEquals(1, emul.getRequests().size());
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/tests/testId/start, tag=null}", emul.getRequests().get(0));
        checkTest(test);
        String logs = logger.getLogs().toString();
        assertEquals(logs, 212, logs.length());
        assertTrue(logs, logs.contains("Start single test id=testId"));
        assertEquals("responseMasterId", master.getId());
        assertEquals("http", test.getTestType());
    }

    @Test
    public void testStartExternal() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseStartSingleTest());

        SingleTest test = new SingleTest(emul, "testId", "testName", "http");
        Master master = test.startExternal();

        assertEquals(1, emul.getRequests().size());
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/tests/testId/start-external, tag=null}", emul.getRequests().get(0));
        checkTest(test);
        String logs = logger.getLogs().toString();
        assertEquals(logs, 230, logs.length());
        assertTrue(logs, logs.contains("Start external single test id=testId"));
        assertEquals("responseMasterId", master.getId());
        assertEquals("http", test.getTestType());
    }

    public static String generateResponseStartSingleTest() {
        JSONObject masterResponse = new JSONObject();
        masterResponse.put("id", "responseMasterId");
        masterResponse.put("name", "responseMasterName");

        JSONObject response = new JSONObject();
        response.put("result", masterResponse);
        return response.toString();
    }

    private void checkTest(SingleTest test) {
        Master master = test.getMaster();
        assertEquals("responseMasterId", master.getId());
        assertEquals("responseMasterName", master.getName());
        assertEquals(Session.UNDEFINED, test.getSignature());
    }

    @Test
    public void testFromJSON() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject configuration = new JSONObject();
        configuration.put("type", "http");

        JSONObject object = new JSONObject();
        object.put("id", "testId");
        object.put("name", "testName");
        object.put("configuration", configuration);

        SingleTest test = SingleTest.fromJSON(emul, object);
        assertEquals("testId", test.getId());
        assertEquals("testName", test.getName());
        assertEquals("http", test.getTestType());
    }

    @Test
    public void testGetSingleTest() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        emul.addEmul(generateResponseGetSingleTest());

        SingleTest test = SingleTest.getSingleTest(emul, "testId");
        assertEquals("testId", test.getId());
        assertEquals("Single_testName", test.getName());
        assertEquals("http", test.getTestType());

        assertEquals(1, emul.getRequests().size());
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/tests/testId, tag=null}", emul.getRequests().get(0));

        String logs = logger.getLogs().toString();
        assertEquals(logs, 222, logs.length());
        assertTrue(logs, logs.contains("Get Single Test id=testId"));
    }

    public static String generateResponseGetSingleTest() {
        JSONObject configuration = new JSONObject();
        configuration.put("type", "http");

        JSONObject result = new JSONObject();
        result.put("id", "testId");
        result.put("name", "Single_testName");
        result.put("configuration", configuration);

        JSONObject response = new JSONObject();
        response.put("result", result);
        return response.toString();
    }
}