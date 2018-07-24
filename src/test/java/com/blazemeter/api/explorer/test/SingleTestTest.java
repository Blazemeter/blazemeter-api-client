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

package com.blazemeter.api.explorer.test;

import com.blazemeter.api.explorer.Master;
import com.blazemeter.api.explorer.Session;
import com.blazemeter.api.logging.LoggerTest;
import com.blazemeter.api.logging.UserNotifier;
import com.blazemeter.api.logging.UserNotifierTest;
import com.blazemeter.api.utils.BlazeMeterUtilsEmul;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_ADDRESS;
import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_DATA_ADDRESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SingleTestTest {

    @Test
    public void testStart() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseStartSingleTest());

        SingleTest test = new SingleTest(emul, "testId", "testName", "http");
        Master master = test.start();

        assertEquals(1, emul.getRequests().size());
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/tests/testId/start, tag=null}", emul.getRequests().get(0));
        checkTest(test);
        String logs = logger.getLogs().toString();
        assertEquals(logs, 212, logs.length());
        assertTrue(logs, logs.contains("Start single test id=testId"));
        assertEquals("responseMasterId", master.getId());
        assertEquals("http", test.getTestType());
    }

    @Test
    public void testStartWithProperties() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseStartSingleTest());

        SingleTest test = new SingleTest(emul, "testId", "testName", "http"){
            @Override
            protected JSONObject sendStartTestWithBody(String uri, String body) throws IOException {
                assertEquals(body, "{\"data\":{\"configuration\":{\"plugins\":{\"remoteControl\":[{\"key\":\"command_property\",\"value\":\"fsdfsd\"},{\"key\":\"command_property2\",\"value\":\"fsdfsd22\"}]}}}}");
                return super.sendStartTestWithBody(uri, body);
            }
        };
        Master master = test.startWithProperties("command_property=fsdfsd,command_property2=fsdfsd22");

        assertEquals(1, emul.getRequests().size());
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/tests/testId/start, tag=null}", emul.getRequests().get(0));
        checkTest(test);
        String logs = logger.getLogs().toString();
        assertEquals(logs, 212, logs.length());
        assertTrue(logs, logs.contains("Start single test id=testId"));
        assertEquals("responseMasterId", master.getId());
        assertEquals("http", test.getTestType());
        assertTrue(emul.getRequestsBody().get(0).contains("size=149"));
    }

    @Test
    public void testStartExternal() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseStartSingleTest());

        SingleTest test = new SingleTest(emul, "testId", "testName", "http");
        Master master = test.startExternal();

        assertEquals(1, emul.getRequests().size());
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/tests/testId/start-external, tag=null}", emul.getRequests().get(0));
        checkTest(test);
        String logs = logger.getLogs().toString();
        assertEquals(logs, 230, logs.length());
        assertTrue(logs, logs.contains("Start external single test id=testId"));
        assertEquals("responseMasterId", master.getId());
        assertEquals("http", test.getTestType());
    }

    @Test
    public void testUploadFile() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseStartSingleTest());

        String path = SingleTestTest.class.getResource("/test.yml").getPath();
        File file = new File(path);

        SingleTest test = new SingleTest(emul, "testId", "testName", "http");
        test.uploadFile(file);

        assertEquals(1, emul.getRequests().size());
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/tests/testId/files, tag=null}", emul.getRequests().get(0));
        String logs = logger.getLogs().toString();
        assertEquals(logs, 316, logs.length());
        assertTrue(logs, logs.contains("Upload file to single test id=testId"));
    }


    @Test
    public void testUpdateFilename() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseStartSingleTest());

        SingleTest test = new SingleTest(emul, "testId", "testName", "jmeter");
        test.updateTestFilename("newTest.jmx");

        assertEquals(1, emul.getRequests().size());
        assertEquals("Request{method=PATCH, url=http://a.blazemeter.com/api/v4/tests/testId, tag=null}", emul.getRequests().get(0));
        String logs = logger.getLogs().toString();
        assertEquals(logs, 437, logs.length());
        assertTrue(logs, logs.contains("Update single test id=testId filename=newTest.jmx"));
        assertTrue(logs, logs.contains("Update single test id=testId data={\"configuration\":{\"plugins\":{\"jmeter\":{\"filename\":\"newTest.jmx\"}}}}"));
    }

    @Test
    public void testUpdateFilename1() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseStartSingleTest());

        SingleTest test = new SingleTest(emul, "testId", "testName", "taurus");
        test.updateTestFilename("newTest.jmx");

        assertEquals(1, emul.getRequests().size());
        assertEquals("Request{method=PATCH, url=http://a.blazemeter.com/api/v4/tests/testId, tag=null}", emul.getRequests().get(0));
        String logs = logger.getLogs().toString();
        assertEquals(logs, 456, logs.length());
        assertTrue(logs, logs.contains("Update single test id=testId filename=newTest.jmx"));
        assertTrue(logs, logs.contains("Update single test id=testId data={\"configuration\":{\"testMode\":\"script\",\"filename\":\"newTest.jmx\",\"scriptType\":\"jmeter\"}}"));
    }

    @Test
    public void testUpdateFilename2() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseStartSingleTest());

        SingleTest test = new SingleTest(emul, "testId", "testName", "functionalApi");
        test.updateTestFilename("test.yml");

        assertEquals(1, emul.getRequests().size());
        assertEquals("Request{method=PATCH, url=http://a.blazemeter.com/api/v4/tests/testId, tag=null}", emul.getRequests().get(0));
        String logs = logger.getLogs().toString();
        assertEquals(logs, 450, logs.length());
        assertTrue(logs, logs.contains("Update single test id=testId filename=test.yml"));
        assertTrue(logs, logs.contains("Update single test id=testId data={\"configuration\":{\"testMode\":\"script\",\"filename\":\"test.yml\",\"scriptType\":\"taurus\"}}"));
    }

    @Test
    public void testUpdateFilename3() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseStartSingleTest());

        SingleTest test = new SingleTest(emul, "testId", "testName", "http");
        test.updateTestFilename("newTest.jmx");

        assertEquals(0, emul.getRequests().size());
        String logs = logger.getLogs().toString();
        assertEquals(logs, 112, logs.length());
        assertTrue(logs, logs.contains("Update single test id=testId filename=newTest.jmx"));
        assertTrue(logs, logs.contains("This test type 'http' does not support script configuration"));
    }

    @Test
    public void testUpdateFilename4() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseStartSingleTest());

        SingleTest test = new SingleTest(emul, "testId", "testName", "functionalApi");
        test.updateTestFilename("test.yaml");

        assertEquals(1, emul.getRequests().size());
        assertEquals("Request{method=PATCH, url=http://a.blazemeter.com/api/v4/tests/testId, tag=null}", emul.getRequests().get(0));
        String logs = logger.getLogs().toString();
        assertEquals(logs, 452, logs.length());
        assertTrue(logs, logs.contains("Update single test id=testId filename=test.yaml"));
        assertTrue(logs, logs.contains("Update single test id=testId data={\"configuration\":{\"testMode\":\"script\",\"filename\":\"test.yaml\",\"scriptType\":\"taurus\"}}"));
    }

    @Test
    public void testValidateFiles() throws IOException {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseStartSingleTest());

        SingleTest test = new SingleTest(emul, "testId", "testName", "functionalApi");
        List<String> files = new ArrayList<>();
        files.add("test.yaml");
        files.add("test.yaml");
        test.validateFiles(files);

        assertEquals(1, emul.getRequests().size());
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/tests/testId/validate, tag=null}", emul.getRequests().get(0));
        String logs = logger.getLogs().toString();
        assertEquals(logs, 468, logs.length());
        assertTrue(logs, logs.contains("Validate files in single test id=testId files=[test.yaml, test.yaml]"));
        assertTrue(logs, logs.contains("Validate single test id=testId data={\"files\":[{\"fileName\":\"test.yaml\"},{\"fileName\":\"test.yaml\"}]}"));
    }

    @Test
    public void testValidate() throws IOException {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseStartSingleTest());

        SingleTest test = new SingleTest(emul, "testId", "testName", "functionalApi");
        test.validate("{data}");

        assertEquals(1, emul.getRequests().size());
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/tests/testId/validate, tag=null}", emul.getRequests().get(0));
        String logs = logger.getLogs().toString();
        assertEquals(logs, 343, logs.length());
        assertTrue(logs, logs.contains("Validate single test id=testId data={data}"));
    }

    @Test
    public void testValidations() throws IOException {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(generateResponseValidations("test.yaml", 100, ""));

        SingleTest test = new SingleTest(emul, "testId", "testName", "functionalApi");
        JSONArray validations = test.validations();

        assertEquals(100, validations.getJSONObject(0).getInt("status"));
        assertEquals("test.yaml", validations.getJSONObject(0).getString("fileName"));

        assertEquals(1, emul.getRequests().size());
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/tests/testId/validations, tag=null}", emul.getRequests().get(0));
        String logs = logger.getLogs().toString();
        assertEquals(logs, 229, logs.length());
        assertTrue(logs, logs.contains("Get validations for single test id=testId"));
    }

    public static String generateResponseStartSingleTest() {
        JSONObject masterResponse = new JSONObject();
        masterResponse.put("id", "responseMasterId");
        masterResponse.put("name", "responseMasterName");

        JSONObject response = new JSONObject();
        response.put("result", masterResponse);
        return response.toString();
    }

    private void checkTest(SingleTest test) {
        Master master = test.getMaster();
        assertEquals("responseMasterId", master.getId());
        assertEquals("responseMasterName", master.getName());
        assertEquals(Session.UNDEFINED, test.getSignature());
    }

    @Test
    public void testFromJSON() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject configuration = new JSONObject();
        configuration.put("type", "http");

        JSONObject object = new JSONObject();
        object.put("id", "testId");
        object.put("name", "testName");
        object.put("configuration", configuration);

        SingleTest test = SingleTest.fromJSON(emul, object);
        assertEquals("testId", test.getId());
        assertEquals("testName", test.getName());
        assertEquals("http", test.getTestType());
    }

    @Test
    public void testGetSingleTest() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        emul.addEmul(generateResponseGetSingleTest());

        SingleTest test = SingleTest.getSingleTest(emul, "testId");
        assertEquals("testId", test.getId());
        assertEquals("Single_testName", test.getName());
        assertEquals("http", test.getTestType());

        assertEquals(1, emul.getRequests().size());
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/tests/testId, tag=null}", emul.getRequests().get(0));

        String logs = logger.getLogs().toString();
        assertEquals(logs, 222, logs.length());
        assertTrue(logs, logs.contains("Get Single Test id=testId"));
    }

    public static String generateResponseGetSingleTest() {
        return generateResponseGetSingleTest("http");
    }

    public static String generateResponseGetSingleTest(String testType) {
        JSONObject configuration = new JSONObject();
        configuration.put("type", testType);

        JSONObject result = new JSONObject();
        result.put("id", "testId");
        result.put("name", "Single_testName");
        result.put("configuration", configuration);

        JSONObject response = new JSONObject();
        response.put("result", result);
        return response.toString();
    }

    public static String generateResponseValidations(String fileName, int status, String errors) {
        JSONObject validation = new JSONObject();
        validation.put("status", status);
        validation.put("fileName", fileName);
        JSONArray errorsArray = new JSONArray();
        if (!errors.isEmpty()) {
            errorsArray.add(errors);
        }
        validation.put("errors", errorsArray);

        JSONArray result = new JSONArray();
        result.add(validation);

        JSONObject response = new JSONObject();
        response.put("result", result);
        return response.toString();
    }
}