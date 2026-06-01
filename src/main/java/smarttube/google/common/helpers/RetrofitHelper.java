package minefarts.smarttube.google.common.helpers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider;

import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.utils.mylogger.Log;
import minefarts.smarttube.google.common.converters.gson.WithGson;
import minefarts.smarttube.google.common.converters.gson.GsonConverterFactory;
import minefarts.smarttube.google.common.converters.jsonpath.WithJsonPath;
import minefarts.smarttube.google.common.converters.jsonpath.WithJsonPathSkip;
import minefarts.smarttube.google.common.converters.jsonpath.converter.JsonPathConverterFactory;
import minefarts.smarttube.google.common.converters.jsonpath.converter.JsonPathSkipConverterFactory;
import minefarts.smarttube.google.common.converters.jsonpath.typeadapter.JsonPathSkipTypeAdapter;
import minefarts.smarttube.google.common.converters.jsonpath.typeadapter.JsonPathTypeAdapter;
import minefarts.smarttube.google.common.converters.querystring.WithQueryString;
import minefarts.smarttube.google.common.converters.querystring.converter.QueryStringConverterFactory;
import minefarts.smarttube.google.common.converters.regexp.WithRegExp;
import minefarts.smarttube.google.common.converters.regexp.converter.RegExpConverterFactory;
import minefarts.smarttube.google.common.helpers.RetrofitOkHttpHelper;
import minefarts.smarttube.google.common.models.gen.AuthErrorResponse;
import minefarts.smarttube.google.common.models.gen.ErrorResponse;

import java.io.IOException;
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
    
    // Ignored when specified url is absolute
    private static final String DEFAULT_BASE_URL = "https://www.youtube.com";

    private static <T> T withGson(Class<T> clazz) {
        return buildRetrofit(GsonConverterFactory.create()).create(clazz);
    }

    private static <T> T withJsonPath(Class<T> clazz) {
        return buildRetrofit(JsonPathConverterFactory.create()).create(clazz);
    }

    /**
     * Skips first line of the response
     */
    private static <T> T withJsonPathSkip(Class<T> clazz) {
        return buildRetrofit(JsonPathSkipConverterFactory.create()).create(clazz);
    }

    private static <T> T withQueryString(Class<T> clazz) {
        return buildRetrofit(QueryStringConverterFactory.create()).create(clazz);
    }

    private static <T> T withRegExp(Class<T> clazz) {
        return buildRetrofit(RegExpConverterFactory.create()).create(clazz);
    }

    public static <T> T get(Call<T> wrapper) {
        return get(wrapper, true);
    }

    public static <T> T get(Call<T> wrapper, boolean auth) {
        return get(wrapper, auth, false);
    }

    public static <T> T getWithErrors(Call<T> wrapper) {
        return getWithErrors(wrapper, true);
    }

    public static <T> T getWithErrors(Call<T> wrapper, boolean auth) {
        return get(wrapper, auth, true);
    }

    private static <T> T get(Call<T> wrapper, boolean auth, boolean withErrors) {
        if (!auth) {
            RetrofitOkHttpHelper.addAuthSkip(wrapper.request());
        }

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

                Log.e(TAG, errorMsg);
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
        } catch (IOException e) {
            // SocketException - no internet
            // InterruptedIOException - Thread interrupted. Thread died!!
            // UnknownHostException: Unable to resolve host (DNS error) Thread died?
            e.printStackTrace();

            if (!e.getMessage().contains("malformed JSON"))
                throw new IllegalStateException(e); // notify caller about network condition

        }

        return null;
    }

    public static <T> JsonPathTypeAdapter<T> adaptJsonPathSkip(Class<?> clazz) {
        Configuration conf = Configuration
                .builder()
                .mappingProvider(new GsonMappingProvider())
                .jsonProvider(new GsonJsonProvider())
                .build();

        ParseContext parser = JsonPath.using(conf);

        return new JsonPathSkipTypeAdapter<>(parser, clazz);
    }

    private static Retrofit buildRetrofit(Converter.Factory factory) {

        Retrofit.Builder builder = new Retrofit.Builder().baseUrl(DEFAULT_BASE_URL);

        builder.client(RetrofitOkHttpHelper.getClient());

        return builder
            .addConverterFactory(factory)
            .build();
    }

    /**
     * Get cookie pair as a string: cookieName=cookieValue
     */
    public static <T> String getCookie(Response<T> response, String cookieName) {
        if (response == null) {
            return null;
        }

        List<String> cookies = response.headers().values("Set-Cookie");

        for (String cookie : cookies) {
            if (cookie.startsWith(cookieName)) {
                return cookie.split(";")[0];
            }
        }

        return null;
    }

    /**
     * Get cookie pairs as a colon delimited string: cookieName=cookieValue; cookieName=cookieValue
     */
    public static <T> String getCookies(Response<T> response) {
        if (response == null) {
            return null;
        }

        List<String> result = new ArrayList<>();

        List<String> cookies = response.headers().values("Set-Cookie");

        for (String cookie : cookies) {
            result.add(cookie.split(";")[0]);
        }

        return result.isEmpty() ? null : Helpers.join("; ", result.toArray(new CharSequence[0]));
    }

    public static <T> T create(Class<T> clazz) {
        Annotation[] annotations = clazz.getAnnotations();

        for (Annotation annotation : annotations) {
            if (annotation instanceof WithRegExp) {
                return withRegExp(clazz);
            } else if (annotation instanceof WithJsonPath) {
                return withJsonPath(clazz);
            } else if (annotation instanceof WithJsonPathSkip) {
                return withJsonPathSkip(clazz);
            } else if (annotation instanceof WithQueryString) {
                return withQueryString(clazz);
            } else if (annotation instanceof WithGson) {
                return withGson(clazz);
            }
        }

        throw new IllegalStateException("RetrofitHelper: unknown class: " + clazz.getName());
    }

}
