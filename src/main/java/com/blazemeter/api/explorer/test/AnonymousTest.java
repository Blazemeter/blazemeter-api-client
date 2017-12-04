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

package com.blazemeter.api.explorer.test;

import com.blazemeter.api.explorer.Master;
import com.blazemeter.api.explorer.Session;
import com.blazemeter.api.utils.BlazeMeterUtils;
import net.sf.json.JSONObject;

import java.io.IOException;

/*
Test that doesn't exist on server.
Used in <a href="https://github.com/Blazemeter/jmeter-bzm-plugins">BZM Jmeter plugins</a>
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

    public Session getSession() {
        return session;
    }
}
