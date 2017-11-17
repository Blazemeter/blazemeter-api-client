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
import com.blazemeter.api.logging.Logger;
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

public class ProjectTest {

    @org.junit.Test
    public void testFlow() throws Exception {
        Logger logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();

        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject result = new JSONObject();
        result.put("id", "100");
        result.put("name", "NEW_TEST");
        JSONObject response = new JSONObject();
        response.put("result", result);

        Project project = new Project(emul, "10", "projectName");
        emul.addEmul(response.toString());
        SingleTest test = project.createSingleTest("NEW_WORKSPACE");
        assertEquals("100", test.getId());
        assertEquals("NEW_TEST", test.getName());

        response.clear();
        JSONArray results = new JSONArray();
        results.add(result);
        results.add(result);
        response.put("result", results);
        emul.addEmul(response.toString());

        List<SingleTest> tests = project.getSingleTests();
        assertEquals(2, tests.size());
        for (SingleTest t :tests) {
            assertEquals("100", t.getId());
            assertEquals("NEW_TEST", t.getName());
        }

        emul.addEmul(response.toString());
        List<MultiTest> multiTests = project.getMultiTests();
        assertEquals(2, multiTests.size());
        for (MultiTest t :multiTests) {
            assertEquals("100", t.getId());
            assertEquals("NEW_TEST", t.getName());
        }
    }

    @org.junit.Test
    public void testFromJSON() throws Exception {
        Logger logger = new LoggerTest();
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