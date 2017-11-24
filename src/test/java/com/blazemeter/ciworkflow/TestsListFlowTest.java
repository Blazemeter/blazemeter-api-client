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
import static org.junit.Assert.fail;


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
}