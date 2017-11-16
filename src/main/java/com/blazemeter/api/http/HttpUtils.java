package com.blazemeter.api.http;

import com.blazemeter.api.logging.Logger;
import net.sf.json.JSON;
import net.sf.json.JSONObject;

import java.io.IOException;

/**
 * Class for working with HTTP requests
 */
public class HttpUtils {

    protected final static int TIMEOUT = 5;

    protected final Logger logger;

    protected final String address;
    protected final String dataAddress;

    public HttpUtils(String address, String dataAddress, Logger logger) {
        this.address = address;
        this.dataAddress = dataAddress;
        this.logger = logger;
//        this.httpClient = createHTTPClient();
    }


    /**
     * Create Get Request
     */
    public Object createGet(String uri) {
        return null;
    }

    /**
     * Create Post Request with json body
     */
    public Object createPost(String uri, String data) {
        return null;
    }

    /**
     * Create Patch Request
     */
    public Object createPatch(String url, JSON data) {
        return null;
    }

    /**
     * Execute Http request and verify response
     * @param request - HTTP Request
     * @param expectedCode - expected response code
     * @return - response in JSONObject
     */
    public JSONObject queryObject(Object request, int expectedCode) throws IOException {
        return null;
    }

    /**
     * Execute Http request and response code
     * @param request - HTTP Request
     * @param expectedCode - expected response code
     * @return - response in JSONObject
     */
    public JSON query(Object request, int expectedCode) throws IOException {
        return null;
    }

    public Logger getLogger() {
        return logger;
    }

    public String getAddress() {
        return address;
    }

    public String getDataAddress() {
        return dataAddress;
    }
}
