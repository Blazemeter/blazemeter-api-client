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

public class ProjectTest {

    @Test
    public void testCreateSingleTest() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseCreateProject());

        Project project = new Project(emul, "10", "projectName");
        SingleTest test = project.createSingleTest("NEW_TEST");

        assertEquals("100", test.getId());
        assertEquals("NEW_TEST", test.getName());
        assertEquals("http", test.getTestType());
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/tests, tag=null}", emul.getRequests().get(0));
        String logs = logger.getLogs().toString();
        assertEquals(logs, 218, logs.length());
        assertTrue(logs, logs.contains("Create single test with name=NEW_TEST"));
    }

    public static String generateResponseCreateProject() {
        JSONObject configuration = new JSONObject();
        configuration.put("type", "http");

        JSONObject result = new JSONObject();
        result.put("id", "100");
        result.put("name", "NEW_TEST");
        result.put("configuration", configuration);

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

        Project project = new Project(emul, "10", "projectName");
        List<SingleTest> tests = project.getSingleTests();
        assertEquals(2, tests.size());
        for (SingleTest t : tests) {
            assertEquals("100", t.getId());
            assertEquals("NEW_TEST", t.getName());
            assertEquals("http", t.getTestType());
        }
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/tests?projectId=10&sort%5B%5D=name&limit=10000, tag=null}", emul.getRequests().get(0));
        String logs = logger.getLogs().toString();
        assertEquals(logs, 328, logs.length());
        assertTrue(logs, logs.contains("Get list of single tests for project id=10"));
    }

    public static String generateResponseGetSingleTests() {
        JSONObject configuration = new JSONObject();
        configuration.put("type", "http");

        JSONObject result = new JSONObject();
        result.put("id", "100");
        result.put("name", "NEW_TEST");
        result.put("configuration", configuration);

        JSONArray results = new JSONArray();
        results.add(result);
        results.add(result);

        JSONObject response = new JSONObject();
        response.put("result", results);
        return response.toString();
    }

    @Test
    public void testGetMultiTests() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseGetMultiTests());

        Project project = new Project(emul, "10", "projectName");
        List<MultiTest> multiTests = project.getMultiTests();
        assertEquals(2, multiTests.size());
        for (MultiTest t : multiTests) {
            assertEquals("100", t.getId());
            assertEquals("NEW_TEST", t.getName());
            assertEquals("multi", t.getTestType());
        }
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/multi-tests?projectId=10, tag=null}", emul.getRequests().get(0));
        String logs = logger.getLogs().toString();
        assertEquals(logs, 291, logs.length());
        assertTrue(logs, logs.contains("Get list of multi tests for project id=10"));
    }

    public static String generateResponseGetMultiTests() {
        JSONObject result = new JSONObject();
        result.put("id", "100");
        result.put("name", "NEW_TEST");
        result.put("collectionType", "multi");

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
        object.put("id", "projectId");
        object.put("name", "projectName");

        Project project = Project.fromJSON(emul, object);
        assertEquals("projectId", project.getId());
        assertEquals("projectName", project.getName());
    }
}