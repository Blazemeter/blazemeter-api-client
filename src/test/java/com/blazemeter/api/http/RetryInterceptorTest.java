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

import com.blazemeter.api.logging.LoggerTest;
import com.blazemeter.api.utils.BlazeMeterUtilsEmul;
import okhttp3.Connection;
import okhttp3.Interceptor;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.junit.Test;

import java.io.IOException;
import java.net.SocketTimeoutException;

import static com.blazemeter.api.http.HttpUtils.JSON_CONTENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RetryInterceptorTest {

    @Test
    public void testFlow() throws Exception {
        LoggerTest logger = new LoggerTest();
        RetryInterceptor retryInterceptor = new RetryInterceptor(logger);
        ChainImpl chain = new ChainImpl();

        Response response = retryInterceptor.intercept(chain);
        assertEquals(200, response.code());
        assertEquals("Response code = 200 -> done 1 attempt\r\n", logger.getLogs().toString());
        logger.reset();

        chain.code = 777;
        response = retryInterceptor.intercept(chain);
        assertEquals(777, response.code());
        assertEquals("Response code = 777 -> done 1 attempt\r\n" +
                "Response code = 777 -> done 2 attempt\r\n" +
                "Response code = 777 -> done 3 attempt\r\n", logger.getLogs().toString());
    }

    public static class ChainImpl implements Interceptor.Chain {

        public int code = 200;
        public String method = "GET";
        public RequestBody body = null;

        @Override
        public Request request() {
            return new Request.Builder().url(BlazeMeterUtilsEmul.BZM_ADDRESS).method(method, body).build();
        }

        @Override
        public Response proceed(Request request) throws IOException {
            Response.Builder responseBuilder = new Response.Builder();
            return responseBuilder.code(code).request(request).protocol(Protocol.get("http/1.1")).build();
        }

        @Override
        public Connection connection() {
            return null;
        }
    }

    @Test
    public void testInterrupt() throws Exception {
        final Throwable[] ex = {null};
        Thread t = new Thread() {
            @Override
            public void run() {
                LoggerTest logger = new LoggerTest();
                RetryInterceptor retryInterceptor = new RetryInterceptor(logger);
                ChainImpl chain = new ChainImpl();
                chain.code = 500;
                try {
                    retryInterceptor.intercept(chain);
                    fail();
                } catch (Throwable e) {
                    ex[0] = e;
                }
            }
        };
        t.start();
        t.interrupt();
        t.join();
        assertNotNull(ex[0]);
        assertEquals("Retry was interrupted on sleep at retry # 1", ex[0].getMessage());

    }

    public static class ChainWithErrorImpl implements Interceptor.Chain {
        public int successAttemptNumber = 5;
        public int code = 200;
        private int currentAttempt = 0;

        @Override
        public Request request() {
            return new Request.Builder().url(BlazeMeterUtilsEmul.BZM_ADDRESS).build();
        }

        @Override
        public Response proceed(Request request) throws IOException {
            currentAttempt++;
            if (currentAttempt == successAttemptNumber) {
                Response.Builder responseBuilder = new Response.Builder();
                return responseBuilder.code(code).request(request).protocol(Protocol.get("http/1.1")).build();
            }
            throw new SocketTimeoutException("ooops");
        }

        @Override
        public Connection connection() {
            return null;
        }
    }

    @Test
    public void testRetriesWithSocketTimeoutException() throws Exception {
        LoggerTest logger = new LoggerTest();
        RetryInterceptor retryInterceptor = new RetryInterceptor(logger);
        ChainWithErrorImpl chain = new ChainWithErrorImpl();

        try {
            retryInterceptor.intercept(chain);
            fail();
        } catch (SocketTimeoutException ex) {
            assertEquals("oooops", ex.getMessage());
            String logs = logger.getLogs().toString();
            assertTrue(logs, logs.contains("Server does not send response -> done 1 attempt"));
            assertTrue(logs, logs.contains("Server does not send response -> done 2 attempt"));
            assertTrue(logs, logs.contains("Server does not send response -> done 3 attempt"));
            assertTrue(logs, logs.contains("Server does not send response"));
        }
    }

    @Test
    public void testRetriesWithSocketTimeoutException2() throws Exception {
        LoggerTest logger = new LoggerTest();
        RetryInterceptor retryInterceptor = new RetryInterceptor(logger);
        ChainWithErrorImpl chain = new ChainWithErrorImpl();
        chain.successAttemptNumber = 3;

        Response response = retryInterceptor.intercept(chain);
        assertEquals(200, response.code());
        String logs = logger.getLogs().toString();
        assertTrue(logs, logs.contains("Server does not send response -> done 1 attempt"));
        assertTrue(logs, logs.contains("Server does not send response -> done 2 attempt"));
        assertTrue(logs, logs.contains("Response code = 200 -> done 3 attempt"));
    }

    @Test
    public void testRetryPOSTRequest() throws Exception {
        LoggerTest logger = new LoggerTest();
        RetryInterceptor retryInterceptor = new RetryInterceptor(logger);
        ChainImpl chain = new ChainImpl();
        chain.method = "POST";
        chain.body = RequestBody.create(JSON_CONTENT, "{}");
        chain.code = 408;

        Response response = retryInterceptor.intercept(chain);
        assertEquals(408, response.code());
        assertEquals("Response code = 408 -> done 1 attempt\r\n", logger.getLogs().toString());
    }
}