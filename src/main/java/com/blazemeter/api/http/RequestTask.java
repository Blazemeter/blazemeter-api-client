package com.blazemeter.api.http;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.concurrent.Callable;

public class RequestTask implements Callable<Response> {

    private OkHttpClient httpClient;
    private Request request;

    public RequestTask(OkHttpClient httpClient, Request request) {
        this.httpClient = httpClient;
        this.request = request;
    }

    @Override
    public Response call() throws Exception {
        return httpClient.newCall(request).execute();
    }
}
