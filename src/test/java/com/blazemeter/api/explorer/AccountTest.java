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

import java.util.List;

import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_ADDRESS;
import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_DATA_ADDRESS;
import static org.junit.Assert.*;

public class AccountTest {

    @org.junit.Test
    public void testFlow() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();

        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        JSONObject result = new JSONObject();
        result.put("id", "100");
        result.put("name", "NEW_WORKSPACE");
        JSONObject response = new JSONObject();
        response.put("result", result);

        Account account = new Account(emul, "777", "account_name");
        emul.addEmul(response.toString());
        Workspace workspace = account.createWorkspace("NEW_WORKSPACE");
        assertEquals("100", workspace.getId());
        assertEquals("NEW_WORKSPACE", workspace.getName());
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/workspaces, tag=null}", emul.getRequests().get(0));
        assertEquals("Create workspace with name=NEW_WORKSPACE\r\n" +
                        "Simulating request: Request{method=POST, url=http://a.blazemeter.com/api/v4/workspaces, tag=null}\r\n" +
                        "Response: {\"result\":{\"id\":\"100\",\"name\":\"NEW_WORKSPACE\"}}\r\n",
                logger.getLogs().toString());


        emul.clean();
        logger.reset();
        response.clear();
        JSONArray results = new JSONArray();
        results.add(result);
        results.add(result);
        response.put("result", results);
        emul.addEmul(response.toString());

        List<Workspace> workspaces = account.getWorkspaces();
        assertEquals(2, workspaces.size());
        for (Workspace wsp : workspaces) {
            assertEquals("100", wsp.getId());
            assertEquals("NEW_WORKSPACE", wsp.getName());
        }
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/workspaces?accountId=777&enabled=true&limit=100, tag=null}", emul.getRequests().get(0));
        assertEquals("Get list of workspaces for account id=777\r\n" +
                        "Simulating request: Request{method=GET, url=http://a.blazemeter.com/api/v4/workspaces?accountId=777&enabled=true&limit=100, tag=null}\r\n" +
                        "Response: {\"result\":[{\"id\":\"100\",\"name\":\"NEW_WORKSPACE\"},{\"id\":\"100\",\"name\":\"NEW_WORKSPACE\"}]}\r\n",
                logger.getLogs().toString());
    }

    @org.junit.Test
    public void testFromJSON() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();

        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject object = new JSONObject();
        object.put("id", "accountId");
        object.put("name", "accountName");
        Account account = Account.fromJSON(emul, object);
        assertEquals("accountId", account.getId());
        assertEquals("accountName", account.getName());
    }
}