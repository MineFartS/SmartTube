package minefarts.smarttube.google.common.converters.querystring.converter;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider;
import minefarts.smarttube.google.common.converters.jsonpath.typeadapter.JsonPathTypeAdapter;
import minefarts.smarttube.google.common.converters.querystring.converter.QueryStringRequestBodyConverter;
import minefarts.smarttube.google.common.converters.querystring.converter.QueryStringResponseBodyConverter;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public final class QueryStringConverterFactory extends Converter.Factory {

    private final ParseContext mParser;

    public QueryStringConverterFactory() {
        
        Configuration conf = Configuration
                .builder()
                .mappingProvider(new GsonMappingProvider())
                .jsonProvider(new GsonJsonProvider())
                .build();

        mParser = JsonPath.using(conf);

    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(
        Type type,
        Annotation[] annotations,
        Retrofit retrofit
    ) {
        return new QueryStringResponseBodyConverter<>(new JsonPathTypeAdapter<>(mParser, type));
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(
        Type type,
        Annotation[] parameterAnnotations,
        Annotation[] methodAnnotations,
        Retrofit retrofit
    ) {
        return new QueryStringRequestBodyConverter<>(new JsonPathTypeAdapter<>(mParser, type));
    }
    
}
