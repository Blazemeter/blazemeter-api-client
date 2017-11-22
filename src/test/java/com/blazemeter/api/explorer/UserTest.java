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

package com.blazemeter.api.explorer;

import com.blazemeter.api.logging.LoggerTest;
import com.blazemeter.api.logging.UserNotifier;
import com.blazemeter.api.logging.UserNotifierTest;
import com.blazemeter.api.utils.BlazeMeterUtilsEmul;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Test;

import java.util.List;

import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_ADDRESS;
import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_DATA_ADDRESS;
import static org.junit.Assert.assertEquals;

public class UserTest {

    @Test
    public void testGetAccounts() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject acc = new JSONObject();
        acc.put("id", "accountId");
        acc.put("name", "accountName");

        JSONArray result = new JSONArray();
        result.add(acc);
        result.add(acc);

        JSONObject response = new JSONObject();
        response.put("result", result);
        emul.addEmul(response.toString());

        User user = new User(emul);
        List<Account> accounts = user.getAccounts();
        assertEquals(2, accounts.size());
        for (Account account : accounts) {
            assertEquals("accountId", account.getId());
            assertEquals("accountName", account.getName());
        }
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/accounts, tag=null}", emul.getRequests().get(0));
        assertEquals("Get list of accounts\r\n" +
                        "Simulating request: Request{method=GET, url=http://a.blazemeter.com/api/v4/accounts, tag=null}\r\n" +
                        "Response: {\"result\":[{\"id\":\"accountId\",\"name\":\"accountName\"},{\"id\":\"accountId\",\"name\":\"accountName\"}]}\r\n",
                logger.getLogs().toString());
    }
}