package minefarts.smarttube.google.common.converters.gson;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.app.models.playback.controllers.VideoStateController;

import okhttp3.ResponseBody;
import retrofit2.Converter;

import java.io.IOException;
import java.io.StringReader;

/**
 * Retrofit Gson converter used across YouTube endpoints.
 * <p>
 * Some endpoints occasionally return non-JSON (HTML/error text) or a JSON string instead of an
 * object. In such cases Gson would throw e.g.:
 * "Expected BEGIN_OBJECT but was STRING at line 1 column 1 path $."
 * <p>
 * We handle this at the boundary to provide a clearer error message to callers.
 */
final class GsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
  
    private final Gson gson;
    private final TypeAdapter<T> adapter;

    GsonResponseBodyConverter(Gson gson, TypeAdapter<T> adapter) {
        this.gson = gson;
        this.adapter = adapter;
    }

    @Override public T convert(ResponseBody value) throws IOException {

        String body = value.string();

        JsonReader jsonReader = gson.newJsonReader(new StringReader(body));
        jsonReader.setLenient(true);

        JsonToken firstToken;
        try {
            firstToken = jsonReader.peek();
        } catch (Exception ignored) {
            // fall through and let adapter throw a parsing exception
            firstToken = null;
        }

        // Common failure: expected object but got a JSON string.
        if (firstToken == JsonToken.STRING) {
            VideoStateController.resetCPN();
            throw new JsonSyntaxException("Expected JSON object but got STRING payload: " + safeSnippet(body));
        }

        T result = adapter.read(jsonReader);

        if (jsonReader.peek() != JsonToken.END_DOCUMENT) {
            throw new JsonIOException("JSON document was not fully consumed.");
        }

        return result;
    }

    private static String safeSnippet(String s) {
        if (s == null) return "null";
        String trimmed = s.trim();
        if (trimmed.length() <= 200) return trimmed;
        return trimmed.substring(0, 200) + "...";
    }

}

