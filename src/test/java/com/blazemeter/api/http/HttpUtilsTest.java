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
import net.sf.json.JSONObject;
import okhttp3.*;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.blazemeter.api.http.HttpUtils.PROXY_HOST;
import static com.blazemeter.api.http.HttpUtils.PROXY_PASS;
import static com.blazemeter.api.http.HttpUtils.PROXY_PORT;
import static com.blazemeter.api.http.HttpUtils.PROXY_USER;
import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_ADDRESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HttpUtilsTest {

    public static final String BLAZEDEMO = "http://blazedemo.com/";

    @Test
    public void testRequests() throws Exception {
        LoggerTest logger = new LoggerTest();

        HttpUtils utils = new HttpUtils(logger);
        Request get = utils.createGet(BZM_ADDRESS);
        assertEquals("GET", get.method());
        assertEquals(BZM_ADDRESS + '/', get.url().toString());

        RequestBody requestBody = RequestBody.create(MediaType.parse("UTF-8"), "param=value");
        Request post1 = utils.createPost(BZM_ADDRESS, requestBody);
        assertEquals("POST", post1.method());
        assertEquals(BZM_ADDRESS + '/', post1.url().toString());
        assertEquals(11, post1.body().contentLength());

        Request post2 = utils.createPost(BZM_ADDRESS, "{\"param\":\"value\"}");
        assertEquals("POST", post2.method());
        assertEquals(BZM_ADDRESS + '/', post2.url().toString());
        assertEquals(17, post2.body().contentLength());


        Request patch1 = utils.createPatch(BZM_ADDRESS, requestBody);
        assertEquals("PATCH", patch1.method());
        assertEquals(BZM_ADDRESS + '/', patch1.url().toString());
        assertEquals(11, patch1.body().contentLength());

        Request patch2 = utils.createPatch(BZM_ADDRESS, "{\"param\":\"value\"}");
        assertEquals("PATCH", patch2.method());
        assertEquals(BZM_ADDRESS + '/', patch2.url().toString());
        assertEquals(17, patch2.body().contentLength());
    }

    @Test
    public void testGetters() throws Exception {
        LoggerTest logger = new LoggerTest();
        HttpUtils utils = new HttpUtils(logger);
        utils.setLogger(logger);
        assertEquals(logger, utils.getLogger());
    }

    @Test
    public void testModifiers() throws Exception {
        LoggerTest logger = new LoggerTest();

        HttpUtils utils = new HttpUtils(logger);

        String response = "No response string";
        assertEquals(response, utils.extractErrorMessage(response));

        String url = BLAZEDEMO;
        assertEquals(url, utils.modifyRequestUrl(url));
        Request.Builder builder = new Request.Builder().url(url).get();
        Request request = utils.addRequiredHeader(builder).build();
        assertEquals(0, request.headers().size());
        String resp = utils.executeRequest(request);
        assertTrue(resp.length() > 0);
        assertTrue(resp.contains("BlazeMeter"));

        final String jsonResponse = "{\"param\":\"value\"}";
        utils = new HttpUtils(logger) {
            @Override
            public String executeRequest(Request request) throws IOException {
                return jsonResponse;
            }
        };
        JSONObject result = utils.execute(null);
        assertEquals(1, result.size());
        assertEquals("value", result.getString("param"));
    }

    @Test
    public void testModifiers2() throws Exception {
        LoggerTest logger = new LoggerTest();

        HttpUtils utils = new HttpUtils(logger) {
            @Override
            protected String modifyRequestUrl(String url) {
                return url + "additional_string";
            }
        };

        String url = BLAZEDEMO;
        assertEquals(url + "additional_string", utils.modifyRequestUrl(url));
    }

    @Test
    public void testProxy() throws Exception {
        final Map<String, String> saveProps = getProxyProps();
        try {
            LoggerTest logger = new LoggerTest();

            setProxyProps(BLAZEDEMO, "9999", "user1", "pass123456");
            HttpUtils utils = new HttpUtils(logger);
            assertNotNull(utils);
            assertEquals("Using http.proxyHost = http://blazedemo.com/\r\n" +
                    "Using http.proxyPort = 9999\r\n" +
                    "Using http.proxyUser = user1\r\n" +
                    "Using http.proxyPass = pass\r\n", logger.getLogs().toString());
            logger.reset();

            setProxyProps(BLAZEDEMO, "use default port", "", "");
            utils = new HttpUtils(logger);
            assertNotNull(utils);
            assertEquals("Using http.proxyHost = http://blazedemo.com/\r\n" +
                    "Failed to read http.proxyPort: \r\n" +
                    "For input string: \"use default port\"\r\n" +
                    "Using http.proxyUser = \r\n" +
                    "Using http.proxyPass = \r\n", logger.getLogs().toString());
            logger.reset();

            try {
                setProxyProps("XXXX", "-12345", "", "");
                new HttpUtils(logger);
                fail("Cannot init proxy with port '-12345'");
            } catch (RuntimeException ex) {
                assertEquals("ERROR Instantiating HTTPClient. Exception received: port out of range:-12345", ex.getMessage());
                assertEquals("Using http.proxyHost = XXXX\r\n" +
                        "Using http.proxyPort = -12345\r\n" +
                        "ERROR Instantiating HTTPClient. Exception received: port out of range:-12345\r\n" +
                        "port out of range:-12345\r\n", logger.getLogs().toString());
            }
        } finally {
            // return properties
            setProxyProps(saveProps.get(PROXY_HOST),
                    saveProps.get(PROXY_PORT),
                    saveProps.get(PROXY_USER),
                    saveProps.get(PROXY_PASS));
        }
    }

    private Map<String, String> getProxyProps() {
        Map<String, String> props = new HashMap<>();
        props.put(PROXY_HOST, System.getProperty(PROXY_HOST));
        props.put(PROXY_PORT, System.getProperty(PROXY_PORT));
        props.put(PROXY_USER, System.getProperty(PROXY_USER));
        props.put(PROXY_PASS, System.getProperty(PROXY_PASS));
        return props;
    }

    private void setProxyProps(String host, String port, String user, String pass) {
        setPropertyOrClearIt(PROXY_HOST, host);
        setPropertyOrClearIt(PROXY_PORT, port);
        setPropertyOrClearIt(PROXY_USER, user);
        setPropertyOrClearIt(PROXY_PASS, pass);
    }

    private void setPropertyOrClearIt(String propertyName, String value) {
        if (value != null) {
            System.setProperty(propertyName, value);
        } else {
            System.clearProperty(propertyName);
        }
    }

    @Test
    public void testInterrupted() throws Exception {
        LoggerTest logger = new LoggerTest();

        final HttpUtils utils = new HttpUtils(logger);

        final Request request = utils.createGet(BLAZEDEMO);
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    utils.executeRequest(request);
                    fail();
                } catch (Throwable e) {
                    assertEquals("Request has been interrupted", e.getMessage());
                }
            }
        };
        t.start();
        t.interrupt();
        t.join();
    }

    @Test
    public void testExecutionException() throws Exception {
        LoggerTest logger = new LoggerTest();

        final HttpUtils utils = new HttpUtils(logger);

        final Request request = utils.createGet("http://aiaiaiaiaiaiaiiaxxxmsmsms.iiingo");
        try {
            utils.executeRequest(request);
            fail();
        } catch (Throwable e) {
            assertEquals("java.net.UnknownHostException: aiaiaiaiaiaiaiiaxxxmsmsms.iiingo: Name or service not known", e.getMessage());
        }
    }

    @Test
    public void testAuth() throws Exception {
        HttpUtils.AuthenticatorExt auth = new HttpUtils.AuthenticatorExt("aaa", "xxx");
        Request.Builder reqBuilder = new Request.Builder();
        reqBuilder.url(BLAZEDEMO).get();
        Response.Builder respBuilder = new Response.Builder();
        respBuilder.request(reqBuilder.build()).protocol(Protocol.HTTP_1_1).code(200);
        Request actual = auth.authenticate(null, respBuilder.build());
        assertEquals(actual.header("Proxy-Authorization"), "Basic YWFhOnh4eA==");

        reqBuilder.addHeader("Proxy-Authorization", "aaa");
        respBuilder.request(reqBuilder.build());
        assertNull(auth.authenticate(null, respBuilder.build()));
    }

    @Test
    public void testPostFile() throws Exception {
        LoggerTest logger = new LoggerTest();
        HttpUtils utils = new HttpUtils(logger);

        String path = HttpUtilsTest.class.getResource("/test.yml").getPath();
        File file = new File(path);

        Request post = utils.createPost("http://blazedemo.com", file);
        RequestBody body = post.body();
        assertEquals("multipart", body.contentType().type());
        assertEquals("form-data", body.contentType().subtype());
        assertTrue(body instanceof MultipartBody);

        MultipartBody.Part part = ((MultipartBody) body).part(0);
        assertEquals(149, part.body().contentLength());
        assertEquals("application/x-www-form-urlencoded", part.body().contentType().toString());
    }
}