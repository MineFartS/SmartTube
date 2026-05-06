package com.liskovsoft.sharedutils.okhttp;

import androidx.annotation.Nullable;

import com.liskovsoft.sharedutils.mylogger.Log;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class OkHttpManager {

    private static final String TAG = OkHttpManager.class.getSimpleName();
    
    private static OkHttpManager sInstance;
    
    private OkHttpClient mClient;

    public static OkHttpManager instance() {
        if (sInstance == null)
            sInstance = new OkHttpManager();

        return sInstance;
    }

    public Response doPostRequest(
        String url, 
        Map<String, String> headers, 
        String postBody, 
        @Nullable String contentType
    ) {

        if (headers == null)
            headers = new HashMap<>();

        Request okHttpRequest = new Request.Builder()
            .url(url)
            .headers(Headers.of(headers))
            .post(RequestBody.create(
                contentType != null ? MediaType.parse(contentType) : null, 
                postBody
            ))
            .build();

        return handleRequest(getClient(), okHttpRequest);
    }

    public Response doGetRequest(String url) {

        Request okHttpRequest = new Request.Builder()
            .url(url)
            .get()
            .build();

        return handleRequest(getClient(), okHttpRequest);
    }

    public Response doGetRequest(
        String url, 
        OkHttpClient client, 
        Map<String, String> headers
    ) {
        
        if (headers == null)
            headers = new HashMap<>();
        
        Request okHttpRequest = new Request.Builder()
            .url(url)
            .headers(Headers.of(headers))
            .build();

        return handleRequest(client, okHttpRequest);
    }

    private Response handleRequest(OkHttpClient client, Request okHttpRequest) {
        try {
            return client.newCall(okHttpRequest).execute();
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage()); // network error
            throw new IllegalStateException("Interrupted OkHttp request to " + okHttpRequest.url(), ex);
        }
    }

    public OkHttpClient getClient() {
        if (mClient == null) {
            mClient = OkHttpCommons.setupBuilder(new OkHttpClient.Builder()).build();
        }

        return mClient;
    }
    
}
