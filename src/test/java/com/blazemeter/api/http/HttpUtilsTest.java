package com.blazemeter.api.http;

import com.blazemeter.api.logging.LoggerTest;
import net.sf.json.JSONObject;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.blazemeter.api.http.HttpUtils.PROXY_HOST;
import static com.blazemeter.api.http.HttpUtils.PROXY_PASS;
import static com.blazemeter.api.http.HttpUtils.PROXY_PORT;
import static com.blazemeter.api.http.HttpUtils.PROXY_USER;
import static org.junit.Assert.*;
import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_ADDRESS;
import static com.blazemeter.api.utils.BlazeMeterUtilsEmul.BZM_DATA_ADDRESS;

public class HttpUtilsTest {

    public static final String BLAZEDEMO = "http://blazedemo.com/";

    @Test
    public void testRequests() throws Exception {
        LoggerTest logger = new LoggerTest();

        HttpUtils utils = new HttpUtils(BZM_ADDRESS, BZM_DATA_ADDRESS, logger);
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

        HttpUtils utils = new HttpUtils(BZM_ADDRESS, BZM_DATA_ADDRESS, logger);
        assertEquals(BZM_ADDRESS, utils.getAddress());
        assertEquals(BZM_DATA_ADDRESS, utils.getDataAddress());
        assertEquals(logger, utils.getLogger());
    }

    @Test
    public void testModifiers() throws Exception {
        LoggerTest logger = new LoggerTest();

        HttpUtils utils = new HttpUtils(BZM_ADDRESS, BZM_DATA_ADDRESS, logger);

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
        utils = new HttpUtils(BZM_ADDRESS, BZM_DATA_ADDRESS, logger) {
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
    public void testProxy() throws Exception {
        final Map<String, String> saveProps = getProxyProps();
        try {
            LoggerTest logger = new LoggerTest();

            setProxyProps(BLAZEDEMO, "9999", "user1", "pass123456");
            HttpUtils utils = new HttpUtils(BZM_ADDRESS, BZM_DATA_ADDRESS, logger);
            assertNotNull(utils);
            assertEquals("Using http.proxyHost = http://blazedemo.com/\r\n" +
                    "Using http.proxyPort = 9999\r\n" +
                    "Using http.proxyUser = user1\r\n" +
                    "Using http.proxyPass = pass\r\n", logger.getLogs().toString());
            logger.reset();

            setProxyProps(BLAZEDEMO, "use default port", "", "");
            utils = new HttpUtils(BZM_ADDRESS, BZM_DATA_ADDRESS, logger);
            assertNotNull(utils);
            assertEquals("Using http.proxyHost = http://blazedemo.com/\r\n" +
                    "Failed to read http.proxyPort: \r\n" +
                    "For input string: \"use default port\"\r\n" +
                    "Using http.proxyUser = \r\n" +
                    "Using http.proxyPass = \r\n", logger.getLogs().toString());
            logger.reset();

            try {
                setProxyProps("XXXX", "-12345", "", "");
                new HttpUtils(BZM_ADDRESS, BZM_DATA_ADDRESS, logger);
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

}