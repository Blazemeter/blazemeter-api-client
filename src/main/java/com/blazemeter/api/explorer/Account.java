package com.blazemeter.api.explorer;

import com.blazemeter.api.explorer.base.BZAObject;
import com.blazemeter.api.utils.BlazeMeterUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Account extends BZAObject {

    public Account(BlazeMeterUtils utils, String id, String name) {
        super(utils, id, name);
    }

    /**
     * Create Workspace in current Account
     * @param name - Name of the new Workspace
     */
    public Workspace createWorkspace(String name) throws IOException {
        String uri = utils.getAddress() + "/api/v4/workspaces";
        JSONObject data = new JSONObject();
        data.put("name", name);
        data.put("accountId", Long.parseLong(getId()));
        JSONObject response = utils.execute(utils.createPost(uri, data.toString()));
        return Workspace.fromJSON(utils, response.getJSONObject("result"));
    }

    /**
     * @return list of Workspace in current Account
     */
    public List<Workspace> getWorkspaces() throws IOException {
        String uri = utils.getAddress() + String.format("/api/v4/workspaces?accountId=%s&enabled=true&limit=100", getId());
        JSONObject response = utils.execute(utils.createGet(uri));
        return extractWorkspaces(response.getJSONArray("result"));
    }

    private List<Workspace> extractWorkspaces(JSONArray result) {
        List<Workspace> workspaces = new ArrayList<>();

        for (Object obj : result) {
            workspaces.add(Workspace.fromJSON(utils, (JSONObject) obj));
        }

        return workspaces;
    }

    public static Account fromJSON(BlazeMeterUtils utils, JSONObject obj) {
        return new Account(utils, obj.getString("id"), obj.getString("name"));
    }
}
