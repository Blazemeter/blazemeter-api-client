package com.blazemeter.api.utils;

import com.blazemeter.api.exception.InterruptRuntimeException;
import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.logging.UserNotifier;
import net.sf.json.JSONObject;
import okhttp3.Request;

import java.io.IOException;

public class BlazeMeterUtilsSlowEmul extends BlazeMeterUtilsEmul {

    public long delay = 5000;
    public boolean isThrowsError = true;

    public BlazeMeterUtilsSlowEmul(String apiKeyId, String apiKeySecret, String address, String dataAddress, UserNotifier notifier, Logger logger) {
        super(apiKeyId, apiKeySecret, address, dataAddress, notifier, logger);
    }

    public BlazeMeterUtilsSlowEmul(String address, String dataAddress, UserNotifier notifier, Logger logger) {
        super(address, dataAddress, notifier, logger);
    }

    @Override
    public JSONObject execute(Request request) throws IOException {
        makeDelay();
        return super.execute(request);
    }

    @Override
    public String executeRequest(Request request) throws IOException {
        makeDelay();
        return super.executeRequest(request);
    }

    protected void makeDelay() {
        try {
            Thread.currentThread().sleep(delay);
        } catch (InterruptedException ex) {
            if (isThrowsError) {
                throw new InterruptRuntimeException("Interrupted emul", ex);
            }
        }
    }
}
