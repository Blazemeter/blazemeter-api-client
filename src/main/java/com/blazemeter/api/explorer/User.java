package com.blazemeter.api.explorer;

import com.blazemeter.api.utils.BlazeMeterUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class User extends BZAObject {


    public User(BlazeMeterUtils utils) {
        super(utils, "", "");
    }


    /**
     * @return list of Account for user token
     */
    public List<Account> getAccounts() throws IOException {
        String uri = utils.getAddress()+ "/api/v4/accounts";
        JSONObject response = utils.queryObject(utils.createGet(uri), 200);
        return extractAccounts(response.getJSONArray("result"));
    }

    private List<Account> extractAccounts(JSONArray result) {
        List<Account> accounts = new ArrayList<>();

        for (Object obj : result) {
            accounts.add(Account.fromJSON(utils, (JSONObject) obj));
        }

        return accounts;
    }
}
