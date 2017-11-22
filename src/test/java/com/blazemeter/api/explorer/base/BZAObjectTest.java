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

package com.blazemeter.api.explorer.base;

import com.blazemeter.api.logging.LoggerTest;
import com.blazemeter.api.logging.UserNotifier;
import com.blazemeter.api.logging.UserNotifierTest;
import com.blazemeter.api.utils.BlazeMeterUtils;
import org.junit.Test;

import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_ADDRESS;
import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_DATA_ADDRESS;
import static org.junit.Assert.*;

public class BZAObjectTest {

    @Test
    public void test() throws Exception {
        BlazeMeterUtils utils = new BlazeMeterUtils("", "", new UserNotifierTest(), new LoggerTest());
        BZAObject entity = new BZAObject(utils, "id", "name");

        assertEquals(utils, entity.getUtils());
        assertEquals("id", entity.getId());
        assertEquals("name", entity.getName());

        entity.setUtils(null);
        entity.setId("id1");
        entity.setName("name1");

        assertNull(entity.getUtils());
        assertEquals("id1", entity.getId());
        assertEquals("name1", entity.getName());
    }

    @Test
    public void testEncoding() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtils utils = new BlazeMeterUtils(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        BZAObject obj = new BZAObject(utils, "id", "name");

        assertEquals("123", obj.encode("123"));
        assertEquals("123", obj.encode("123", "Wrong encoding"));
        assertEquals("Cannot encode 123 to 'Wrong encoding' encoding\r\n" +
                "Wrong encoding\r\n", logger.getLogs().toString());
    }
}