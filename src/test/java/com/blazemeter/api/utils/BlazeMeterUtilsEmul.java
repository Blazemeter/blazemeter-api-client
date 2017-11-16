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

import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.logging.UserNotifier;
import net.sf.json.JSON;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.util.LinkedList;


public class BlazeMeterUtilsEmul extends BlazeMeterUtils {

    private LinkedList<JSON> responses = new LinkedList<>();
    private LinkedList<String> requests = new LinkedList<>();

    public BlazeMeterUtilsEmul(String apiKeyId, String apiKeySecret, String address, String dataAddress, UserNotifier notifier, Logger logger) {
        super(apiKeyId, apiKeySecret, address, dataAddress, notifier, logger);
    }

    public BlazeMeterUtilsEmul(String address, String dataAddress, UserNotifier notifier, Logger logger) {
        super(address, dataAddress, notifier, logger);
    }

    public void addEmul(JSON response) {
        responses.add(response);
    }

    public void clean() {
        requests.clear();
    }

    public LinkedList<String> getRequests() {
        return requests;
    }

    @Override
    public JSON query(Object request, int expectedCode) throws IOException {
        extractBody(request);
        return getResponse(request);
    }

    @Override
    public JSONObject queryObject(Object request, int expectedCode) throws IOException {
        extractBody(request);
        return (JSONObject) getResponse(request);
    }

    public void extractBody(Object request) throws IOException {
//        requests.add(request.toString());
    }

    public JSON getResponse(Object request) throws IOException {
        logger.info("Simulating request: " + request);
        if (responses.size() > 0) {
            JSON resp = responses.remove();
            logger.info("Response: " + resp);
            return resp;
        } else {
            throw new IOException("No responses to emulate");
        }
    }
}