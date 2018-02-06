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
import com.blazemeter.api.explorer.base.BZAObject;
import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.utils.BlazeMeterUtils;
import net.sf.json.JSONObject;

import java.io.IOException;


/**
 * Corresponds to '.multi' or '.multi-location' tests on server.
 */
public class MultiTest extends AbstractTest {

    private static String MULTI_TESTS = "/api/v4/multi-tests";

    public MultiTest(BlazeMeterUtils utils, String id, String name, String testType) {
        super(utils, id, name, testType);
    }

    /**
     * POST request to 'https://a.blazemeter.com/api/v4/multi-tests/{testId}/start'
     */
    @Override
    public Master start() throws IOException {
        logger.info("Start multi test id=" + getId());
        JSONObject result = sendStartTest(utils.getAddress() + String.format(MULTI_TESTS + "/%s/start", encode(getId())));
        fillFields(result);
        return master;
    }

    /**
     * POST request to 'https://a.blazemeter.com/api/v4/multi-tests/{testId}/start'
     */
    @Override
    public Master startWithProperties(String properties) throws IOException {
        logger.info("Start multi test id=" + getId());
        JSONObject result = sendStartTestWithBody(utils.getAddress() + String.format(MULTI_TESTS + "/%s/start", encode(getId())), properties);
        fillFields(result);
        return master;
    }


    @Override
    public Master startExternal() throws IOException {
        logger.error("Start external is not supported for multi test type id=" + getId());
        throw new UnsupportedOperationException("Start external is not supported for multi test type id=" + getId());
    }

    /**
     * Get multi-test
     * GET request to 'https://a.blazemeter.com/api/v4/multi-tests/{testId}'
     *
     * @param utils - BlazeMeterUtils that contains logging and http setup
     * @param id    - multi-test Id
     * @return MultiTest entity, which contains test ID and name (test label)
     */
    public static MultiTest getMultiTest(BlazeMeterUtils utils, String id) throws IOException {
        Logger logger = utils.getLogger();
        logger.info("Get Multi Test id=" + id);
        String uri = utils.getAddress() + String.format(MULTI_TESTS + "/%s", BZAObject.encode(logger, id));
        JSONObject response = utils.execute(utils.createGet(uri));
        return MultiTest.fromJSON(utils, response.getJSONObject("result"));
    }

    @Override
    public void fillFields(JSONObject result) {
        this.signature = Session.UNDEFINED;
        this.master = Master.fromJSON(utils, result);
    }

    public static MultiTest fromJSON(BlazeMeterUtils utils, JSONObject obj) {
        return new MultiTest(utils, obj.getString("id"), obj.getString("name"), obj.getString("collectionType"));
    }
}
