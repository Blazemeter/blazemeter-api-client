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
import com.blazemeter.api.explorer.test.MultiTest;
import com.blazemeter.api.explorer.test.SingleTest;
import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.utils.BlazeMeterUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Workspace belongs to Account and has at least one Project.
 */
public class Workspace extends BZAObject {

    public Workspace(BlazeMeterUtils utils, String id, String name) {
        super(utils, id, name);
    }

    /**
     * Get workspace
     * GET request to 'https://a.blazemeter.com/api/v4/workspaces/{workspaceId}'
     * @param utils - BlazeMeterUtils that contains logging and http setup
     * @param id - workspaces Id
     * @return Workspace entity, which contains workspace ID and name (workspace label)
     */
    public static Workspace getWorkspace(BlazeMeterUtils utils, String id) throws IOException {
        Logger logger = utils.getLogger();
        logger.info("Get Workspace id=" + id);
        String uri = utils.getAddress() + String.format("/api/v4/workspaces/%s", BZAObject.encode(logger, id));
        JSONObject response = utils.execute(utils.createGet(uri));
        return Workspace.fromJSON(utils, response.getJSONObject("result"));
    }

    /**
     * Create Project in current Workspace
     * POST request to 'https://a.blazemeter.com/api/v4/projects'
     * @param name - Name of the new Project
     */
    public Project createProject(String name) throws IOException {
        logger.info("Create project with name=" + name);
        String uri = utils.getAddress() + "/api/v4/projects";
        JSONObject data = new JSONObject();
        data.put("name", name);
        data.put("workspaceId", Long.parseLong(getId()));
        JSONObject response = utils.execute(utils.createPost(uri, data.toString()));
        return Project.fromJSON(utils, response.getJSONObject("result"));
    }

    /**
     * Get Project for Workspace
     * limit = 10000, sorted by name
     */
    public List<Project> getProjects() throws IOException {
        return getProjects("10000", "name");
    }

    /**
     * Get Project for Workspace
     * GET request to 'https://a.blazemeter.com/api/v4/projects?workspaceId={workspaceId}&amp;limit=99999'
     * @param limit of tests count in returned list
     * @param sort sort type: 'name', 'updated' or other
     * @return list of Projects in current Workspace
     */
    public List<Project> getProjects(String limit, String sort) throws IOException {
        logger.info("Get list of projects for workspace id=" + getId());
        String uri = utils.getAddress() + String.format("/api/v4/projects?workspaceId=%s", encode(getId()));
        uri = addParamToUrl(uri, "sort%5B%5D", sort); // 'sort%5B%5D' == 'sort[]'
        uri = addParamToUrl(uri, "limit", limit);
        JSONObject response = utils.execute(utils.createGet(uri));
        return extractProjects(response.getJSONArray("result"));
    }

    /**
     * Get Single tests for Workspace
     * limit = 10000, sorted by name
     */
    public List<SingleTest> getSingleTests() throws IOException {
        return getSingleTests("10000", "name");
    }

    /**
     * Get Single tests for Workspace
     * GET request to 'https://a.blazemeter.com/api/v4/tests?workspaceId={workspaceId}'
     * @param limit of tests count in returned list
     * @param sort sort type: 'name', 'updated' or other
     * @return list of Tests in current Workspace
     */
    public List<SingleTest> getSingleTests(String limit, String sort) throws IOException {
        logger.info("Get list of single tests for workspace id=" + getId());
        String uri = utils.getAddress() + "/api/v4/tests?workspaceId=" + encode(getId());
        uri = addParamToUrl(uri, "sort%5B%5D", sort); // 'sort%5B%5D' == 'sort[]'
        uri = addParamToUrl(uri, "limit", limit);
        JSONObject response = utils.execute(utils.createGet(uri));
        return extractSingleTests(response.getJSONArray("result"));
    }

    /**
     * Get Multi tests for Workspace
     * limit = 10000, sorted by name
     */
    public List<MultiTest> getMultiTests() throws IOException {
        return getMultiTests("10000", "name");
    }

    /**
     * Get Test Suite for Workspace
     * limit = 10000, sorted by name
     */
    public List<MultiTest> getTestSuite() throws IOException {
        return getTestSuite("10000", "name");
    }


    /**
     * Get Multi test for Workspace
     * GET request to 'https://a.blazemeter.com/api/v4/multi-tests?workspaceId={workspaceId}'
     * @param limit of tests count in returned list
     * @param sort sort type: 'name', 'updated' or other
     * @return list of Multi-Tests in current Workspace
     */
    public List<MultiTest> getMultiTests(String limit, String sort) throws IOException {
        logger.info("Get list of multi tests for workspace id=" + getId());
        String uri = utils.getAddress() + "/api/v4/multi-tests?workspaceId=" + encode(getId());
        uri = addParamToUrl(uri, "sort%5B%5D", sort); // 'sort%5B%5D' == 'sort[]'
        uri = addParamToUrl(uri, "limit", limit);
        JSONObject response = utils.execute(utils.createGet(uri));
        return extractMultiTests(response.getJSONArray("result"));
    }

    /**
     * Get Test suite for Workspace
     * GET request to 'https://a.blazemeter.com/api/v4/multi-tests?workspaceId={workspaceId}&amp;platform=functional'
     * @param limit of tests count in returned list
     * @param sort sort type: 'name', 'updated' or other
     * @return list of Test-suite in current Workspace
     */
    public List<MultiTest> getTestSuite(String limit, String sort) throws IOException
    {
        logger.info("Get list of test suite for workspace id=" + getId());
        String uri = utils.getAddress() + "/api/v4/multi-tests?workspaceId="+encode(getId())+"&platform=functional";
        uri = addParamToUrl(uri, "sort%5B%5D", sort);
        uri = addParamToUrl(uri, "limit", limit);
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

    private List<Project> extractProjects(JSONArray result) {
        List<Project> projects = new ArrayList<>();

        for (Object obj : result) {
            projects.add(Project.fromJSON(utils, (JSONObject) obj));
        }

        return projects;
    }

    public static Workspace fromJSON(BlazeMeterUtils utils, JSONObject obj) {
        return new Workspace(utils, obj.getString("id"), obj.getString("name"));
    }
}