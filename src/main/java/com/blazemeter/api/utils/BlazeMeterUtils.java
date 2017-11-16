package com.blazemeter.api.utils;

import com.blazemeter.api.http.HttpUtils;
import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.logging.UserNotifier;
import org.apache.commons.codec.binary.Base64;


public class BlazeMeterUtils extends HttpUtils {

    private final String token;

    protected final UserNotifier notifier;

    /**
     * @param apiKeyId - BlazeMeter Api Key Id
     * @param apiKeySecret - BlazeMeter Api Key Secret
     * @param address - BlazeMeter app address: http://a.blazemeter.com/
     * @param dataAddress - BlazeMeter data address: http://data.blazemeter.com/
     * @param notifier - user notifier, to show user information
     * @param logger - logger, for log events of http requests / response etc.
     */
    public BlazeMeterUtils(String apiKeyId, String apiKeySecret,
                           String address, String dataAddress,
                           UserNotifier notifier, Logger logger) {
        super(address, dataAddress, logger);
        // TODO: replace this other lib
        // TODO: add check for empty tokens.
        this.token = new String(Base64.encodeBase64((apiKeyId + ':' + apiKeySecret).getBytes()));
        this.notifier = notifier;
    }


    public BlazeMeterUtils(String address, String dataAddress, UserNotifier notifier, Logger logger) {
        this("", "", address, dataAddress, notifier, logger);
    }

//    @Override
//    protected void addRequiredHeader(HttpRequestBase httpRequestBase) {
//        if (token != null && !token.isEmpty()) {
//            httpRequestBase.setHeader("Authorization", "Basic " + new String(Base64.encodeBase64(token.getBytes())));
//        }
//    }

//    @Override
//    protected String extractErrorMessage(String response) {
//        if (response != null && !response.isEmpty()) {
//            try {
//                JSON jsonResponse = JSONSerializer.toJSON(response, new JsonConfig());
//                if (jsonResponse instanceof JSONObject) {
//                    JSONObject object = (JSONObject) jsonResponse;
//                    JSONObject errorObj = object.getJSONObject("error");
//                    if (errorObj.containsKey("message")) {
//                        return errorObj.getString("message");
//                    }
//                }
//            } catch (JSONException ex) {
//                log.debug("Cannot parse JSON error response: " + response);
//            }
//        }
//        return response;
//    }
}
