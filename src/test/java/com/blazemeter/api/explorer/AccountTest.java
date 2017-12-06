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
import static org.junit.Assert.assertTrue;

public class AccountTest {

    @Test
    public void testCreateWorkspace() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);


        emul.addEmul(generateResponseCreateWorkspace());

        Account account = new Account(emul, "777", "account_name");
        Workspace workspace = account.createWorkspace("NEW_WORKSPACE");

        assertEquals("100", workspace.getId());
        assertEquals("NEW_WORKSPACE", workspace.getName());
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/workspaces, tag=null}", emul.getRequests().get(0));
        String logs = logger.getLogs().toString();
        assertEquals(logs, 199, logs.length());
        assertTrue(logs, logs.contains("Create workspace with name=NEW_WORKSPACE"));
    }

    public static String generateResponseCreateWorkspace() {
        JSONObject result = new JSONObject();
        result.put("id", "100");
        result.put("name", "NEW_WORKSPACE");

        JSONObject response = new JSONObject();
        response.put("result", result);
        return response.toString();
    }

    @Test
    public void testGetWorkspaces() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseGetWorkspaces());

        Account account = new Account(emul, "777", "account_name");
        List<Workspace> workspaces = account.getWorkspaces();

        assertEquals(2, workspaces.size());
        for (Workspace wsp : workspaces) {
            assertEquals("100", wsp.getId());
            assertEquals("NEW_WORKSPACE", wsp.getName());
        }
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/workspaces?accountId=777&enabled=true&limit=1000, tag=null}", emul.getRequests().get(0));
        String logs = logger.getLogs().toString();
        assertEquals(logs, 275, logs.length());
        assertTrue(logs, logs.contains("Get list of workspaces for account id=777"));
    }

    public static String generateResponseGetWorkspaces() {
        JSONObject result = new JSONObject();
        result.put("id", "100");
        result.put("name", "NEW_WORKSPACE");

        JSONArray results = new JSONArray();
        results.add(result);
        results.add(result);

        JSONObject response = new JSONObject();
        response.put("result", results);
        return response.toString();
    }

    @Test
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