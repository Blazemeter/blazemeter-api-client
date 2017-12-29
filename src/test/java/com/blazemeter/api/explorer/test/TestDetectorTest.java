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

import com.blazemeter.api.exception.UnexpectedResponseException;
import com.blazemeter.api.logging.LoggerTest;
import com.blazemeter.api.logging.UserNotifier;
import com.blazemeter.api.logging.UserNotifierTest;
import com.blazemeter.api.utils.BlazeMeterUtilsEmul;
import net.sf.json.JSONObject;
import org.junit.Test;

import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_ADDRESS;
import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_DATA_ADDRESS;
import static org.junit.Assert.*;

public class TestDetectorTest {

    @Test
    public void testDetectTestReturnSingle() throws Exception {
        TestDetector detector = new TestDetector();
        assertNotNull(detector);
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(SingleTestTest.generateResponseGetSingleTest());

        AbstractTest abstractTest = TestDetector.detectTest(emul, "xxxx");

        assertTrue(abstractTest instanceof SingleTest);
        assertEquals("testId", abstractTest.getId());
        assertEquals("Single_testName", abstractTest.getName());
        assertEquals("http", abstractTest.getTestType());
        assertEquals(1, emul.getRequests().size());
        String logs = logger.getLogs().toString();
        assertEquals(logs, 267, logs.length());
        assertTrue(logs, logs.contains("Attempt to detect Single test type with id=xxx"));
    }

    @Test
    public void testDetectTestReturnMulti() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseTestNotFound());
        emul.addEmul(MultiTestTest.generateResponseGetMultiTest());

        AbstractTest abstractTest = TestDetector.detectTest(emul, "xxxx");

        assertTrue(abstractTest instanceof MultiTest);
        assertEquals("testId", abstractTest.getId());
        assertEquals("Multi_testName", abstractTest.getName());
        assertEquals("multi", abstractTest.getTestType());
        assertEquals(2, emul.getRequests().size());
        String logs = logger.getLogs().toString();
        assertEquals(logs, 614, logs.length());
        assertTrue(logs, logs.contains("Single test with id=xxxx not found"));
        assertTrue(logs, logs.contains("Attempt to detect Multi test type with id=xxx"));
    }

    @Test
    public void testDetectTestNotFound() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseTestNotFound());
        emul.addEmul(generateResponseTestNotFound());

        AbstractTest test = TestDetector.detectTest(emul, "xxxx");
        assertNull(test);
        assertEquals(2, emul.getRequests().size());
        String logs = logger.getLogs().toString();
        assertEquals(logs, 705, logs.length());
        assertTrue(logs, logs.contains("Multi test with id=xxxx not found"));
    }

    @Test
    public void testDetectTestFailedGetSingleTest() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseUnauthorized());

        try {
            TestDetector.detectTest(emul, "xxxx");
            fail();
        } catch (UnexpectedResponseException ex) {
            assertEquals(1, emul.getRequests().size());
            String logs = logger.getLogs().toString();
            assertEquals(logs, 458, logs.length());
            assertTrue(logs, logs.contains("Fail for detect Single test type id=xxxx. Reason is: Received response with the following error: Unauthorized"));
        }
    }

    public static String generateResponseTestNotFound() {
        JSONObject error = new JSONObject();
        error.put("code", 404);
        error.put("message", "Not Found: Test not found");

        JSONObject response = new JSONObject();
        response.put("error", error);
        response.put("result", null);
        return response.toString();
    }

    public static String generateResponseUnauthorized() {
        JSONObject error = new JSONObject();
        error.put("code", 404);
        error.put("message", "Unauthorized");

        JSONObject response = new JSONObject();
        response.put("error", error);
        response.put("result", null);
        return response.toString();
    }
}