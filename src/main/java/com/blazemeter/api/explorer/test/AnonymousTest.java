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
import com.blazemeter.api.utils.BlazeMeterUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.File;
import java.io.IOException;

/**
 * Test that doesn't exist on server.
 * Used in <a href="https://github.com/Blazemeter/jmeter-bzm-plugins">BZM Jmeter plugins</a>
*/
public class AnonymousTest extends AbstractTest {

    private Session session;

    public AnonymousTest(BlazeMeterUtils utils) {
        super(utils, "", "", "external");
    }

    @Override
    public Master start() throws IOException {
        logger.error("Start is not supported for anonymous test type");
        throw new UnsupportedOperationException("Start is not supported for anonymous test type");
    }

    @Override
    public Master startWithProperties(String properties) throws IOException {
        logger.error("Start is not supported for anonymous test type");
        throw new UnsupportedOperationException("Start is not supported for anonymous test type");
    }

    /**
     * GET request to 'https://a.blazemeter.com/api/v4/sessions'
     */
    @Override
    public Master startExternal() throws IOException {
        logger.info("Start external anonymous test");
        JSONObject result = sendStartTest(utils.getAddress() + "/api/v4/sessions");
        fillFields(result);
        return master;
    }

    @Override
    public void fillFields(JSONObject result) {
        this.signature = result.getString("signature");
        this.master = Master.fromJSON(utils, result.getJSONObject("master"));
        JSONObject test = result.getJSONObject("test");
        this.id = test.getString("id");
        this.name = test.getString("name");
        this.session = Session.fromJSON(utils, getId(), signature, result.getJSONObject("session"));
    }

    @Override
    public void uploadFile(File file) throws IOException {
        logger.error("Upload file is not supported for anonymous test type");
        throw new UnsupportedOperationException("Upload file is not supported for anonymous test type");
    }

    @Override
    public void update(String data) throws IOException {
        logger.error("Update is not supported for anonymous test type");
        throw new UnsupportedOperationException("Update is not supported for anonymous test type");
    }

    @Override
    public void validate(String data) throws IOException {
        logger.error("Validate is not supported for anonymous test type");
        throw new UnsupportedOperationException("Validate is not supported for anonymous test type");
    }

    @Override
    public JSONArray validations() throws IOException {
        logger.error("Validations is not supported for anonymous test type");
        throw new UnsupportedOperationException("Validations is not supported for anonymous test type");
    }

    public Session getSession() {
        return session;
    }
}
