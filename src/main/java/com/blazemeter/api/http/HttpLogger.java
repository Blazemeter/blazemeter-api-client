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

package com.blazemeter.api.http;

import com.blazemeter.api.logging.Logger;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.lang.StringUtils;

/*
Pass here valid implementation of @link com.blazemeter.api.logging.Logger
 */
public class HttpLogger implements HttpLoggingInterceptor.Logger {

    private final Logger logger;

    public HttpLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void log(String message) {
        logger.debug(format(message));
    }

    /*
    Cuts out credentials from "Authorization" header.
     */
    protected String format(String logEntry) {
        int authorization = logEntry.lastIndexOf(HttpUtils.AUTHORIZATION) + 1;
        if (authorization > 0) {
            String keyToReplace = logEntry.substring(authorization + 13, logEntry.length() - 10);
            return StringUtils.replace(logEntry, keyToReplace, "...");
        }
        return logEntry;
    }
}
