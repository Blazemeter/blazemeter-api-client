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
import net.sf.json.JSONObject;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.*;

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
    protected static final MediaType FILE_STREAM = MediaType.parse("application/x-www-form-urlencoded");

    protected Logger logger;

    protected OkHttpClient httpClient;
    protected final ExecutorService service = Executors.newFixedThreadPool(4);

    public HttpUtils(Logger logger) {
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
     * Create Post Request with json body
     */
    public Request createPost(String url, File data) {
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder();
        bodyBuilder.addPart(MultipartBody.Part.createFormData("file", data.getAbsolutePath(), RequestBody.create(FILE_STREAM, data)));
        bodyBuilder.setType(MultipartBody.FORM);
        return createRequestBuilder(url).post(bodyBuilder.build()).build();
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
        RequestTask task = new RequestTask(httpClient, request);
        Future<Response> future = service.submit(task);
        Response response;
        try {
            response = future.get();
        } catch (InterruptedException e) {
            future.cancel(true);
            logger.warn("Caught InterruptedException ", e);
            throw new InterruptRuntimeException("Request has been interrupted", e);
        } catch (ExecutionException e) {
            future.cancel(true);
            logger.warn("Caught ExecutionException ", e);
            throw new IOException(e.getMessage(), e);
        }
        return response.body().string();
    }

    protected String extractErrorMessage(String response) {
        return response;
    }

    protected Request.Builder addRequiredHeader(Request.Builder requestBuilder) {
        // NOOP
        return requestBuilder;
    }

    private Request.Builder createRequestBuilder(String url) {
        final Request.Builder builder = new Request.Builder().url(modifyRequestUrl(url)).
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

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    protected OkHttpClient createHTTPClient() {
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
                    .connectTimeout(180, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .proxy(proxy)
                    .proxyAuthenticator(auth).build();
        } catch (Exception ex) {
            logger.warn("ERROR Instantiating HTTPClient. Exception received: " + ex.getMessage(), ex);
            throw new RuntimeException("ERROR Instantiating HTTPClient. Exception received: " + ex.getMessage(), ex);
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
            return new AuthenticatorExt(proxyUser, proxyPass);
        }
        return Authenticator.NONE;
    }

    protected static class AuthenticatorExt implements Authenticator {
        private final String proxyUser;
        private final String proxyPass;

        public AuthenticatorExt(String proxyUser, String proxyPass) {
            this.proxyUser = proxyUser;
            this.proxyPass = proxyPass;
        }

        // https://github.com/square/okhttp/wiki/Recipes#handling-authentication
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
    }
}
