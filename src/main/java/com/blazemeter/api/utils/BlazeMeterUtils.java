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
