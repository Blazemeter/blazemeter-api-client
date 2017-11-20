package com.blazemeter.api.http;

import com.blazemeter.api.logging.LoggerTest;
import org.junit.Test;

import static org.junit.Assert.*;

public class HttpLoggerTest {

    @Test
    public void testFormat() throws Exception {
        LoggerTest logger = new LoggerTest();
        HttpLogger httpLogger = new HttpLogger(logger);
        httpLogger.log("Accept: application/json");
        httpLogger.log("Authorization: xxxxxxxxxxxxxx:yyyyyyyyyyyyyyyyyyyyyyyyyyyyy");
        assertEquals("Accept: application/json\r\nAuthorization:...yyyyyyyyyy\r\n", logger.getLogs().toString());
    }
}