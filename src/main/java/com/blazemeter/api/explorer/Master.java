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

import com.blazemeter.api.explorer.base.BZAObject;
import com.blazemeter.api.utils.BlazeMeterUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class Master extends BZAObject {

    public Master(BlazeMeterUtils utils, String id, String name) {
        super(utils, id, name);
    }

    /**
     * @return public link to the report
     */
    public String getPublicReport() throws IOException {
        logger.info("Get link to public report for master id=" + getId());
        String uri = utils.getAddress() + String.format("/api/v4/masters/%s/public-token", encode(getId()));
        JSONObject request = new JSONObject();
        request.put("publicToken", "None");
        JSONObject response = utils.execute(utils.createPost(uri, request.toString()));
        return utils.getAddress() + String.format("/app/?public-token=%s#/masters/%s/summary",
                extractPublicToken(response.getJSONObject("result")), getId());
    }

    /**
     * @return junit report as a string
     */
    public String getJUnitReport() throws IOException {
        logger.info("Get JUnit report for master id=" + getId());
        String uri = utils.getAddress() + String.format("/api/v4/masters/%s/reports/thresholds?format=junit", encode(getId()));
        return utils.executeRequest(utils.createGet(uri));
    }

    /**
     * @return list of Sessions Id
     */
    public List<Session> getSessions() throws IOException {
        logger.info("Get list of sessions for master id=" + getId());
        String uri = utils.getAddress() + String.format("/api/v4/masters/%s/sessions", encode(getId()));
        List<Session> sessions = new ArrayList<>();
        JSONObject response = utils.execute(utils.createGet(uri));
        JSONObject result = response.getJSONObject("result");
        JSONArray sessionsArray = result.getJSONArray("sessions");
        for (int i = 0; i < sessionsArray.size(); i++) {
            JSONObject so = sessionsArray.getJSONObject(i);
            sessions.add(Session.fromJSON(this.utils, so));
        }
        return sessions;
    }

    /**
     * Posts properties to master.
     */
    public void postProperties(String properties) {
        try {
            List<Session> sessions = this.getSessions();
            this.postProperties(properties, sessions);
        } catch (IOException ioe) {
            logger.error("Failed to get sessions for master = " + this.id, ioe);
        }

    }

    private void postProperties(String properties, List<Session> sessions) {
        JSONArray propertiesArray = null;
        try {
            propertiesArray = convertProperties(properties);
        } catch (Exception e) {
            logger.error("Failed to convert properties from String to JSONArray ", e);
            return;
        }
        for (Session s : sessions) {
            try {
                s.postProperties(propertiesArray);
            } catch (Exception e) {
                logger.error("Failed to send properties for session = " + s.getId());
            }
        }
    }

    public JSONArray stop() throws IOException {
        logger.info("Stop master id=" + getId());
        String uri = utils.getAddress() + String.format("/api/v4/masters/%s/stop", encode(getId()));
        RequestBody emptyBody = RequestBody.create(null, new byte[0]);
        return utils.execute(utils.createPost(uri, emptyBody)).getJSONArray("result");
    }

    public JSONArray terminate() throws IOException {
        logger.info("Terminate master id=" + getId());
        String uri = utils.getAddress() + String.format("/api/v4/masters/%s/terminate", encode(getId()));
        RequestBody emptyBody = RequestBody.create(null, new byte[0]);
        return utils.execute(utils.createPost(uri, emptyBody)).getJSONArray("result");
    }

    /**
     * @return master status code
     */
    public int getStatus() throws IOException {
        logger.info("Get master status id=" + getId());
        String uri = utils.getAddress() + String.format("/api/v4/masters/%s/status?events=false", encode(getId()));
        JSONObject result = utils.execute(utils.createGet(uri)).getJSONObject("result");
        return result.getInt("progress");
    }

    /**
     * @return summary as JSON object
     */
    public JSONObject getSummary() throws IOException {
        logger.info("Get summary for master id=" + getId());
        String uri = utils.getAddress() + String.format("/api/v4/masters/%s/reports/main/summary", encode(getId()));
        JSONObject result = utils.execute(utils.createGet(uri)).getJSONObject("result");
        JSONObject summary = result.getJSONArray("summary").getJSONObject(0);
        return extractSummary(summary);
    }

    /**
     * @return functional report as JSON object
     */
    public JSONObject getFunctionalReport() throws IOException {
        logger.info("Get functional report for master id=" + getId());
        String uri = utils.getAddress() + String.format("/api/v4/masters/%s", encode(getId()));
        JSONObject result = utils.execute(utils.createGet(uri)).getJSONObject("result");
        return result.has("functionalSummary") ? result.getJSONObject("functionalSummary") : new JSONObject();
    }

    /**
     * @return note which was applied to master if request was successful
     */
    public String postNotes(String note) throws IOException {
        logger.info("Post notes to master id=" + getId());
        String uri = utils.getAddress() + String.format("/api/v4/masters/%s", encode(getId()));
        // Hack to escape '\r\n' chars..
        String noteEscape = StringEscapeUtils.escapeJson("{'note':'" + note + "'}");
        RequestBody body = RequestBody.create(MediaType.parse("text/plain; charset=ISO-8859-1"), noteEscape);
        JSONObject result = utils.execute(utils.createPatch(uri, body)).getJSONObject("result");
        return result.getString("note");
    }

    /**
     * @return ci-status as JSONObject
     */
    public JSONObject getCIStatus() throws IOException {
        logger.info("Get CI status for master id=" + getId());
        String uri = utils.getAddress() + String.format("/api/v4/masters/%s/ci-status", encode(getId()));
        return utils.execute(utils.createGet(uri)).getJSONObject("result");
    }

    private String extractPublicToken(JSONObject result) {
        return result.getString("publicToken");
    }

    private JSONObject extractSummary(JSONObject sumserv) {
        JSONObject summary = new JSONObject();
        summary.put("avg", Math.round(sumserv.getDouble("avg") * 100.0) / 100.0);
        summary.put("min", sumserv.getInt("min"));
        summary.put("max", sumserv.getInt("max"));
        summary.put("tp90", sumserv.getInt("tp90"));
        summary.put("errorPercentage", Math.round((sumserv.getDouble("failed") / sumserv.getDouble("hits") * 100) * 100) / 100);
        int hits = sumserv.getInt("hits");
        double last = sumserv.getDouble("last");
        double first = sumserv.getDouble("first");
        summary.put("hits", sumserv.getInt("hits"));
        summary.put("avgthrpt", Math.round(hits / (last - first) * 100.0) / 100.0);
        return summary;
    }

    public static Master fromJSON(BlazeMeterUtils utils, JSONObject obj) {
        return new Master(utils, obj.getString("id"), obj.getString("name"));
    }

    public static JSONArray convertProperties(String properties) throws Exception {
        JSONArray propsArray = new JSONArray();
        List<String> propList = Arrays.asList(properties.split(","));
        for (String s : propList) {
            JSONObject prop = new JSONObject();
            List<String> pr = Arrays.asList(s.split("="));
            if (pr.size() > 1) {
                prop.put("key", pr.get(0).trim());
                prop.put("value", pr.get(1).trim());
            }
            propsArray.add(prop);
        }
        return propsArray;
    }

    /**
     * Waits until test will be over on server
     *
     * @throws InterruptedException IOException
     */
    public void waitForFinish() throws InterruptedException, IOException {
        long lastPrint = 0;
        while (true) {
            Thread.sleep(10000);
            if (getStatus() == 140) {
                return;
            }
            long start = Calendar.getInstance().getTime().getTime();
            long now = Calendar.getInstance().getTime().getTime();
            long diffInSec = (now - start) / 1000;
            if (now - lastPrint > 60000) {
                logger.info("BlazeMeter test# , masterId # " + getId() + " running from " + start + " - for " + diffInSec + " seconds");
                lastPrint = now;
            }
            if (Thread.interrupted()) {
                logger.info("Job was stopped by user");
                throw new InterruptedException("Job was stopped by user");
            }
        }
    }

}
