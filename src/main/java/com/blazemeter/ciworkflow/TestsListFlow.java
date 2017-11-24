package com.blazemeter.ciworkflow;

import com.blazemeter.api.explorer.Account;
import com.blazemeter.api.explorer.User;
import com.blazemeter.api.explorer.Workspace;
import com.blazemeter.api.explorer.test.AbstractTest;
import com.blazemeter.api.explorer.test.MultiTest;
import com.blazemeter.api.explorer.test.SingleTest;
import com.blazemeter.api.utils.BlazeMeterUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestsListFlow {

    private BlazeMeterUtils utils;

    public TestsListFlow() {
    }

    public TestsListFlow(BlazeMeterUtils utils) {
        this.utils = utils;
    }

    /**
     * @return List of all tests that available for your credentials
     */
    public List<AbstractTest> getUsersTests() {
        final List<AbstractTest> result = new ArrayList<>();
        try {
            User user = new User(utils);
            List<Account> accounts = user.getAccounts();
            for (Account account : accounts) {
                result.addAll(getTestsForAccount(account));
            }
        } catch (IOException ex) {
            utils.getNotifier().notifyAbout("Failed to get accounts. Reason is: " + ex.getMessage());
            utils.getLogger().error("Failed to get accounts. Reason is: " + ex.getMessage(), ex);
        }
        return result;
    }

    private List<AbstractTest> getTestsForAccount(Account account) {
        final List<AbstractTest> result = new ArrayList<>();
        try {
            List<Workspace> workspaces = account.getWorkspaces();
            for (Workspace workspace : workspaces) {
                result.addAll(getSingleTestsForWorkspace(workspace));
                result.addAll(getMultiTestsForWorkspace(workspace));
            }
        } catch (IOException e) {
            utils.getNotifier().notifyAbout("Failed to get workspaces for account id =" + account.getId() +". Reason is: " + e.getMessage());
            utils.getLogger().error("Failed to get workspaces for account id =" + account.getId() +". Reason is: " + e.getMessage(), e);
        }
        return result;
    }

    private List<SingleTest> getSingleTestsForWorkspace(Workspace workspace) {
        try {
            return workspace.getSingleTests();
        } catch (IOException e) {
            utils.getNotifier().notifyAbout("Failed to get single tests for workspace id =" + workspace.getId() +". Reason is: " + e.getMessage());
            utils.getLogger().error("Failed to get single tests for workspace id =" + workspace.getId() +". Reason is: " + e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    private List<MultiTest> getMultiTestsForWorkspace(Workspace workspace) {
        try {
            return workspace.getMultiTests();
        } catch (IOException e) {
            utils.getNotifier().notifyAbout("Failed to get multi tests for workspace id =" + workspace.getId() +". Reason is: " + e.getMessage());
            utils.getLogger().error("Failed to get multi tests for workspace id =" + workspace.getId() +". Reason is: " + e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    public BlazeMeterUtils getUtils() {
        return utils;
    }

    public void setUtils(BlazeMeterUtils utils) {
        this.utils = utils;
    }
}
