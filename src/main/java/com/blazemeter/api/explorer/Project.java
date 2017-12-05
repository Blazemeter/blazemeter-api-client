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
import com.blazemeter.api.explorer.test.MultiTest;
import com.blazemeter.api.explorer.test.SingleTest;
import com.blazemeter.api.utils.BlazeMeterUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Project is set of tests.
 * Each project belongs to some workspace
 */
public class Project extends BZAObject {

    public static final String DEFAULT_PROJECT = "Default project";

    public Project(BlazeMeterUtils utils, String id, String name) {
        super(utils, id, name);
    }

    /**
     * Create Test in current Project
     * POST request to 'https://a.blazemeter.com/api/v4/tests'
     * @param name - title of the new Test
     */
    public SingleTest createSingleTest(String name) throws IOException {
        logger.info("Create single test with name=" + name);
        String uri = utils.getAddress() + "/api/v4/tests";
        JSONObject response = utils.execute(utils.createPost(uri, generateRequestBody(name).toString()));
        return SingleTest.fromJSON(utils, response.getJSONObject("result"));
    }

    private JSONObject generateRequestBody(String name) {
        JSONObject data = new JSONObject();
        data.put("projectId", Long.parseLong(getId()));
        JSONObject configuration = new JSONObject();
        configuration.put("type", "external");
        data.put("configuration", configuration);
        data.put("name", name);
        return data;
    }

    /**
     * Get Single tests for Project
     * limit = 10000, sorted by name
     */
    public List<SingleTest> getSingleTests() throws IOException {
        return getSingleTests("10000", "name");
    }

    /**
     * Get Single tests for Project
     * GET request to 'https://a.blazemeter.com/api/v4/tests??projectId={projectId}'
     *
     * @param limit of tests count in returned list
     * @param sort sort type: 'name', 'updated' or other
     * @return list of Tests in current Project
     */
    public List<SingleTest> getSingleTests(String limit, String sort) throws IOException {
        logger.info("Get list of single tests for project id=" + getId());
        String uri = utils.getAddress() + "/api/v4/tests?projectId=" + encode(getId());
        uri = addParamToUrl(uri, "sort%5B%5D", sort); // 'sort%5B%5D' == 'sort[]'
        uri = addParamToUrl(uri, "limit", limit);
        JSONObject response = utils.execute(utils.createGet(uri));
        return extractSingleTests(response.getJSONArray("result"));
    }

    /**
     * GET request to 'https://a.blazemeter.com/api/v4/multi-tests??projectId={projectId}'
     * @return list of Multi-Tests in current Project
     */
    public List<MultiTest> getMultiTests() throws IOException {
        logger.info("Get list of multi tests for project id=" + getId());
        String uri = utils.getAddress() + "/api/v4/multi-tests?projectId=" + encode(getId());
        JSONObject response = utils.execute(utils.createGet(uri));
        return extractMultiTests(response.getJSONArray("result"));
    }


    private List<SingleTest> extractSingleTests(JSONArray result) {
        List<SingleTest> tests = new ArrayList<>();

        for (Object obj : result) {
            tests.add(SingleTest.fromJSON(utils, (JSONObject) obj));
        }

        return tests;
    }

    private List<MultiTest> extractMultiTests(JSONArray result) {
        List<MultiTest> tests = new ArrayList<>();

        for (Object obj : result) {
            tests.add(MultiTest.fromJSON(utils, (JSONObject) obj));
        }

        return tests;
    }

    public static Project fromJSON(BlazeMeterUtils utils, JSONObject obj) {
        return new Project(utils, obj.getString("id"), obj.getString("name"));
    }
}
