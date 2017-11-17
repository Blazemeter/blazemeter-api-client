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
import net.sf.json.JSONObject;

import java.io.IOException;

@Deprecated
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
        JSONObject result = sendStartTest(utils.getAddress() + String.format("/api/v4/tests/%s/start-external", getId()));
        fillFields(result);
    }

    /**
     * Start Anonymous External test
     * @return public link to the report
     */
    public String startAnonymousExternal() throws IOException {
        JSONObject result = sendStartTest(utils.getAddress() + "/api/v4/sessions");
        setTestFields(result.getJSONObject("test"));
        reportURL = result.getString("publicTokenUrl");
        fillFields(result);
        return reportURL;
    }

    private JSONObject sendStartTest(String uri) throws IOException {
        JSONObject response = utils.execute(utils.createPost(uri, ""));
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