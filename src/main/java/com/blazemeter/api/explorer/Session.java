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

import java.io.IOException;

public class Session extends BZAObject {

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
     *
     * @return session in JSONObject
     */
    public JSONObject sendData(JSONObject data) throws IOException {
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
     *
     * @return if properties were send correctly(server's response contains the same properties)
     */
    public void properties(JSONArray properties) throws IOException {
        String uri = utils.getAddress() + String.format("/api/v4/sessions/%s/properties?target=all", encode(getId()));
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                properties.toString());
        utils.execute(utils.createPost(uri, body));
    }

    /**
     * Stop anonymous session
     */
    public void stopAnonymous() throws IOException {
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

    public static Session fromJSON(BlazeMeterUtils utils, String testId, String signature, JSONObject session) {
        return new Session(utils, session.getString("id"), session.getString("name"), session.getString("userId"), testId, signature);
    }
}
