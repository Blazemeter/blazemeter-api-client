package com.blazemeter.api.http;

import com.blazemeter.api.logging.LoggerTest;
import com.blazemeter.api.utils.BlazeMeterUtilsEmul;
import okhttp3.Connection;
import okhttp3.Interceptor;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class RetryInterceptorTest {

    @Test
    public void testFlow() throws Exception {
        LoggerTest logger = new LoggerTest();
        RetryInterceptor retryInterceptor = new RetryInterceptor(logger);
        ChainImpl chain = new ChainImpl();

        Response response = retryInterceptor.intercept(chain);
        assertEquals(200, response.code());
        assertTrue(logger.getLogs().toString().isEmpty());

        chain.code = 777;
        response = retryInterceptor.intercept(chain);
        assertEquals(777, response.code());
        assertEquals("Child request: code = 777 -> 1 retry\r\n" +
                "Child request: code = 777 -> 2 retry\r\n" +
                "Child request: code = 777 -> 3 retry\r\n", logger.getLogs().toString());
    }

    public static class ChainImpl implements Interceptor.Chain {

        public int code = 200;

        @Override
        public Request request() {
            return new Request.Builder().url(BlazeMeterUtilsEmul.BZM_ADDRESS).build();
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
}