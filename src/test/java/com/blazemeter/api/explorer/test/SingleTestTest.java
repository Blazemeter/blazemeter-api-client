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
import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.logging.LoggerTest;
import com.blazemeter.api.logging.UserNotifier;
import com.blazemeter.api.logging.UserNotifierTest;
import com.blazemeter.api.utils.BlazeMeterUtilsEmul;
import net.sf.json.JSONObject;

import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_ADDRESS;
import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_DATA_ADDRESS;
import static org.junit.Assert.*;

public class SingleTestTest {

    @org.junit.Test
    public void testFlow() throws Exception {
        Logger logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();

        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject response = new JSONObject();
        response.put("result", generateResponse());

        SingleTest test = new SingleTest(emul, "testId", "testName");
        emul.addEmul(response.toString());
        test.startExternal();
        assertEquals(1, emul.getRequests().size());
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/tests/testId/start-external, tag=null}", emul.getRequests().get(0));
        checkTest(test);
        emul.clean();

        test = new SingleTest(emul, "testId", "testName");
        emul.addEmul(response.toString());
        test.start();
        assertEquals(1, emul.getRequests().size());
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/tests/testId/start, tag=null}", emul.getRequests().get(0));
        checkTest(test);
    }

    private JSONObject generateResponse() {
        JSONObject testResponse = new JSONObject();
        testResponse.put("id", "responseTestId");
        testResponse.put("name", "responseTestName");

        JSONObject masterResponse = new JSONObject();
        masterResponse.put("id", "responseMasterId");
        masterResponse.put("name", "responseMasterName");

        JSONObject result = new JSONObject();
        result.put("test", testResponse);
        result.put("signature", "responseSignature");
        result.put("master", masterResponse);
        return result;
    }


    private void checkTest(SingleTest test) {
        Master master = test.getMaster();
        assertEquals("responseMasterId", master.getId());
        assertEquals("responseMasterName", master.getName());

        assertEquals("responseSignature", test.getSignature());
    }

    @org.junit.Test
    public void testFromJSON() throws Exception {
        Logger logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();

        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        JSONObject object = new JSONObject();
        object.put("id", "testId");
        object.put("name", "testName");
        SingleTest test = SingleTest.fromJSON(emul, object);
        assertEquals("testId", test.getId());
        assertEquals("testName", test.getName());
    }
}