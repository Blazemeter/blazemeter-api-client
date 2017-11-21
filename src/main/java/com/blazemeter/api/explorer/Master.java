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
import java.util.List;

public class Master extends BZAObject {

    private String MASTER_ID = "/api/v4/masters/{id}";

    public Master(BlazeMeterUtils utils, String id, String name) {
        super(utils, id, name);
    }

    /**
     * @return public link to the report
     */
    public String publicreport() throws IOException {
        String uri = utils.getAddress() + String.format("/api/v4/masters/%s/public-token", encode(getId()));
        JSONObject obj = new JSONObject();
        obj.put("publicToken", "None");
        JSONObject response = utils.execute(utils.createPost(uri, obj.toString()));

        return utils.getAddress() + String.format("/app/?public-token=%s#/masters/%s/summary",
                extractPublicToken(response.getJSONObject("result")), getId());
    }

    /**
     * @return junit report as a string
     */
    public String junitReport() throws IOException {
        String uri = utils.getAddress() + String.format("/api/v4/masters/%s/reports/thresholds?format=junit", encode(getId()));
        return utils.executeRequest(utils.createGet(uri));
    }

    /**
     * @return junit report as a string
     */
    public List<String> sessionIds() throws IOException {
        String uri = utils.getAddress() + String.format("/api/v4/masters/%s/sessions", encode(getId()));
        List<String> sessionIds = new ArrayList<>();
        JSONObject response = utils.execute(utils.createGet(uri));
        JSONObject result = response.getJSONObject("result");
        JSONArray sessions = result.getJSONArray("sessions");
        int sessionsLength = sessions.size();
        for (int i = 0; i < sessionsLength; i++) {
            sessionIds.add(sessions.getJSONObject(i).getString("id"));
        }
        return sessionIds;
    }

    /**
     * @return JSONObject
     */

    public JSONArray stop() throws IOException {
        String uri = utils.getAddress() + String.format("/api/v4/masters/%s/stop", encode(getId()));
        RequestBody emptyBody = RequestBody.create(null, new byte[0]);
        return utils.execute(utils.createPost(uri, emptyBody)).getJSONArray("result");
    }

    /**
     * @return JSONObject
     */
    public JSONArray terminate() throws IOException {
        String uri = utils.getAddress() + String.format("/api/v4/masters/%s/terminate", getId());
        RequestBody emptyBody = RequestBody.create(null, new byte[0]);
        return utils.execute(utils.createPost(uri, emptyBody)).getJSONArray("result");
    }

    /**
     * @return master status code
     */
    public int status() throws IOException {
        String uri = utils.getAddress() + String.format("/api/v4/masters/%s/status?events=false", getId());
        JSONObject r = utils.execute(utils.createGet(uri)).getJSONObject("result");
        return r.getInt("progress");
    }

    /**
     * @return summary as JSON object
     */
    public JSONObject summary() throws IOException {
        String uri = utils.getAddress() + String.format("/api/v4/masters/%s/reports/main/summary", getId());
        JSONObject r = utils.execute(utils.createGet(uri)).getJSONObject("result");
        JSONObject sumserv = r.getJSONArray("summary").getJSONObject(0);
        JSONObject summary = extractSummary(sumserv);
        return summary;
    }

    /**
     * @return functional report as JSON object
     */
    public JSONObject funcReport() throws IOException {
        String uri = utils.getAddress() + String.format(MASTER_ID, getId());
        JSONObject r = utils.execute(utils.createGet(uri)).getJSONObject("result");
        return r.has("functionalSummary") ? r.getJSONObject("functionalSummary") : new JSONObject();
    }

    /**
     * @return note which was applied to master if request was successful
     */
    public String note(String note) throws IOException {
        String uri = utils.getAddress() + String.format(MASTER_ID, getId());
        String noteEsc = StringEscapeUtils.escapeJson("{'note':'" + note + "'}");
        RequestBody body = RequestBody.create(MediaType.parse("text/plain; charset=ISO-8859-1"), noteEsc);

        JSONObject r = utils.execute(utils.createPatch(uri, body)).getJSONObject("result");
        return r.getString("note");
    }

    /**
     * @return ci-status as JSONObject
     */
    public JSONObject cistatus() throws IOException {
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
}
