package com.liskovsoft.sharedutils.okhttp;

import okhttp3.*;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;

public class ApiCaller {

    private final Gson mGson = new Gson();

    private final OkHttpClient mClient = new OkHttpClient();

    private HttpUrl.Builder mUrl;

    public static void setBypassEnabled(boolean enabled) {

        ThreadPolicy.Builder builder = new ThreadPolicy.Builder();

        if (enabled)
            builder = builder.permitNetwork();

        StrictMode.setThreadPolicy(builder.build());

    }

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
        mUrl = mUrl.addQueryParameter(name, value);
    }

    /**
     * Fire-and-forget async API call (background thread via enqueue + executor-ready).
     * Non-blocking; no response handling needed.
     */
    public CompletableFuture<Response> call() {
        
        Request request = new Request.Builder()
            .url(mUrl.toString())
            .build();

        CompletableFuture<Response> future = new CompletableFuture<>();
        
        Callback cb = new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                future.complete(response);
            }

        };

        mClient.newCall(request).enqueue(cb);
        
        return future;
    }

}