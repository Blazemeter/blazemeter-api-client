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

import java.io.IOException;

import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_ADDRESS;
import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_DATA_ADDRESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
        emul.addEmul(generateResponseCollectionNotFound());

        AbstractTest test = TestDetector.detectTest(emul, "xxxx");
        assertNull(test);
        assertEquals(2, emul.getRequests().size());
        String logs = logger.getLogs().toString();
        assertEquals(logs, 717, logs.length());
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

    @Test
    public void detectTestBySyffix() {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        String testId = "123.abc";
        try {
            TestDetector.detectTestBySuffix(emul, "123", "abc");
        } catch (IOException e) {
            assertEquals("Test type = abc is unexpected", e.getMessage());
        }
        emul.addEmul(SingleTestTest.generateResponseGetSingleTest());
        emul.addEmul(SingleTestTest.generateResponseGetSingleTest());
        emul.addEmul(SingleTestTest.generateResponseGetSingleTest());
        emul.addEmul(SingleTestTest.generateResponseGetSingleTest());

        testId = "testId.http";
        try {
            AbstractTest test = TestDetector.detectTestBySuffix(emul, "testId", "http");
            assertEquals("testId", test.getId());
            assertEquals("http", test.getTestType());
            assertEquals("http", test.getTestType());
            assertEquals("Single_testName", test.getName());
        } catch (IOException e) {
            fail();
        }

        testId = "testId.jmeter";
        try {
            AbstractTest test = TestDetector.detectTestBySuffix(emul, "testId", "jmeter");
            assertEquals("testId", test.getId());
            assertEquals("http", test.getTestType());
            assertEquals("http", test.getTestType());
            assertEquals("Single_testName", test.getName());
        } catch (IOException e) {
            fail();
        }

        testId = "testId.webdriver";
        try {
            AbstractTest test = TestDetector.detectTestBySuffix(emul, "testId", "webdriver");
            assertEquals("testId", test.getId());
            assertEquals("http", test.getTestType());
            assertEquals("http", test.getTestType());
            assertEquals("Single_testName", test.getName());
        } catch (IOException e) {
            fail();
        }

        testId = "testId.taurus";
        try {
            AbstractTest test = TestDetector.detectTestBySuffix(emul, "testId", "taurus");
            assertEquals("testId", test.getId());
            assertEquals("http", test.getTestType());
            assertEquals("http", test.getTestType());
            assertEquals("Single_testName", test.getName());
        } catch (IOException e) {
            fail();
        }

        emul.addEmul(MultiTestTest.generateResponseGetMultiTest());
        emul.addEmul(MultiTestTest.generateResponseGetMultiTest());


        try {
            AbstractTest test = TestDetector.detectTestBySuffix(emul, "testId", "multi");
            assertEquals("testId", test.getId());
            assertEquals("multi", test.getTestType());
            assertEquals("multi", test.getTestType());
            assertEquals("Multi_testName", test.getName());
        } catch (IOException e) {
            fail();
        }

        testId = "testId.multi-location";
        try {
            AbstractTest test = TestDetector.detectTestBySuffix(emul, "testId", "multi-location");
            assertEquals("testId", test.getId());
            assertEquals("multi", test.getTestType());
            assertEquals("multi", test.getTestType());
            assertEquals("Multi_testName", test.getName());
        } catch (IOException e) {
            fail();
        }

    }

    @Test
    public void testDetectFailedMultiTest() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseTestNotFound());
        emul.addEmul(generateResponseUnauthorized());

        try {
            TestDetector.detectTest(emul, "xxxx");
            fail();
        } catch (UnexpectedResponseException ex) {
            assertEquals(2, emul.getRequests().size());
            String logs = logger.getLogs().toString();
            assertEquals(logs, 812, logs.length());
            assertTrue(logs, logs.contains("Fail for detect Multi test type id=xxxx. Reason is: Received response with the following error: Unauthorized"));
        }
    }

    @Test
    public void getTestId() {
        String test = "123.vbg";
        String testId = TestDetector.getTestId(test);
        assertEquals("123", testId);

        test = ".";
        testId = TestDetector.getTestId(test);
        assertEquals("", testId);

        test = "123";
        testId = TestDetector.getTestId(test);
        assertEquals("123", testId);

        test = "";
        testId = TestDetector.getTestId(test);
        assertEquals("", testId);
    }

    @Test
    public void getTestTypePrefix() {
        String testId = "123.blabla";
        String prefix = TestDetector.getTestTypeSuffix(testId);
        assertEquals("blabla", prefix);

        testId = ".blabla";
        prefix = TestDetector.getTestTypeSuffix(testId);
        assertEquals("blabla", prefix);

        testId = ".";
        prefix = TestDetector.getTestTypeSuffix(testId);
        assertEquals("", prefix);

        testId = "1";
        prefix = TestDetector.getTestTypeSuffix(testId);
        assertEquals("", prefix);

        testId = "112345";
        prefix = TestDetector.getTestTypeSuffix(testId);
        assertEquals("", prefix);
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

    public static String generateResponseCollectionNotFound() {
        JSONObject error = new JSONObject();
        error.put("code", 404);
        error.put("message", "Not Found: Collection not found");

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