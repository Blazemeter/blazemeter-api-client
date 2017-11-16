package com.blazemeter.api.explorer;

import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.logging.LoggerTest;
import com.blazemeter.api.logging.UserNotifier;
import com.blazemeter.api.logging.UserNotifierTest;
import com.blazemeter.api.utils.BlazeMeterUtilsEmul;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class UserTest {

    @org.junit.Test
    public void testFlow() throws Exception {
        Logger logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();

        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul("test_address", "test_data_address", notifier, logger);

        User user = new User(emul);

        JSONObject acc = new JSONObject();
        acc.put("id", "accountId");
        acc.put("name", "accountName");
        JSONArray result = new JSONArray();
        result.add(acc);
        result.add(acc);
        JSONObject response = new JSONObject();
        response.put("result", result);
        emul.addEmul(response);

        List<Account> accounts = user.getAccounts();
        assertEquals(2, accounts.size());
        for (Account account : accounts) {
            assertEquals("accountId", account.getId());
            assertEquals("accountName", account.getName());
        }
        // TODO: add logger & notifier test
    }
}