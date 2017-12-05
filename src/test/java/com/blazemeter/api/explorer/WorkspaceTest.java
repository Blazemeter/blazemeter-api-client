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

import com.blazemeter.api.explorer.test.MultiTest;
import com.blazemeter.api.explorer.test.SingleTest;
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

public class WorkspaceTest {

    @Test
    public void testCreateProject() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseCreateProject());

        Workspace workspace = new Workspace(emul, "888", "workspace_name");
        Project project = workspace.createProject("NEW_PROJECT");
        assertEquals("999", project.getId());
        assertEquals("NEW_PROJECT", project.getName());
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/projects, tag=null}", emul.getRequests().get(0));
        String logs = logger.getLogs().toString();
        assertEquals(logs, 191, logs.length());
        assertTrue(logs, logs.contains("Create project with name=NEW_PROJECT"));
    }

    public static String generateResponseCreateProject() {
        JSONObject result = new JSONObject();
        result.put("id", "999");
        result.put("name", "NEW_PROJECT");

        JSONObject response = new JSONObject();
        response.put("result", result);
        return response.toString();
    }

    @Test
    public void testGetProjects() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseGetProjects());

        Workspace workspace = new Workspace(emul, "888", "workspace_name");
        List<Project> projects = workspace.getProjects();
        assertEquals(2, projects.size());
        for (Project p : projects) {
            assertEquals("999", p.getId());
            assertEquals("NEW_PROJECT", p.getName());
        }
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/projects?workspaceId=888&sort%5B%5D=name&limit=10000, tag=null}", emul.getRequests().get(0));
        String logs = logger.getLogs().toString();
        assertEquals(logs, 275, logs.length());
        assertTrue(logs, logs.contains("Get list of projects for workspace id=888"));
    }

    public static String generateResponseGetProjects() {
        JSONObject project = new JSONObject();
        project.put("id", "999");
        project.put("name", "NEW_PROJECT");

        JSONArray result = new JSONArray();
        result.add(project);
        result.add(project);

        JSONObject response = new JSONObject();
        response.put("result", result);
        return response.toString();
    }

    @Test
    public void testGetSingleTests() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseGetSingleTests());

        Workspace workspace = new Workspace(emul, "888", "workspace_name");
        List<SingleTest> tests = workspace.getSingleTests();
        assertEquals(2, tests.size());
        for (SingleTest t :tests) {
            assertEquals("999", t.getId());
            assertEquals("SINGLE_TEST", t.getName());
            assertEquals("http", t.getTestType());
        }
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/tests?workspaceId=888, tag=null}", emul.getRequests().get(0));
        String logs = logger.getLogs().toString();
        assertEquals(logs, 312, logs.length());
        assertTrue(logs, logs.contains("Get list of single tests for workspace id=888"));
    }

    public static String generateResponseGetSingleTests() {
        JSONObject configuration = new JSONObject();
        configuration.put("type", "http");

        JSONObject test = new JSONObject();
        test.put("id", "999");
        test.put("name", "SINGLE_TEST");
        test.put("configuration", configuration);

        JSONArray result = new JSONArray();
        result.add(test);
        result.add(test);

        JSONObject response = new JSONObject();
        response.put("result", result);
        return response.toString();
    }

    @Test
    public void testGetMultiTests() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseGetMultiTests());

        Workspace workspace = new Workspace(emul, "888", "workspace_name");
        List<MultiTest> multiTests = workspace.getMultiTests();
        assertEquals(2, multiTests.size());
        for (MultiTest t :multiTests) {
            assertEquals("999", t.getId());
            assertEquals("MULTI_TEST", t.getName());
            assertEquals("multi", t.getTestType());
        }
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/multi-tests?workspaceId=888, tag=null}", emul.getRequests().get(0));
        String logs = logger.getLogs().toString();
        assertEquals(logs, 301, logs.length());
        assertTrue(logs, logs.contains("Get list of multi tests for workspace id=888"));
    }

    public static String generateResponseGetMultiTests() {
        JSONObject test = new JSONObject();
        test.put("id", "999");
        test.put("name", "MULTI_TEST");
        test.put("collectionType", "multi");

        JSONArray result = new JSONArray();
        result.add(test);
        result.add(test);

        JSONObject response = new JSONObject();
        response.put("result", result);
        return response.toString();
    }

    @Test
    public void testFromJSON() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject object = new JSONObject();
        object.put("id", "workspaceId");
        object.put("name", "workspaceName");
        Workspace workspace = Workspace.fromJSON(emul, object);
        assertEquals("workspaceId", workspace.getId());
        assertEquals("workspaceName", workspace.getName());
    }

}