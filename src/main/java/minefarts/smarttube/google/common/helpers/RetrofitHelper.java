package minefarts.smarttube.google.common.helpers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.utils.mylogger.Log;
import minefarts.smarttube.google.common.converters.gson.WithGson;
import minefarts.smarttube.google.common.converters.gson.GsonConverterFactory;
import minefarts.smarttube.google.common.converters.jsonpath.WithJsonPath;
import minefarts.smarttube.google.common.converters.jsonpath.WithJsonPathSkip;
import minefarts.smarttube.google.common.converters.jsonpath.converter.JsonPathConverterFactory;
import minefarts.smarttube.google.common.converters.jsonpath.converter.JsonPathSkipConverterFactory;
import minefarts.smarttube.google.common.converters.querystring.WithQueryString;
import minefarts.smarttube.google.common.converters.querystring.converter.QueryStringConverterFactory;
import minefarts.smarttube.google.common.converters.regexp.WithRegExp;
import minefarts.smarttube.google.common.converters.regexp.RegExpConverterFactory;
import minefarts.smarttube.google.common.helpers.RetrofitOkHttpHelper;
import minefarts.smarttube.google.common.models.gen.AuthErrorResponse;
import minefarts.smarttube.google.common.models.gen.ErrorResponse;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.annotation.Annotation;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;
import okhttp3.ResponseBody;

import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RetrofitHelper {

    private static final String TAG = RetrofitHelper.class.getSimpleName();

    public static <T> T get(Call<T> wrapper) {
        return get(wrapper, true);
    }

    public static <T> T get(Call<T> wrapper, boolean auth) {
        return get(wrapper, auth, false);
    }

    public static <T> T get(Call<T> wrapper, boolean auth, boolean withErrors) {
        if (!auth)
            RetrofitOkHttpHelper.addAuthSkip(wrapper.request());

        Response<T> response = getResponse(wrapper);

        if (withErrors 
            && response != null 
            && response.body() == null
            && (response.code() == 400 || response.code() == 403 || response.code() == 428)
        ) {

            Gson gson = new GsonBuilder().setLenient().create();

            try (ResponseBody body = response.errorBody()) {
                String errorMsg;
                String errorData = body != null ? body.string() : null;

                try {
                    ErrorResponse error = errorData != null ? gson.fromJson(errorData, ErrorResponse.class) : null;
                    errorMsg = error != null && error.getError() != null ? ErrorResponse.class.getSimpleName() + ": " + error.getError().getMessage() : null;
                } catch (JsonSyntaxException e) {
                    AuthErrorResponse authError = gson.fromJson(errorData, AuthErrorResponse.class);
                    errorMsg = "AuthError: " + authError.getError();
                }

                errorMsg = errorMsg != null ? errorMsg : String.format("Unknown %s error", response.code());

                // Extra context for debugging OAuth/token issues (no secrets)
                String safeErrorBody = errorData != null ? errorData : "";
                // Truncate large bodies to keep logs usable.
                if (safeErrorBody.length() > 2000) {
                    safeErrorBody = safeErrorBody.substring(0, 2000) + "…";
                }

                String codeMsg = String.format("HTTP %s", response.code());
                Log.e(TAG, String.format("%s; %s; body=%s", codeMsg, errorMsg, safeErrorBody));

                throw new IllegalStateException(errorMsg);
            } catch (IOException e) {}
            
        }

        return response != null ? response.body() : null;
    }

    public static <T> Headers getHeaders(Call<T> wrapper) {
        Response<T> response = getResponse(wrapper);

        return response != null ? response.headers() : null;
    }

    public static <T> Response<T> getResponse(Call<T> wrapper) {
        try {
            return wrapper.execute();
        } catch (ConnectException e) {
            // ConnectException - server is down or address is banned (returnyoutubedislikeapi.com)
            e.printStackTrace();
        } catch (InterruptedIOException e) {
            // InterruptedIOException - Thread was interrupted (e.g., RxJava subscription disposed).
            // This is a normal condition in reactive/async flows, not a fatal error.
            // Restore the interrupt status and return null gracefully.
            Thread.currentThread().interrupt();
            Log.d(TAG, "Request was interrupted (thread interrupted)");
        } catch (IOException e) {
            // SocketException - no internet
            // UnknownHostException: Unable to resolve host (DNS error) Thread died?
            e.printStackTrace();

            if (!e.getMessage().contains("malformed JSON"))
                throw new IllegalStateException(e); // notify caller about network condition

        }

        return null;
    }

    public static <T> T create(Class<T> clazz) {

        Retrofit.Builder builder = new Retrofit.Builder().baseUrl("https://www.youtube.com");
        builder.client(RetrofitOkHttpHelper.getClient());

        for (Annotation annotation : clazz.getAnnotations()) {

            Converter.Factory factory;

            if (annotation instanceof WithRegExp) {
                factory = new RegExpConverterFactory();
            } else if (annotation instanceof WithJsonPath) {
                factory = new JsonPathConverterFactory();
            } else if (annotation instanceof WithJsonPathSkip) {
                factory = new JsonPathSkipConverterFactory();
            } else if (annotation instanceof WithQueryString) {
                factory = new QueryStringConverterFactory();
            } else if (annotation instanceof WithGson) {
                factory = new GsonConverterFactory();
            } else {
                continue;
            }

            return builder
                .addConverterFactory(factory)
                .build()
                .create(clazz);

        }

        throw new IllegalStateException("RetrofitHelper: unknown class: " + clazz.getName());
    }

}
