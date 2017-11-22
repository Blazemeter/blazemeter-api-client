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
import com.blazemeter.api.logging.LoggerTest;
import com.blazemeter.api.logging.UserNotifier;
import com.blazemeter.api.logging.UserNotifierTest;
import com.blazemeter.api.utils.BlazeMeterUtilsEmul;
import net.sf.json.JSONObject;

import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_ADDRESS;
import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_DATA_ADDRESS;
import static org.junit.Assert.*;

public class AnonymousTestTest {

    @org.junit.Test
    public void testFlow() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();

        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject response = new JSONObject();
        response.put("result", generateResponse());


        AnonymousTest test = new AnonymousTest(emul);
        emul.addEmul(response.toString());
        test.startExternal();
        assertEquals(1, emul.getRequests().size());
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/sessions, tag=null}", emul.getRequests().get(0));
        checkTest(test);
        assertEquals("Start external anonymous test\r\n" +
                        "Simulating request: Request{method=POST, url=http://a.blazemeter.com/api/v4/sessions, tag=null}\r\n" +
                        "Response: {\"result\":{\"test\":{\"id\":\"responseTestId\",\"name\":\"responseTestName\"},\"signature\":\"responseSignature\",\"master\":{\"id\":\"responseMasterId\",\"name\":\"responseMasterName\"},\"session\":{\"id\":\"responseSessionId\",\"name\":\"responseSessionName\",\"userId\":\"responseUserId\"}}}\r\n",
                logger.getLogs().toString());
        logger.reset();

        try {
            test.start();
            fail("Cannot start this test type");
        } catch (UnsupportedOperationException ex) {
            assertEquals("Start is not supported for anonymous test type", ex.getMessage());
            assertEquals("Start is not supported for anonymous test type\r\n", logger.getLogs().toString());
        }
    }

    private JSONObject generateResponse() {
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
        return result;
    }


    private void checkTest(AnonymousTest test) {
        Master master = test.getMaster();
        assertEquals("responseMasterId", master.getId());
        assertEquals("responseMasterName", master.getName());

        assertEquals("responseSignature", test.getSignature());
    }

}