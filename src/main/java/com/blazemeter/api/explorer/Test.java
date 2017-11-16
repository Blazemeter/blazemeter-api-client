package com.blazemeter.api.explorer;



import com.blazemeter.api.utils.BlazeMeterUtils;
import net.sf.json.JSONObject;

import java.io.IOException;

public class Test extends BZAObject {

    public static final String DEFAULT_TEST = "Default test";

    private Session session;
    private Master master;
    private String signature;
    private String reportURL;

    public Test(BlazeMeterUtils utils) {
        super(utils, "", "");
    }

    public Test(BlazeMeterUtils utils, String id, String name) {
        super(utils, id, name);
    }

    /**
     * Start External test for user token
     */
    public void startExternal() throws IOException {
        JSONObject result = sendStartTest(utils.getAddress() + String.format("/api/v4/tests/%s/start-external", getId()), 202);
        fillFields(result);
    }

    /**
     * Start Anonymous External test
     * @return public link to the report
     */
    public String startAnonymousExternal() throws IOException {
        JSONObject result = sendStartTest(utils.getAddress() + "/api/v4/sessions", 201);
        setTestFields(result.getJSONObject("test"));
        reportURL = result.getString("publicTokenUrl");
        fillFields(result);
        return reportURL;
    }

    private JSONObject sendStartTest(String uri, int expectedRC) throws IOException {
        JSONObject response = utils.queryObject(utils.createPost(uri, ""), expectedRC);
        return response.getJSONObject("result");
    }

    private void fillFields(JSONObject result) {
        this.signature = result.getString("signature");
        this.session = Session.fromJSON(utils, getId(), signature, result.getJSONObject("session"));
        this.master = Master.fromJSON(utils, result.getJSONObject("master"));
    }

    private void setTestFields(JSONObject obj) {
        setId(obj.getString("id"));
        setName(obj.getString("name"));
    }

    public Session getSession() {
        return session;
    }

    public Master getMaster() {
        return master;
    }

    public String getSignature() {
        return signature;
    }

    public String getReportURL() {
        return reportURL;
    }

    public static Test fromJSON(BlazeMeterUtils utils, JSONObject obj) {
        return new Test(utils, obj.getString("id"), obj.getString("name"));
    }
}