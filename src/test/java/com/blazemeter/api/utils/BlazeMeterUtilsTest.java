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

package com.blazemeter.api.utils;

import com.blazemeter.api.exception.UnexpectedResponseException;
import com.blazemeter.api.logging.LoggerTest;
import com.blazemeter.api.logging.UserNotifier;
import com.blazemeter.api.logging.UserNotifierTest;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import okhttp3.Request;
import org.junit.Test;

import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_ADDRESS;
import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_DATA_ADDRESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class BlazeMeterUtilsTest {

    @Test
    public void testProcessResponse() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtils utils = new BlazeMeterUtils(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject result = utils.processResponse("{\"result\": \"ok\"}");
        assertNotNull(result);
        assertEquals("ok", result.get("result"));
        result = utils.processResponse("{\"result\": \"ok\", \"error\": null}");
        assertNotNull(result);
        assertEquals("ok", result.get("result"));

        try {
            utils.processResponse("{\"result\": \"ok\",\"error\": {\"code\": 404, \"message\": \"Not Found: Project not found\"}}");
            fail("Must fail, because response have empty 'result'");
        } catch (UnexpectedResponseException ex) {
            assertEquals("Received response with the following error: Not Found: Project not found", ex.getMessage());
            assertEquals("Received response with the following error: Not Found: Project not found\r\n", logger.getLogs().toString());
        }
        logger.reset();
        try {
            utils.processResponse("{\"result\": null,\"error\": {\"code\": 404, \"message\": \"Not Found: Project not found\"}}");
            fail("Must fail, because response have empty 'result'");
        } catch (UnexpectedResponseException ex) {
            assertEquals("Received response with the following error: Not Found: Project not found", ex.getMessage());
            assertEquals("Received response with the following error: Not Found: Project not found\r\n", logger.getLogs().toString());
        }

        logger.reset();

        try {
            utils.processResponse("incorrect json");
            fail("Incorrect json format");
        } catch (JSONException ex) {
            assertEquals("A JSONObject text must begin with '{' at character 1 of incorrect json", ex.getMessage());
            assertEquals("Cannot parse response: incorrect json\r\n" +
                    "A JSONObject text must begin with '{' at character 1 of incorrect json\r\n", logger.getLogs().toString());
        } catch (UnexpectedResponseException ex) {
            assertEquals("Received response with the following error: Cannot parse response: incorrect json", ex.getMessage());
            assertEquals(194, logger.getLogs().toString().length());
        }
    }

    @Test
    public void testSetters() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtils utils = new BlazeMeterUtils(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        Request.Builder builder = new Request.Builder().url(BZM_ADDRESS).get();

        Request request = utils.addRequiredHeader(builder).build();
        assertEquals(0, request.headers().size());
        utils.setAddress(BZM_ADDRESS);
        utils.setDataAddress(BZM_DATA_ADDRESS);
        utils.setApiKeyId("xxxx");
        utils.setApiKeySecret("yyy");
        request = utils.addRequiredHeader(builder).build();
        assertEquals(1, request.headers().size());
        assertEquals(BZM_ADDRESS, utils.getAddress());
        assertEquals(BZM_DATA_ADDRESS, utils.getDataAddress());
    }

    @Test
    public void testGetTimeoutFailed() throws Exception {
        System.setProperty("bzm.checkTimeout", "aaaa");
        try {
            long checkTimeout = BlazeMeterUtils.getCheckTimeout();
            assertEquals(10000, checkTimeout);
        } finally {
            System.setProperty("bzm.checkTimeout", "10000");
        }
    }
}