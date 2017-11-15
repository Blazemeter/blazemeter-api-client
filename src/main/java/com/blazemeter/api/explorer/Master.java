package com.blazemeter.api.explorer;

import com.blazemeter.api.utils.BlazeMeterUtils;
import net.sf.json.JSONObject;

import java.io.IOException;

public class Master extends BZAObject {

    public Master(BlazeMeterUtils utils, String id, String name) {
        super(utils, id, name);
    }

    /**
     * Makes a private user report public
     * @return public link to the report
     */
    public String makeReportPublic() throws IOException {
        String uri = utils.getAddress() + String.format("/api/v4/masters/%s/public-token", getId());
        JSONObject obj = new JSONObject();
        obj.put("publicToken", "None");
        JSONObject response = utils.queryObject(utils.createPost(uri, obj.toString()), 201);

        return utils.getAddress() + String.format("/app/?public-token=%s#/masters/%s/summary",
                extractPublicToken(response.getJSONObject("result")), getId());
    }

    private String extractPublicToken(JSONObject result) {
        return result.getString("publicToken");
    }

    public static Master fromJSON(BlazeMeterUtils utils, JSONObject obj) {
        return new Master(utils, obj.getString("id"), obj.getString("name"));
    }
}
