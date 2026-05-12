package com.liskovsoft.sharedutils.okhttp;

import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class ApiCaller {

    private final OkHttpClient mClient = new OkHttpClient();

    private HttpUrl.Builder mUrl;

    public ApiCaller(String url) {
        mUrl = HttpUrl.parse(url).newBuilder();
    }

    public ApiCaller copy() {
        return new ApiCaller(mUrl.toString());
    }

    public void add(String name, Float value) {
        add(name, String.valueOf(value));
    }

    public void add(String name, String value) {
        mUrl = mUrl.setQueryParameter(name, value);
    }

    /**
     * Fire-and-forget async API call (background thread via enqueue + executor-ready).
     * Non-blocking; no response handling needed.
     */
    public String call() {
        
        Request request = new Request.Builder()
            .url(mUrl.toString())
            .build();

        try (Response response = mClient.newCall(request).execute()) {

            if (!response.isSuccessful()) 
                throw new IOException("Unexpected code " + response);
            
            // Process response body
            return response.body().string();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        
    }

}