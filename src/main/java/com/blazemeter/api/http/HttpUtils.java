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
import net.sf.json.JSONObject;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

/**
 * Class for working with HTTP requests
 */
public class HttpUtils {

    public static final String PROXY_HOST = "http.proxyHost";
    public static final String PROXY_PORT = "http.proxyPort";
    public static final String PROXY_USER = "http.proxyUser";
    public static final String PROXY_PASS = "http.proxyPass";

    protected static final String ACCEPT = "Accept";
    protected static final String APP_JSON = "application/json";
    protected static final String CONTENT_TYPE = "Content-type";
    protected static final String APP_JSON_UTF_8 = "application/json; charset=UTF-8";
    protected static final String AUTHORIZATION = "Authorization";
    protected static final MediaType JSON_CONTENT = MediaType.parse("application/json; charset=utf-8");

    protected final static int TIMEOUT = 5;

    protected final Logger logger;

    protected final String address;
    protected final String dataAddress;
    private OkHttpClient httpClient;

    public HttpUtils(String address, String dataAddress, Logger logger) {
        this.address = address;
        this.dataAddress = dataAddress;
        this.logger = logger;
        this.httpClient = createHTTPClient();
    }

    /**
     * Create Get Request
     */
    public Request createGet(String url) {
        return createRequestBuilder(url).get().build();
    }

    /**
     * Create Post Request with json body
     */
    public Request createPost(String url, RequestBody data) {
        return createRequestBuilder(url).post(data).build();
    }

    /**
     * Create Post Request with json body
     */
    public Request createPost(String url, String data) {
        return createRequestBuilder(url).post(RequestBody.create(JSON_CONTENT, data)).build();
    }

    /**
     * Create Patch Request
     */
    public Request createPatch(String url, String data) {
        return createRequestBuilder(url).patch(RequestBody.create(JSON_CONTENT, data)).build();
    }

    /**
     * Create Patch Request
     */
    public Request createPatch(String url, RequestBody data) {
        return createRequestBuilder(url).patch(data).build();
    }


    /**
     * Execute Http request
     * @param request - HTTP Request
     * @return - response in JSONObject
     */
    public JSONObject execute(Request request) throws IOException {
        return processResponse(executeRequest(request));
    }

    protected JSONObject processResponse(String response) {
        return JSONObject.fromObject(response);
    }

    /**
     * Execute Http request
     * @param request - HTTP Request
     * @return - response in String
     */
    public String executeRequest(Request request) throws IOException {
        String response = httpClient.newCall(request).execute().body().string();
        // TODO: is it log write into HttpLogger?
        logger.debug("Received response: " + response);
        return response;
    }


    protected String extractErrorMessage(String response) {
        return response;
    }

    protected Request.Builder addRequiredHeader(Request.Builder requestBuilder) {
        // NOOP
        return requestBuilder;
    }

    private Request.Builder createRequestBuilder(String url) {
        final Request.Builder builder = new Request.Builder().url(url).
                addHeader(ACCEPT, APP_JSON).
                addHeader(CONTENT_TYPE, APP_JSON_UTF_8);
        return addRequiredHeader(builder);
    }

    /**
     * Override this method if you want add some require additional params to your URL
     */
    protected String modifyRequestUrl(String url) {
        return url;
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

    private OkHttpClient createHTTPClient() {
        Proxy proxy = Proxy.NO_PROXY;
        Authenticator auth = Authenticator.NONE;
        try {
            String proxyHost = System.getProperty(PROXY_HOST);
            if (!StringUtils.isBlank(proxyHost)) {
                logger.info("Using http.proxyHost = " + proxyHost);
                int proxyPort = getProxyPort();
                proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
                auth = createAuthenticator();
            }

            HttpLoggingInterceptor httpLog = new HttpLoggingInterceptor(new HttpLogger(logger));
            httpLog.setLevel(HttpLoggingInterceptor.Level.BODY);

            return new OkHttpClient.Builder()
                    .addInterceptor(new RetryInterceptor(logger))
                    .addInterceptor(httpLog)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .proxy(proxy)
                    .proxyAuthenticator(auth).build();
        } catch (Exception ex) {
            logger.warn("ERROR Instantiating HTTPClient. Exception received: ", ex);
            throw new RuntimeException("ERROR Instantiating HTTPClient. Exception received: ", ex);
        }
    }

    private int getProxyPort() {
        try {
            int proxyPort = Integer.parseInt(System.getProperty(PROXY_PORT));
            logger.info("Using http.proxyPort = " + proxyPort);
            return proxyPort;
        } catch (NumberFormatException nfe) {
            logger.warn("Failed to read http.proxyPort: ", nfe);
            return 8080;
        }
    }

    private Authenticator createAuthenticator() {
        final String proxyUser = System.getProperty(PROXY_USER);
        logger.info("Using http.proxyUser = " + proxyUser);
        final String proxyPass = System.getProperty(PROXY_PASS);
        logger.info("Using http.proxyPass = " + StringUtils.left(proxyPass, 4));
        if (!StringUtils.isBlank(proxyUser) && !StringUtils.isBlank(proxyPass)) {
            return new Authenticator() {
                @Override
                public Request authenticate(Route route, Response response) throws IOException {
                    String credential = Credentials.basic(proxyUser, proxyPass);
                    if (response.request().header("Proxy-Authorization") != null) {
                        return null; // Give up, we've already attempted to authenticate.
                    }
                    return response.request().newBuilder()
                            .header("Proxy-Authorization", credential)
                            .build();
                }
            };
        }
        return Authenticator.NONE;
    }
}
