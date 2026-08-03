package minefarts.smarttube.google.common.converters.regexp;

import minefarts.smarttube.google.common.converters.regexp.RegExpTypeAdapter;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import retrofit2.Converter;
import retrofit2.Retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public final class RegExpConverterFactory extends Converter.Factory {

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(
        Type type,
        Annotation[] annotations,
        Retrofit retrofit
    ) {
        return new RegExpResponseBodyConverter<>(new RegExpTypeAdapter<>(type));
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(
        Type type,
        Annotation[] parameterAnnotations,
        Annotation[] methodAnnotations,
        Retrofit retrofit
    ) {
        return new RegExpRequestBodyConverter<>(new RegExpTypeAdapter<>(type));
    }

}
