package com.blazemeter.api.explorer;

import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.logging.LoggerTest;
import com.blazemeter.api.logging.UserNotifier;
import com.blazemeter.api.logging.UserNotifierTest;
import com.blazemeter.api.utils.BlazeMeterUtilsEmul;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_ADDRESS;
import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_DATA_ADDRESS;
import static org.junit.Assert.*;

public class SessionTest {

    @org.junit.Test
    public void properties() throws Exception {
        Logger logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();

        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        JSONArray properties = new JSONArray();
        JSONObject property = new JSONObject();
        property.put("key", "url");
        property.put("value", "google.com");
        properties.add(property);
        JSONObject result = new JSONObject();
        result.put("result", property);
        emul.addEmul(result.toString());
        Session session = new Session(emul, "id", "name", "userId", "testId", "sign");
        try {
            session.properties(properties);
        } catch (Exception e) {
            fail("Got an exception while submitting properties");
        }
        emul.clean();
    }

    @org.junit.Test
    public void jtl() throws Exception {
        Logger logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();

        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        JSONArray data = new JSONArray();
        JSONObject dataUrl = new JSONObject();
        dataUrl.put("dataUrl","dataUrl");
        dataUrl.put("title","Zip");
        data.add(dataUrl);
        JSONObject result = new JSONObject();
        result.put("data",data);
        JSONObject o = new JSONObject();
        o.put("result",result);
        emul.addEmul(o.toString());
        Session session = new Session(emul, "id", "name", "userId", "testId", "sign");
        String url=session.jtl();
        assertEquals("dataUrl",url);
        emul.clean();
    }

}
