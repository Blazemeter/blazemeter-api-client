package com.blazemeter.api.explorer;


import com.blazemeter.api.utils.BlazeMeterUtils;
import net.sf.json.JSON;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;

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
     * @return session in JSONObject
     */
    public JSONObject sendData(JSONObject data) throws IOException {
        String uri = utils.getDataAddress() +
                String.format("/submit.php?session_id=%s&signature=%s&test_id=%s&user_id=%s",
                        getId(), signature, testId, userId);
        uri += "&pq=0&target=labels_bulk&update=1"; //TODO: % self.kpi_target
        String dataStr = data.toString();
        logger.debug("Sending active test data: " + dataStr);
        JSONObject response = utils.queryObject(utils.createPost(uri, dataStr), 200);
        return response.getJSONObject("result").getJSONObject("session");
    }

    /**
     * Stop session for user token
     */
    public void stop() throws IOException {
        String uri = utils.getAddress() + String.format("/api/v4/sessions/%s/stop", getId());
        utils.query(utils.createPost(uri, ""), 202);
    }

    /**
     * Stop anonymous session
     */
    public void stopAnonymous() throws IOException {
        String uri = utils.getAddress() + String.format("/api/v4/sessions/%s/terminate-external", getId());
        JSONObject data = new JSONObject();
        data.put("signature", signature);
        data.put("testId", testId);
        data.put("sessionId", getId());
        utils.query(utils.createPost(uri, data.toString()), 200);
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
