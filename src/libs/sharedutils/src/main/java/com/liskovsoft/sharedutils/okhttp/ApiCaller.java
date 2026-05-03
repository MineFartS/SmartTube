package com.liskovsoft.sharedutils.okhttp;

import retrofit2.Retrofit;
import okhttp3.*;
import com.google.gson.Gson;
import java.io.IOException;

public class ApiCaller {

    private final Gson mGson = new Gson();

    private final OkHttpClient mClient = new OkHttpClient();

    private HttpUrl.Builder mUrl;

    private static Callback mCallback = new Callback() {

        @Override
        public void onFailure(Call call, IOException e) {
            e.printStackTrace();
        }

        @Override
        public void onResponse(Call call, Response response) {
            // NOP
        }
        
    };

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

    public void call() {

        Request request = new Request.Builder()
            .url(mUrl.toString())
            .build();

        mClient.newCall(request).enqueue(mCallback);

    }

}