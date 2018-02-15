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
import static org.junit.Assert.fail;

public class AnonymousTestTest {

    @Test
    public void testStart() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        AnonymousTest test = new AnonymousTest(emul);
        try {
            test.start();
            fail("Cannot start this test type");
        } catch (UnsupportedOperationException ex) {
            assertEquals("Start is not supported for anonymous test type", ex.getMessage());
            assertEquals("Start is not supported for anonymous test type\r\n", logger.getLogs().toString());
        }
    }

    @Test
    public void testStartExternal() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject response = new JSONObject();
        response.put("result", generateResponseStartExternalAnonymousTest());

        AnonymousTest test = new AnonymousTest(emul);
        emul.addEmul(response.toString());
        Master master = test.startExternal();
        assertEquals(1, emul.getRequests().size());
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/sessions, tag=null}", emul.getRequests().get(0));
        checkTest(test);
        String logs = logger.getLogs().toString();
        assertEquals(logs, 396, logs.length());
        assertTrue(logs, logs.contains("Start external anonymous test"));
        Session session = test.getSession();
        assertEquals("responseSessionId", session.getId());
        assertEquals("responseMasterId", master.getId());
        assertEquals("external", test.getTestType());
    }

    @Test
    public void testStartWithProperties() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        AnonymousTest test = new AnonymousTest(emul);
        try {
            test.startWithProperties("");
            fail("Cannot start this test type");
        } catch (UnsupportedOperationException ex) {
            assertEquals("Start is not supported for anonymous test type", ex.getMessage());
            assertEquals("Start is not supported for anonymous test type\r\n", logger.getLogs().toString());
        }
    }

    public static String generateResponseStartExternalAnonymousTest() {
        JSONObject testResponse = new JSONObject();
        testResponse.put("id", "responseTestId");
        testResponse.put("name", "responseTestName");

        JSONObject masterResponse = new JSONObject();
        masterResponse.put("id", "responseMasterId");
        masterResponse.put("name", "responseMasterName");

        JSONObject sessionResponse = new JSONObject();
        sessionResponse.put("id", "responseSessionId");
        sessionResponse.put("name", "responseSessionName");
        sessionResponse.put("userId", "responseUserId");

        JSONObject result = new JSONObject();
        result.put("test", testResponse);
        result.put("signature", "responseSignature");
        result.put("master", masterResponse);
        result.put("session", sessionResponse);
        return result.toString();
    }


    private void checkTest(AnonymousTest test) {
        Master master = test.getMaster();
        assertEquals("responseMasterId", master.getId());
        assertEquals("responseMasterName", master.getName());
        assertEquals("responseSignature", test.getSignature());
    }

}