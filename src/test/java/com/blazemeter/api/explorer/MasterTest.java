package com.blazemeter.api.explorer;

import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.logging.LoggerTest;
import com.blazemeter.api.logging.UserNotifier;
import com.blazemeter.api.logging.UserNotifierTest;
import com.blazemeter.api.utils.BlazeMeterUtilsEmul;
import net.sf.json.JSONObject;

import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_ADDRESS;
import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_DATA_ADDRESS;
import static org.junit.Assert.assertEquals;

public class MasterTest {

    @org.junit.Test
    public void junitReport() throws Exception {
        Logger logger = new LoggerTest();
        UserNotifier notifier = new UserNotifierTest();

        BlazeMeterUtilsEmul emul = new BlazeMeterUtilsEmul(BZM_ADDRESS, BZM_DATA_ADDRESS, notifier, logger);
        String result = "junit";
        emul.addEmul(result);
        Master master = new Master(emul,"id","name");
        assertEquals("junit", master.junitReport());


    }
}
