package com.blazemeter.api.utils;

import com.blazemeter.api.explorer.exception.UnexpectedResponseException;
import com.blazemeter.api.logging.LoggerTest;
import com.blazemeter.api.logging.UserNotifier;
import com.blazemeter.api.logging.UserNotifierTest;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.junit.Test;

import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_ADDRESS;
import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_DATA_ADDRESS;
import static org.junit.Assert.*;

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
            assertEquals("Receive response with the following error message: Not Found: Project not found", ex.getMessage());
            assertEquals("Receive response with the following error message: Not Found: Project not found\r\n", logger.getLogs().toString());
        }
        logger.reset();
        try {
            utils.processResponse("{\"result\": null,\"error\": {\"code\": 404, \"message\": \"Not Found: Project not found\"}}");
            fail("Must fail, because response have empty 'result'");
        } catch (UnexpectedResponseException ex) {
            assertEquals("Receive response with the following error message: Not Found: Project not found", ex.getMessage());
            assertEquals("Receive response with the following error message: Not Found: Project not found\r\n", logger.getLogs().toString());
        }

        logger.reset();

        try {
            utils.processResponse("incorrect json");
            fail("Incorrect json format");
        } catch (JSONException ex) {
            assertEquals("A JSONObject text must begin with '{' at character 1 of incorrect json", ex.getMessage());
            assertEquals("Cannot parse response: incorrect json\r\n" +
                    "A JSONObject text must begin with '{' at character 1 of incorrect json\r\n", logger.getLogs().toString());
        }
    }
}