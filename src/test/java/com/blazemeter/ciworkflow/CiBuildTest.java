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

import com.blazemeter.api.explorer.Master;
import com.blazemeter.api.explorer.MasterTest;
import com.blazemeter.api.explorer.test.*;
import com.blazemeter.api.logging.LoggerTest;
import com.blazemeter.api.logging.UserNotifierTest;
import com.blazemeter.api.utils.BlazeMeterUtilsEmul;
import com.blazemeter.api.utils.BlazeMeterUtilsSlowEmul;
import net.sf.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_ADDRESS;
import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_DATA_ADDRESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CiBuildTest {
    @Before
    public void setUp() throws Exception {
        System.setProperty("bzm.checkTimeout", "1000");
        System.setProperty("bzm.minute", "900");
    }

    @After
    public void tearDown() throws Exception {
        System.setProperty("bzm.checkTimeout", "10000");
        System.setProperty("bzm.minute", "60000");
    }

    @Test
    public void testWaitForFinish() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject result = new JSONObject();
        result.put("progress", 70);

        JSONObject response = new JSONObject();
        response.put("result", result);
        emul.addEmul(response.toString());

        result = new JSONObject();
        result.put("progress", 140);
        response.put("result", result);
        emul.addEmul(response.toString());

        Master master = new Master(emul, "id", "name");
        CiPostProcess postProcess = new CiPostProcess(false, false, "", "", "", notifier, logger);
        CiBuild ciBuild = new CiBuild(emul, "id", "", "", postProcess);
        ciBuild.waitForFinish(master);
        assertEquals(2, emul.getRequests().size());
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/id/status?events=false, tag=null}", emul.getRequests().get(0));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/id/status?events=false, tag=null}", emul.getRequests().get(1));
        assertEquals(363, logger.getLogs().length());
    }

    @Test
    public void testExecute() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        setEmulator(emul);
        CiPostProcess postProcess = new CiPostProcess(false, false, "", "", "", notifier, logger);
        CiBuild ciBuild = new CiBuild(emul, "testId", "1=2", "i", postProcess);
        ciBuild.execute();
        assertEquals(11, emul.getRequests().size());
        int i = 0;
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/tests/testId, tag=null}", emul.getRequests().get(i++));
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/tests/testId/start, tag=null}", emul.getRequests().get(i++));
        assertEquals("Request{method=POST, url=http://a.blazemeter.com/api/v4/masters/responseMasterId/public-token, tag=null}", emul.getRequests().get(i++));
        assertEquals("http://a.blazemeter.com/app/?public-token=x1x1x1x1x1x1x1x11x1x1x1#/masters/responseMasterId/summary",
                ciBuild.getPublicReport());
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/responseMasterId/status?events=false, tag=null}", emul.getRequests().get(i++));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/responseMasterId/status?events=false, tag=null}", emul.getRequests().get(i++));
        assertEquals("Request{method=PATCH, url=http://a.blazemeter.com/api/v4/masters/responseMasterId, tag=null}", emul.getRequests().get(i++));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/responseMasterId/status?events=false, tag=null}", emul.getRequests().get(i++));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/responseMasterId/status?events=false, tag=null}", emul.getRequests().get(i++));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/responseMasterId/ci-status, tag=null}", emul.getRequests().get(i++));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/responseMasterId, tag=null}", emul.getRequests().get(i++));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/responseMasterId/reports/main/summary, tag=null}", emul.getRequests().get(i));
        String logs = logger.getLogs().toString();
        assertTrue(logs, logs.contains("Get Single Test id=testId"));
        assertTrue(logs, logs.contains("Start single test id=testId"));
        assertTrue(logs, logs.contains("Get link to public report for master id=responseMasterId"));
        assertTrue(logs, logs.contains("Post notes to master id=responseMasterId"));
        assertTrue(logs, logs.contains("Response: {\"result\":{\"progress\":70}}"));
        assertTrue(logs, logs.contains("Response: {\"result\":{\"progress\":140}}"));
        assertEquals(logs, 2536, logger.getLogs().length());
    }


    private void setEmulator(BlazeMeterUtilsEmul emul) {
        emul.addEmul(SingleTestTest.generateResponseGetSingleTest());
        emul.addEmul(SingleTestTest.generateResponseStartSingleTest());

        String token = "x1x1x1x1x1x1x1x11x1x1x1";
        emul.addEmul(MasterTest.generateResponseGetPublicToken(token));
        emul.addEmul(MasterTest.generateResponseGetStatus(0));
        emul.addEmul(MasterTest.generateResponseGetStatus(90));
        emul.addEmul(MasterTest.generateResponsePostNote());

        emul.addEmul(MasterTest.generateResponseGetStatus(70));
        emul.addEmul(MasterTest.generateResponseGetStatus(140));
        emul.addEmul(MasterTest.generateResponseGetCIStatus());
    }

    @Test
    public void testExecuteFailStart() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        CiPostProcess postProcess = new CiPostProcess(false, false, "", "", "", notifier, logger);
        CiBuild ciBuild = new CiBuild(emul, "id", "1=2", "", postProcess);
        BuildResult result = ciBuild.execute();
        assertEquals(BuildResult.FAILED, result);

        assertEquals(1, emul.getRequests().size());
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/tests/id, tag=null}", emul.getRequests().get(0));
        String logs = logger.getLogs().toString();
        assertEquals(logs, 272, logs.length());
        assertTrue(logs, logs.contains("Caught exception. Set Build status [FAILED]. Reason is:"));
    }

    @Test
    public void testGetters() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        CiPostProcess postProcess = new CiPostProcess(false, false, "", "", "", notifier, logger);
        CiBuild ciBuild = new CiBuild(emul, "id", "props", "notes", postProcess);

        assertEquals("id", ciBuild.getTestId());
        assertEquals(emul, ciBuild.getUtils());
        assertEquals("props", ciBuild.getProperties());
        assertEquals("notes", ciBuild.getNotes());
        assertNull(ciBuild.getPublicReport());
        ciBuild.setPublicReport("report");
        assertEquals("report", ciBuild.getPublicReport());

        assertNotNull(ciBuild.getCiPostProcess());
    }

    @Test
    public void testStartFailDetect() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(TestDetectorTest.generateResponseTestNotFound());
        emul.addEmul(TestDetectorTest.generateResponseCollectionNotFound());

        CiBuild ciBuild = new CiBuild(emul, "id", "props", "notes", null);
        Master master = ciBuild.start();
        assertNull(master);
        assertEquals(2, emul.getRequests().size());
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/tests/id, tag=null}", emul.getRequests().get(0));
        assertEquals("Request{method=GET, url=http://a.blazemeter.com/api/v4/multi-tests/id, tag=null}", emul.getRequests().get(1));

        String logs = logger.getLogs().toString();
        assertEquals(logs, 757, logs.length());
        assertTrue(logs, logs.contains("Failed to detect test type. Test with id=id not found."));
    }

    @Test
    public void testExecuteFailDetect() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(TestDetectorTest.generateResponseTestNotFound());
        emul.addEmul(TestDetectorTest.generateResponseCollectionNotFound());

        CiBuild ciBuild = new CiBuild(emul, "id", "props", "notes", null);
        BuildResult result = ciBuild.execute();
        assertEquals(BuildResult.FAILED, result);
        assertEquals(2, emul.getRequests().size());

        String logs = logger.getLogs().toString();
        assertEquals(logs, 785, logs.length());
        assertTrue(logs, logs.contains("Set Build status [FAILED]."));
    }


    @Test
    public void testInterrupt() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        CiBuild ciBuild = new CiBuild(emul, "id", "props", "notes", null);
        Master master = new Master(emul, "id", "name");

        emul.addEmul(MasterTest.generateResponseGetStatus(70));
        emul.addEmul(MasterTest.generateResponseTerminateMaster());
        boolean interrupt = ciBuild.interrupt(master);
        assertFalse(interrupt);

        emul.addEmul(MasterTest.generateResponseGetStatus(110));
        emul.addEmul(MasterTest.generateResponseStopMaster());
        interrupt = ciBuild.interrupt(master);
        assertTrue(interrupt);
    }

    @Test
    public void testStartFailed() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        CiBuild ciBuild = new CiBuild(emul, "id", "props", "notes", null) {
            @Override
            public Master start() throws IOException {
                return null;
            }
        };
        BuildResult result = ciBuild.execute();

        assertEquals(BuildResult.FAILED, result);
        String logs = notifier.getLogs().toString();
        assertTrue(logs, logs.contains("Set Build status [FAILED]."));
    }

    @Test
    public void testSkipInitStateFailed() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        CiBuild ciBuild = new CiBuild(emul, "id", "props", "notes", null);
        Master master = new Master(emul, "id", "name");
        ciBuild.skipInitState(master);

        String logs = logger.getLogs().toString();
        assertTrue(logs, logs.contains("Failed to skip INIT state"));
    }

    @Test
    public void testStartTest() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject response = new JSONObject();
        response.put("result", MultiTestTest.generateResponseStartMultiTest());
        emul.addEmul(response.toString());
        emul.addEmul(MasterTest.generateResponseGetPublicToken("multi-test-public-token"));
        emul.addEmul(MasterTest.generateResponseGetStatus(30));

        CiBuild ciBuild = new CiBuild(emul, "id", "props", "", null);
        MultiTest multiTest = new MultiTest(emul, "123", "multi-test-name", "multi");

        ciBuild.startTest(multiTest);

        String log = logger.getLogs().toString();
        assertEquals(log, emul.getRequests().get(0), "Request{method=POST, url=http://a.blazemeter.com/api/v4/multi-tests/123/start, tag=null}");
        assertEquals(log, emul.getRequests().get(3), "Request{method=GET, url=http://a.blazemeter.com/api/v4/sessions?masterId=responseMasterId, tag=null}");
        assertEquals(log, 970, log.length());
    }

    @Test
    public void testGeneratePublicToken() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        CiBuild ciBuild = new CiBuild(emul, "id", "props", "", null);
        ciBuild.generatePublicReport(null);

        String log = logger.getLogs().toString();
        assertTrue(log, log.contains("Cannot get public token"));

        String userLog = notifier.getLogs().toString();
        assertTrue(userLog, userLog.contains("Cannot get public token"));
    }

    @Test
    public void testPostNotes() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        CiBuild ciBuild = new CiBuild(emul, "id", "props", "notes", null);
        ciBuild.postNotes(null);

        String log = logger.getLogs().toString();
        assertTrue(log, log.contains("Cannot post notes"));

        String userLog = notifier.getLogs().toString();
        assertTrue(userLog, userLog.contains("Cannot post notes"));
    }

    @Test
    public void testGeneratePublicReportInterrupt() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        final BlazeMeterUtilsSlowEmul emul = new BlazeMeterUtilsSlowEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        final Throwable ex[] = {null};
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    CiBuild ciBuild = new CiBuild(emul, "id", "props", "notes", null);
                    Master master = new Master(emul, "id", "name");
                    ciBuild.generatePublicReport(master);
                } catch (Throwable e) {
                    ex[0] = e;
                }
            }
        };
        t.start();
        t.interrupt();
        t.join();

        String logs = logger.getLogs().toString();
        assertTrue(logs, logs.contains("Interrupt while get public report"));
        assertNotNull(ex[0]);
        assertEquals("Interrupted emul", ex[0].getMessage());
    }

    @Test
    public void testPostNotesInterrupt() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        final BlazeMeterUtilsSlowEmul emul = new BlazeMeterUtilsSlowEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        final Throwable ex[] = {null};
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    CiBuild ciBuild = new CiBuild(emul, "id", "props", "notes", null);
                    Master master = new Master(emul, "id", "name");
                    ciBuild.postNotes(master);
                } catch (Throwable e) {
                    ex[0] = e;
                }
            }
        };
        t.start();
        t.interrupt();
        t.join();

        String logs = logger.getLogs().toString();
        assertTrue(logs, logs.contains("Interrupt while post notes"));
        assertNotNull(ex[0]);
        assertEquals("Interrupted emul", ex[0].getMessage());
    }

    @Test
    public void testSkipInitStateInterrupt() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        final BlazeMeterUtilsSlowEmul emul = new BlazeMeterUtilsSlowEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        final Throwable ex[] = {null};
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    CiBuild ciBuild = new CiBuild(emul, "id", "props", "notes", null);
                    Master master = new Master(emul, "id", "name");
                    ciBuild.skipInitState(master);
                } catch (Throwable e) {
                    ex[0] = e;
                }
            }
        };
        t.start();
        t.interrupt();
        t.join();

        String logs = logger.getLogs().toString();
        assertTrue(logs, logs.contains("Caught InterruptedException while skip InitState"));
        assertNotNull(ex[0]);
        assertEquals("sleep interrupted", ex[0].getMessage());
    }

    @Test(timeout = 30000)
    public void testStartTestInterrupt() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        final BlazeMeterUtilsSlowEmul emul = new BlazeMeterUtilsSlowEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject response = new JSONObject();
        response.put("result", MultiTestTest.generateResponseStartMultiTest());
        emul.addEmul(response.toString());
        emul.addEmul(MasterTest.generateResponseGetStatus(120));
        emul.addEmul(MasterTest.generateResponseStopMaster());

        final CiPostProcess ciPostProcess = new CiPostProcess(false, false, "re", "", "", notifier, logger) {
            @Override
            public BuildResult execute(Master master) {
                return BuildResult.FAILED;
            }
        };

        final Throwable ex[] = {null};
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    MultiTest multiTest = new MultiTest(emul, "123", "multi-test-name", "multi");
                    CiBuild ciBuild = new CiBuild(emul, "id", "props", "notes", ciPostProcess);
                    ciBuild.startTest(multiTest);
                } catch (Throwable e) {
                    ex[0] = e;
                }
            }
        };
        t.start();
        // we should skip the first request (start test)
        Thread.currentThread().sleep(7000);
        t.interrupt();
        t.join();

        String logs = logger.getLogs().toString();
        assertTrue(logs, logs.contains("Interrupt master"));
        assertNotNull(ex[0]);
        assertEquals("Interrupt master", ex[0].getMessage());
        assertEquals(logs, emul.getRequests().get(0), "Request{method=POST, url=http://a.blazemeter.com/api/v4/multi-tests/123/start, tag=null}");
        assertEquals(logs, emul.getRequests().get(1), "Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/responseMasterId/status?events=false, tag=null}");
        assertEquals(logs, emul.getRequests().get(2), "Request{method=POST, url=http://a.blazemeter.com/api/v4/masters/responseMasterId/stop, tag=null}");
        assertEquals(logs, 817, logs.length());
    }

    @Test
    public void testExecuteInterrupt() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        final BlazeMeterUtilsSlowEmul emul = new BlazeMeterUtilsSlowEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        final CiPostProcess ciPostProcess = new CiPostProcess(false, false, "re", "", "", notifier, logger) {
            @Override
            public BuildResult execute(Master master) {
                return BuildResult.FAILED;
            }
        };

        Thread t = new Thread() {
            @Override
            public void run() {
                CiBuild ciBuild = new CiBuild(emul, "id", "props", "notes", ciPostProcess);
                assertEquals(BuildResult.ABORTED, ciBuild.execute());
            }
        };
        t.start();
        t.interrupt();
        t.join();

        String logs = logger.getLogs().toString();
        assertTrue(logs, logs.contains("Caught exception. Set Build status [ABORTED]. Reason is: Interrupted emul"));
        String notifications = notifier.getLogs().toString();
        assertTrue(notifications, notifications.contains("Caught exception. Set Build status [ABORTED]."));
    }


    @Test(timeout = 30000)
    public void testwaitForFinishAndDoPostProcessInterrupt() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        final BlazeMeterUtilsSlowEmul emul = new BlazeMeterUtilsSlowEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(MasterTest.generateResponseGetStatus(120));
        emul.addEmul(MasterTest.generateResponseStopMaster());

        final CiPostProcess ciPostProcess = new CiPostProcess(false, false, "re", "", "", notifier, logger) {
            @Override
            public BuildResult execute(Master master) {
                return BuildResult.ABORTED;
            }
        };

        Thread t = new Thread() {
            @Override
            public void run() {
                Master master = new Master(emul, "id", "name");
                CiBuild ciBuild = new CiBuild(emul, "id", "props", "notes", ciPostProcess);
                try {
                    assertEquals(BuildResult.ABORTED, ciBuild.waitForFinishAndDoPostProcess(master));
                } catch (IOException e) {
                    fail();
                }
            }
        };
        t.start();
        t.interrupt();
        t.join();

        String logs = logger.getLogs().toString();
        assertTrue(logs, logs.contains("Caught InterruptedException, execute postProcess."));
        assertEquals(logs, emul.getRequests().get(0), "Request{method=GET, url=http://a.blazemeter.com/api/v4/masters/id/status?events=false, tag=null}");
        assertEquals(logs, emul.getRequests().get(1), "Request{method=POST, url=http://a.blazemeter.com/api/v4/masters/id/stop, tag=null}");
    }

    @Test
    public void testUploadAndUpdateTestFiles() throws Exception {
        LoggerTest logger = new LoggerTest();
        UserNotifierTest notifier = new UserNotifierTest();
        final BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        emul.addEmul(SingleTestTest.generateResponseGetSingleTest("jmeter")); // detect test type
        emul.addEmul(SingleTestTest.generateResponseGetSingleTest("jmeter")); // upload main file
        emul.addEmul(SingleTestTest.generateResponseGetSingleTest("jmeter")); // update main filename
        emul.addEmul(SingleTestTest.generateResponseGetSingleTest("jmeter")); // upload additional file

        String path = CiBuildTest.class.getResource("/test.yml").getPath();
        File file = new File(path);

        List<File> files = new ArrayList<>();
        files.add(file);

        CiBuild ciBuild = new CiBuild(emul, "id", file, files, "", "", null) {
            @Override
            protected Master startTest(AbstractTest test) throws IOException, InterruptedException {
                return new Master(emul, "12345", "54321");
            }
        };
        Master master = ciBuild.start();
        assertEquals("12345", master.getId());
        assertEquals("54321", master.getName());

        AbstractTest currentTest = ciBuild.getCurrentTest();
        assertEquals("testId", currentTest.getId());

        LinkedList<String> requests = emul.getRequests();
        assertEquals(4, requests.size());
        String logs = logger.getLogs().toString();

        assertEquals(logs, emul.getRequests().get(1), "Request{method=POST, url=http://a.blazemeter.com/api/v4/tests/testId/files, tag=null}");
        assertEquals(logs, emul.getRequests().get(2), "Request{method=PATCH, url=http://a.blazemeter.com/api/v4/tests/testId, tag=null}");
        assertEquals(logs, emul.getRequests().get(3), "Request{method=POST, url=http://a.blazemeter.com/api/v4/tests/testId/files, tag=null}");
    }
}
