package com.blazemeter.api.com.blazemeter.ciworkflow;

import com.blazemeter.api.explorer.Master;
import com.blazemeter.api.logging.LoggerTest;
import com.blazemeter.api.logging.UserNotifier;
import com.blazemeter.api.logging.UserNotifierTest;
import com.blazemeter.api.utils.BlazeMeterUtilsEmul;
import com.blazemeter.ciworkflow.BuildResult;
import com.blazemeter.ciworkflow.CiPostProcess;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Test;

import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_ADDRESS;
import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_DATA_ADDRESS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CiPostProcessTest {

    @Test
    public void downloadSummary_func() {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        CiPostProcess ciPostProcess = new CiPostProcess(false, false,
                "", "", "", logger);
        Master master = new Master(emul, "id", "name");

        JSONObject funcReportEmul = new JSONObject();
        funcReportEmul.put("testsCount", 1);
        funcReportEmul.put("requestsCount", 1);
        funcReportEmul.put("errorsCount", 1);
        funcReportEmul.put("assertions", new JSONObject());
        funcReportEmul.put("responseTime", new JSONObject());
        funcReportEmul.put("isFailed", true);
        funcReportEmul.put("failedCount", 1);
        funcReportEmul.put("failedPercentage", 100);

        JSONObject result = new JSONObject();
        result.put("functionalSummary", funcReportEmul);

        JSONObject response = new JSONObject();
        response.put("result", result);
        emul.addEmul(response.toString());

        try {
            JSONObject summary = ciPostProcess.downloadSummary(master);
            assertTrue(summary.size() == 8);
            assertTrue(summary.getInt("testsCount") == 1);
            assertTrue(summary.getInt("requestsCount") == 1);
            assertTrue(summary.getInt("errorsCount") == 1);

        } catch (InterruptedException e) {
            fail();
        } finally {
            emul.clean();
        }
    }

    @Test
    public void downloadSummary_agr() {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        CiPostProcess ciPostProcess = new CiPostProcess(false, false,
                "", "", "", logger);
        Master master = new Master(emul, "id", "name");
        JSONObject result = new JSONObject();
        JSONObject response = new JSONObject();
        response.put("result", result);
        emul.addEmul(response.toString());

        JSONObject summaryAgrEmul = new JSONObject();
        summaryAgrEmul.put("first", 1437397105);
        summaryAgrEmul.put("last", 1437397406);
        summaryAgrEmul.put("min", 0);
        summaryAgrEmul.put("max", 177);
        summaryAgrEmul.put("tp90", 2);
        summaryAgrEmul.put("tp90", 2);
        summaryAgrEmul.put("failed", 1236);
        summaryAgrEmul.put("hits", 2482);
        summaryAgrEmul.put("avg", 1.47);

        JSONArray sumArrray = new JSONArray();
        sumArrray.add(summaryAgrEmul);

        result = new JSONObject();
        result.put("summary", sumArrray);

        response = new JSONObject();
        response.put("result", result);
        emul.addEmul(response.toString());
        try {
            JSONObject summary = ciPostProcess.downloadSummary(master);
            assertTrue(summary.size() == 7);
            assertTrue(summary.has("hits"));
            assertTrue(summary.getInt("hits") == 2482);
        } catch (InterruptedException e) {
            fail();
        } finally {
            emul.clean();
        }
    }

    @Test
    public void errorsFailed() {

        JSONArray e = new JSONArray();
        JSONObject o = new JSONObject();
        o.put("code", 70404);
        e.add(o);

        LoggerTest logger = new LoggerTest();
        CiPostProcess ciPostProcess = new CiPostProcess(false, false,
                "", "", "", logger);
        assertTrue(ciPostProcess.errorsFailed(e));

        e = new JSONArray();
        o = new JSONObject();
        o.put("code", 0);
        e.add(o);

        assertTrue(ciPostProcess.errorsFailed(e));

        e = new JSONArray();
        o = new JSONObject();
        o.put("code", 111);
        e.add(o);

        assertFalse(ciPostProcess.errorsFailed(e));

        e = new JSONArray();
        assertFalse(ciPostProcess.errorsFailed(e));

    }

    @Test
    public void validateCIStatusSuccess() {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject result = new JSONObject();
        JSONArray e = new JSONArray();
        result.put("errors", e);
        JSONArray f = new JSONArray();
        result.put("failures", f);
        JSONObject o = new JSONObject();
        o.put("result", result);
        emul.addEmul(o.toString());
        CiPostProcess ciPostProcess = new CiPostProcess(false, false,
                "", "", "", logger);
        Master master = new Master(emul, "id", "name");
        BuildResult r = ciPostProcess.validateCiStatus(master);
        assertTrue(r.equals(BuildResult.SUCCESS));
        emul.clean();
    }

    @Test
    public void validateCIStatusFailure() {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject result = new JSONObject();
        JSONArray e = new JSONArray();
        result.put("errors", e);
        JSONArray fa = new JSONArray();
        JSONObject fo = new JSONObject();
        fo.put("code", 61000);
        fa.add(fo);
        result.put("failures", fa);
        JSONObject o = new JSONObject();
        o.put("result", result);
        emul.addEmul(o.toString());
        CiPostProcess ciPostProcess = new CiPostProcess(false, false,
                "", "", "", logger);
        Master master = new Master(emul, "id", "name");
        BuildResult r = ciPostProcess.validateCiStatus(master);
        assertTrue(r.equals(BuildResult.FAILED));
        emul.clean();

    }

    @Test
    public void validateCIStatus70404Failure() {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject result = new JSONObject();
        JSONArray ea = new JSONArray();
        JSONObject eo = new JSONObject();

        eo.put("code", 70404);
        ea.add(eo);
        result.put("errors", ea);

        JSONArray fa = new JSONArray();
        result.put("failures", fa);
        JSONObject o = new JSONObject();
        o.put("result", result);
        emul.addEmul(o.toString());
        CiPostProcess ciPostProcess = new CiPostProcess(false, false,
                "", "", "", logger);
        Master master = new Master(emul, "id", "name");
        BuildResult r = ciPostProcess.validateCiStatus(master);
        assertTrue(r.equals(BuildResult.FAILED));
        emul.clean();

    }

    @Test
    public void validateCIStatusError() {
        LoggerTest logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();
        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);

        JSONObject result = new JSONObject();
        JSONArray ea = new JSONArray();
        JSONObject eo = new JSONObject();

        eo.put("code", 111);
        ea.add(eo);
        result.put("errors", ea);

        JSONArray fa = new JSONArray();
        result.put("failures", fa);
        JSONObject o = new JSONObject();
        o.put("result", result);
        emul.addEmul(o.toString());
        CiPostProcess ciPostProcess = new CiPostProcess(false, false,
                "", "", "", logger);
        Master master = new Master(emul, "id", "name");
        BuildResult r = ciPostProcess.validateCiStatus(master);
        assertTrue(r.equals(BuildResult.ERROR));
        emul.clean();

    }

}
