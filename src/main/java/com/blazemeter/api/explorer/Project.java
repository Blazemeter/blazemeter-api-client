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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Project extends BZAObject {

    public static final String DEFAULT_PROJECT = "Default project";

    public Project(BlazeMeterUtils utils, String id, String name) {
        super(utils, id, name);
    }

    /**
     * Create Test in current Project
     * @param name - title of the new Test
     */
    public Test createTest(String name) throws IOException {
        String uri = utils.getAddress() + "/api/v4/tests";
        JSONObject data = new JSONObject();
        data.put("projectId", Long.parseLong(getId()));
        JSONObject configuration = new JSONObject();
        configuration.put("type", "external");
        data.put("configuration", configuration);
        data.put("name", name);
        JSONObject response = utils.execute(utils.createPost(uri, data.toString()));
        return Test.fromJSON(utils, response.getJSONObject("result"));
    }

    /**
     * @return list of Tests in current Project
     */
    public List<Test> getTests() throws IOException {
        String uri = utils.getAddress() + "/api/v4/tests?projectId=" + getId();
        JSONObject response = utils.execute(utils.createGet(uri));
        return extractTests(response.getJSONArray("result"));
    }

    private List<Test> extractTests(JSONArray result) {
        List<Test> accounts = new ArrayList<>();

        for (Object obj : result) {
            accounts.add(Test.fromJSON(utils, (JSONObject) obj));
        }

        return accounts;
    }

    public static Project fromJSON(BlazeMeterUtils utils, JSONObject obj) {
        return new Project(utils, obj.getString("id"), obj.getString("name"));
    }
}
