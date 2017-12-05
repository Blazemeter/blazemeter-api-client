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