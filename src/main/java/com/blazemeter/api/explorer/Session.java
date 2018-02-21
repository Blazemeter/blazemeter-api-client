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

package com.blazemeter.api.explorer;


import com.blazemeter.api.explorer.base.BZAObject;
import com.blazemeter.api.utils.BlazeMeterUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Each session belongs to some Master object.
 */
public class Session extends BZAObject {

    public static final String UNDEFINED = "undefined";

    private final String userId;
    private final String testId;
    private final String signature;

    public Session(BlazeMeterUtils utils, String id, String name, String userId, String testId, String signature) {
        super(utils, id, name);
        this.userId = userId;
        this.testId = testId;
        this.signature = signature;
    }

    /**
     * Send test json data for the report
     * POST request to 'https://a.blazemeter.com/submit.php?session_id={sessionId}&signature={signature}&test_id={testId}&user_id={userId}'
     * @return session in JSONObject
     */
    public JSONObject sendData(JSONObject data) throws IOException {
        logger.info("Send data to session id=" + getId());
        String uri = utils.getDataAddress() +
                String.format("/submit.php?session_id=%s&signature=%s&test_id=%s&user_id=%s",
                        getId(), signature, testId, userId);
        uri += "&pq=0&target=labels_bulk&update=1"; //TODO: % self.kpi_target
        String dataStr = data.toString();
        logger.debug("Sending active test data: " + dataStr);
        JSONObject response = utils.execute(utils.createPost(uri, dataStr));
        return response.getJSONObject("result").getJSONObject("session");
    }


    /**
     * Send properties to test session
     * if properties were send correctly(server's response contains the same properties)
     * POST request to 'https://a.blazemeter.com/api/v4/sessions/{sessionId}/properties?target=all'
     */
    public void postProperties(String properties) throws IOException, InterruptedException {
        if (StringUtils.isBlank(properties)) {
            logger.warn("Properties are empty, won't be sent to session = " + getId());
            return;
        }
        postProperties(convertProperties(properties));
    }


    /**
     * Send properties to Session.
     * POST request to 'https://a.blazemeter.com/api/v4/sessions/{sessionId}/properties?target=all'
     * @param properties - in JSONArray. For convert from String use @link com.blazemeter.api.explorer.Session#convertProperties(java.lang.String)
     */
    void postProperties(JSONArray properties) throws IOException, InterruptedException {
        logger.info("Post properties to session id=" + getId());
        String uri = utils.getAddress() + String.format("/api/v4/sessions/%s/properties?target=all", encode(getId()));
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                properties.toString());
        utils.execute(utils.createPost(uri, body));
    }


    /**
     * GET request to 'https://a.blazemeter.com/api/v4/sessions/{sessionId}/reports/logs'
     * @return url for downloading jtl report
     */
    public String getJTLReport() throws IOException {
        logger.info("Get JTL report for session id=" + getId());
        String uri = utils.getAddress() + String.format("/api/v4/sessions/%s/reports/logs", encode(getId()));
        JSONObject o = utils.execute(utils.createGet(uri));
        return extractDataUrl(o);
    }

    /**
     * Stop anonymous session
     * POST request to 'https://a.blazemeter.com/api/v4/sessions/{sessionId}/terminate-external'
     */
    public void terminateExternal() throws IOException {
        logger.info("Terminate external session id=" + getId());
        String uri = utils.getAddress() + String.format("/api/v4/sessions/%s/terminate-external", encode(getId()));
        JSONObject data = new JSONObject();
        data.put("signature", signature);
        data.put("testId", testId);
        data.put("sessionId", getId());
        utils.executeRequest(utils.createPost(uri, data.toString()));
    }

    public String getUserId() {
        return userId;
    }

    public String getTestId() {
        return testId;
    }

    public String getSignature() {
        return signature;
    }

    private String extractDataUrl(JSONObject o) {
        JSONObject result = o.getJSONObject("result");
        JSONArray data = result.getJSONArray("data");
        for (int i = 0; i < data.size(); i++) {
            String title = data.getJSONObject(i).getString("title");
            if ("Zip".equals(title)) {
                return data.getJSONObject(i).getString("dataUrl");
            }
        }
        return null;
    }

    /**
     * Converts String of properties "v=o,b=i" to JSONArray
     * which can be posted to Session.
     */
    public static JSONArray convertProperties(String properties) {
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
     * Creates Session from JSON which is received from server.
     */
    public static Session fromJSON(BlazeMeterUtils utils, JSONObject so) {
        return new Session(utils, so.getString("id"), so.getString("name"),
                so.getString("userId"), so.getString("testId"), Session.UNDEFINED);
    }

    public static Session fromJSON(BlazeMeterUtils utils, String testId, String signature, JSONObject session) {
        return new Session(utils, session.getString("id"), session.getString("name"), session.getString("userId"), testId, signature);
    }
}
