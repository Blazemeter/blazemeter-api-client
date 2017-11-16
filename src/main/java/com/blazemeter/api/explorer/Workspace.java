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

public class Workspace extends BZAObject {

    public Workspace(BlazeMeterUtils utils, String id, String name) {
        super(utils, id, name);
    }

    /**
     * Create Project in current Workspace
     * @param name - Name of the new Project
     */
    public Project createProject(String name) throws IOException {
        String uri = utils.getAddress() + "/api/v4/projects";
        JSONObject data = new JSONObject();
        data.put("name", name);
        data.put("workspaceId", Long.parseLong(getId()));
        JSONObject response = utils.execute(utils.createPost(uri, data.toString()));
        return Project.fromJSON(utils, response.getJSONObject("result"));
    }

    /**
     * @return list of Projects in current Workspace
     */
    public List<Project> getProjects() throws IOException {
        String uri = utils.getAddress() + String.format("/api/v4/projects?workspaceId=%s&limit=99999", getId());
        JSONObject response = utils.execute(utils.createGet(uri));
        return extractProjects(response.getJSONArray("result"));
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
