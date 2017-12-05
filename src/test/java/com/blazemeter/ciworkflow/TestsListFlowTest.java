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

package com.blazemeter.ciworkflow;

import com.blazemeter.api.explorer.AccountTest;
import com.blazemeter.api.explorer.UserTest;
import com.blazemeter.api.explorer.WorkspaceTest;
import com.blazemeter.api.explorer.test.AbstractTest;
import com.blazemeter.api.logging.LoggerTest;
import com.blazemeter.api.logging.UserNotifierTest;
import com.blazemeter.api.utils.BlazeMeterUtilsEmul;
import org.junit.Test;

import java.util.List;

import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_ADDRESS;
import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_DATA_ADDRESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class TestsListFlowTest {

    @Test
    public void testFlow() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(UserTest.generateResponseGetAccounts());

        emul.addEmul(AccountTest.generateResponseGetWorkspaces());
        emul.addEmul(WorkspaceTest.generateResponseGetSingleTests());
        emul.addEmul(WorkspaceTest.generateResponseGetMultiTests());
        emul.addEmul(WorkspaceTest.generateResponseGetSingleTests());
        emul.addEmul(WorkspaceTest.generateResponseGetMultiTests());

        emul.addEmul(AccountTest.generateResponseGetWorkspaces());
        emul.addEmul(WorkspaceTest.generateResponseGetSingleTests());
        emul.addEmul(WorkspaceTest.generateResponseGetMultiTests());
        emul.addEmul(WorkspaceTest.generateResponseGetSingleTests());
        emul.addEmul(WorkspaceTest.generateResponseGetMultiTests());

        TestsListFlow flow = new TestsListFlow(emul);

        List<AbstractTest> usersTests = flow.getUsersTests();
        assertEquals(16, usersTests.size());
        assertFalse(logger.getLogs().toString().contains("Fail"));
        List<String> requests = emul.getRequests();
        assertEquals(11, requests.size());

        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/accounts, tag=null}", requests.get(0));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/workspaces?accountId=accountId&enabled=true&limit=100, tag=null}", requests.get(1));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/tests?workspaceId=100, tag=null}", requests.get(2));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/multi-tests?workspaceId=100, tag=null}", requests.get(3));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/tests?workspaceId=100, tag=null}", requests.get(4));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/multi-tests?workspaceId=100, tag=null}", requests.get(5));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/workspaces?accountId=accountId&enabled=true&limit=100, tag=null}", requests.get(6));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/tests?workspaceId=100, tag=null}", requests.get(7));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/multi-tests?workspaceId=100, tag=null}", requests.get(8));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/tests?workspaceId=100, tag=null}", requests.get(9));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/multi-tests?workspaceId=100, tag=null}", requests.get(10));
    }

    @Test
    public void testGetters() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        TestsListFlow flow = new TestsListFlow();
        flow.setUtils(emul);
        assertEquals(emul, flow.getUtils());
    }

    @Test
    public void testFailGetAccount() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        TestsListFlow flow = new TestsListFlow(emul);

        List<AbstractTest> usersTests = flow.getUsersTests();
        assertEquals(0, usersTests.size());
        assertTrue(logger.getLogs().toString().contains("Failed to get accounts. Reason is:"));
        assertTrue(notifier.getLogs().toString().contains("Failed to get accounts. Reason is:"));
    }

    @Test
    public void testFailGetWorkspaces() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        emul.addEmul(UserTest.generateResponseGetAccounts());

        TestsListFlow flow = new TestsListFlow(emul);

        List<AbstractTest> usersTests = flow.getUsersTests();
        assertEquals(0, usersTests.size());
        assertTrue(logger.getLogs().toString().contains("Failed to get workspaces for account id ="));
        assertTrue(notifier.getLogs().toString().contains("Failed to get workspaces for account id ="));
    }

    @Test
    public void testFailTests() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        emul.addEmul(UserTest.generateResponseGetAccounts());
        emul.addEmul(AccountTest.generateResponseGetWorkspaces());

        TestsListFlow flow = new TestsListFlow(emul);

        List<AbstractTest> usersTests = flow.getUsersTests();
        assertEquals(0, usersTests.size());
        assertTrue(logger.getLogs().toString().contains("Failed to get single tests for workspace id ="));
        assertTrue(notifier.getLogs().toString().contains("Failed to get single tests for workspace id ="));
        assertTrue(logger.getLogs().toString().contains("Failed to get multi tests for workspace id ="));
        assertTrue(notifier.getLogs().toString().contains("Failed to get multi tests for workspace id ="));
    }
}