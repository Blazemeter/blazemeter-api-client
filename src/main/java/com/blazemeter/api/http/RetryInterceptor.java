/**
 * Copyright 2018 BlazeMeter Inc.
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

import com.blazemeter.api.exception.InterruptRuntimeException;
import com.blazemeter.api.logging.Logger;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * If request was not successful, retry will be taken two times more.
 */
public class RetryInterceptor implements Interceptor {

    private final Logger logger;

    public RetryInterceptor(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String method = request.method();
        int retry = 1;
        int maxRetries = retry + 3;
        Response response = null;
        do {
            try {
                response = chain.proceed(request);
                logger.info("Response code = " + response.code() + " -> done " + retry + " attempt");
                if (respSuccess(response) ||!shouldRetry(method, retry, maxRetries - 1)) {
                    break;
                }

                try {
                    Thread.sleep(1000 * retry);
                } catch (InterruptedException e) {
                    throw new InterruptRuntimeException("Retry was interrupted on sleep at retry # " + retry);
                }
            } catch (SocketTimeoutException ex) {
                logger.info("Server does not send response -> done " + retry + " attempt");
                logger.warn("Server does not send response", ex);
            }
            retry++;

        } while (!respSuccess(response) && shouldRetry(method, retry, maxRetries));

        return response;
    }

    private boolean shouldRetry(String method, int currentRetry, int maxRetries) {
        return "GET".equals(method) && currentRetry < maxRetries;
    }

    private boolean respSuccess(Response response) {
        return response != null && response.isSuccessful();
    }
}
