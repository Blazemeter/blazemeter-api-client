/**
 * Copyright 2018 BlazeMeter Inc.
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

/**
 * Convenient wrapper for getting tests available for particular credentials.
 */
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
            utils.getNotifier().notifyError("Failed to get accounts. Reason is: " + ex.getMessage());
            utils.getLogger().error("Failed to get accounts. Reason is: " + ex.getMessage(), ex);
        }
        return result;
    }

    protected List<AbstractTest> getTestsForAccount(Account account) {
        final List<AbstractTest> result = new ArrayList<>();
        try {
            List<Workspace> workspaces = account.getWorkspaces();
            for (Workspace workspace : workspaces) {
                result.addAll(getSingleTestsForWorkspace(workspace));
                result.addAll(getMultiTestsForWorkspace(workspace));
            }
        } catch (IOException e) {
            utils.getNotifier().notifyError("Failed to get workspaces for account id =" + account.getId() + ". Reason is: " + e.getMessage());
            utils.getLogger().error("Failed to get workspaces for account id =" + account.getId() + ". Reason is: " + e.getMessage(), e);
        }
        return result;
    }

    public List<AbstractTest> getAllTestsForWorkspace(Workspace workspace) {
        final List<AbstractTest> result = new ArrayList<>();
        result.addAll(getSingleTestsForWorkspace(workspace));
        result.addAll(getMultiTestsForWorkspace(workspace));
        return result;
    }

    protected List<SingleTest> getSingleTestsForWorkspace(Workspace workspace) {
        try {
            return workspace.getSingleTests();
        } catch (IOException e) {
            utils.getNotifier().notifyError("Failed to get single tests for workspace id =" + workspace.getId() + ". Reason is: " + e.getMessage());
            utils.getLogger().error("Failed to get single tests for workspace id =" + workspace.getId() + ". Reason is: " + e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    protected List<MultiTest> getMultiTestsForWorkspace(Workspace workspace) {
        try {
            return workspace.getMultiTests();
        } catch (IOException e) {
            utils.getNotifier().notifyError("Failed to get multi tests for workspace id =" + workspace.getId() + ". Reason is: " + e.getMessage());
            utils.getLogger().error("Failed to get multi tests for workspace id =" + workspace.getId() + ". Reason is: " + e.getMessage(), e);
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
