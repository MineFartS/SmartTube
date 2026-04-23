package com.liskovsoft.sharedutils.okhttp;

import retrofit2.Retrofit;
import okhttp3.OkHttpClient;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.Gson;
import java.io.IOException;

public class ApiCaller {

    private final Gson mGson = new Gson();

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
        mUrl = mUrl.addQueryParameter(name, value);
    }

    public String call() {
        return call(String.class);
    }

    public <T> T call(Class<T> type) {

        Request request = new Request.Builder()
            .url(mUrl.toString())
            .build();

        try (Response response = mClient.newCall(request).execute()) {

            String content = response.body().string();

            if (!response.isSuccessful()) throw new IOException(content);
            
            return mGson.fromJson(content, type);
        
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

}