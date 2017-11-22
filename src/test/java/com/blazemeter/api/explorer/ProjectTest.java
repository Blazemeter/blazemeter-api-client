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
import static org.junit.Assert.*;

public class ProjectTest {

    @Test
    public void testCreateSingleTest() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject result = new JSONObject();
        result.put("id", "100");
        result.put("name", "NEW_TEST");

        JSONObject response = new JSONObject();
        response.put("result", result);
        emul.addEmul(response.toString());

        Project project = new Project(emul, "10", "projectName");
        SingleTest test = project.createSingleTest("NEW_TEST");

        assertEquals("100", test.getId());
        assertEquals("NEW_TEST", test.getName());
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/tests, tag=null}", emul.getRequests().get(0));
        assertEquals("Create single test with name=NEW_TEST\r\n" +
                        "Simulating request: Request{method=POST, url=http://a.blazemeter.com/api/v4/tests, tag=null}\r\n" +
                        "Response: {\"result\":{\"id\":\"100\",\"name\":\"NEW_TEST\"}}\r\n",
                logger.getLogs().toString());
    }

    @Test
    public void testGetSingleTests() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject result = new JSONObject();
        result.put("id", "100");
        result.put("name", "NEW_TEST");

        JSONArray results = new JSONArray();
        results.add(result);
        results.add(result);

        JSONObject response = new JSONObject();
        response.put("result", results);
        emul.addEmul(response.toString());

        Project project = new Project(emul, "10", "projectName");
        List<SingleTest> tests = project.getSingleTests();
        assertEquals(2, tests.size());
        for (SingleTest t :tests) {
            assertEquals("100", t.getId());
            assertEquals("NEW_TEST", t.getName());
        }
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/tests?projectId=10, tag=null}", emul.getRequests().get(0));
        assertEquals("Get list of single tests for project id=10\r\n" +
                        "Simulating request: Request{method=GET, url=http://a.blazemeter.com/api/v4/tests?projectId=10, tag=null}\r\n" +
                        "Response: {\"result\":[{\"id\":\"100\",\"name\":\"NEW_TEST\"},{\"id\":\"100\",\"name\":\"NEW_TEST\"}]}\r\n",
                logger.getLogs().toString());
    }

    @Test
    public void testGetMultiTests() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject result = new JSONObject();
        result.put("id", "100");
        result.put("name", "NEW_TEST");

        JSONArray results = new JSONArray();
        results.add(result);
        results.add(result);

        JSONObject response = new JSONObject();
        response.put("result", results);
        emul.addEmul(response.toString());

        Project project = new Project(emul, "10", "projectName");
        List<MultiTest> multiTests = project.getMultiTests();
        assertEquals(2, multiTests.size());
        for (MultiTest t :multiTests) {
            assertEquals("100", t.getId());
            assertEquals("NEW_TEST", t.getName());
        }
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/tests?projectId=10, tag=null}", emul.getRequests().get(0));
        assertEquals("Get list of multi tests for project id=10\r\n" +
                        "Simulating request: Request{method=GET, url=http://a.blazemeter.com/api/v4/tests?projectId=10, tag=null}\r\n" +
                        "Response: {\"result\":[{\"id\":\"100\",\"name\":\"NEW_TEST\"},{\"id\":\"100\",\"name\":\"NEW_TEST\"}]}\r\n",
                logger.getLogs().toString());
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