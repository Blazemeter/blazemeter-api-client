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

import com.blazemeter.api.exception.UnexpectedResponseException;
import com.blazemeter.api.http.HttpUtils;
import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.logging.UserNotifier;
import com.sun.istack.internal.NotNull;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import okhttp3.Credentials;
import okhttp3.Request;
import org.apache.commons.lang.StringUtils;


public class BlazeMeterUtils extends HttpUtils {

    public static final String EMPTY_TOKEN = "";
    private String apiKeyId;
    private String apiKeySecret;

    protected final UserNotifier notifier;

    /**
     * @param apiKeyId     - BlazeMeter Api Key Id
     * @param apiKeySecret - BlazeMeter Api Key Secret
     * @param address      - BlazeMeter app address: http://a.blazemeter.com/
     * @param dataAddress  - BlazeMeter data address: http://data.blazemeter.com/
     * @param notifier     - user notifier, to show user information
     * @param logger       - logger, for log events of http requests / response etc.
     */
    public BlazeMeterUtils(@NotNull String apiKeyId, @NotNull String apiKeySecret,
                           @NotNull String address, @NotNull String dataAddress,
                           UserNotifier notifier, @NotNull Logger logger) {
        super(address, dataAddress, logger);
        this.notifier = notifier;
    }


    public BlazeMeterUtils(@NotNull String address, @NotNull String dataAddress, UserNotifier notifier, @NotNull Logger logger) {
        this("", "", address, dataAddress, notifier, logger);
    }

    protected boolean isValidCredantials(String apiKeyId, String apiKeySecret) {
        return !StringUtils.isBlank(apiKeyId) && !StringUtils.isBlank(apiKeySecret);
    }

    @Override
    protected Request.Builder addRequiredHeader(Request.Builder requestBuilder) {
        return isValidCredantials(apiKeyId, apiKeySecret) ?
                requestBuilder.addHeader(AUTHORIZATION, Credentials.basic(apiKeyId, apiKeySecret)) :
                requestBuilder;
    }

    @Override
    protected JSONObject processResponse(String response) {
        String error = extractErrorMessage(response);
        if (error != null) {
            logger.error("Receive response with the following error message: " + error);
            throw new UnexpectedResponseException("Receive response with the following error message: " + error);
        }
        return JSONObject.fromObject(response);
    }

    @Override
    protected String extractErrorMessage(String response) {
        if (response != null && !response.isEmpty()) {
            try {
                JSONObject jsonResponse = JSONObject.fromObject(response);
                JSONObject errorObj = jsonResponse.getJSONObject("error");
                if (errorObj.containsKey("message")) {
                    return errorObj.getString("message");
                }
            } catch (JSONException ex) {
                logger.debug("Cannot parse response: " + response, ex);
            }
        }
        return null;
    }

    public UserNotifier getNotifier() {
        return notifier;
    }

    public void setApiKeyId(String apiKeyId) {
        this.apiKeyId = apiKeyId;
    }

    public void setApiKeySecret(String apiKeySecret) {
        this.apiKeySecret = apiKeySecret;
    }
}
