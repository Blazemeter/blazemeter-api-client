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
