package minefarts.smarttube.google.common.converters.gson;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.google.common.helpers.ReflectionHelper;

import okhttp3.ResponseBody;
import retrofit2.Converter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

final class GsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
  private final Gson gson;
  private final TypeAdapter<T> adapter;

  GsonResponseBodyConverter(Gson gson, TypeAdapter<T> adapter) {
    this.gson = gson;
    this.adapter = adapter;
  }

  @Override public T convert(ResponseBody value) throws IOException {
    //String response = Helpers.toString(value.byteStream());
    //InputStream newStream = Helpers.toStream(response);
    //JsonReader jsonReader = gson.newJsonReader(new InputStreamReader(newStream));

    JsonReader jsonReader = gson.newJsonReader(value.charStream());
    jsonReader.setLenient(true);
    try {
      // Some endpoints occasionally return HTML/error text instead of JSON.
      // Gson leniency won't help if payload doesn't start like JSON.
      // Attempt a non-fatal peek to ensure we don't get stuck.
      try {
        jsonReader.peek();
      } catch (Exception ignored) {
        // fall through and let adapter handle it
      }

      T result = adapter.read(jsonReader);

      // Dumping all data for debug purposes
      //ReflectionHelper.dumpDebugInfo(result.getClass(), response);

      if (jsonReader.peek() != JsonToken.END_DOCUMENT) {
        throw new JsonIOException("JSON document was not fully consumed.");
      }
      return result;
    } finally {
      value.close();
    }
  }
}
