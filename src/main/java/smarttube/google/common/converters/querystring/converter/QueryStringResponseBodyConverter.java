package minefarts.smarttube.google.common.converters.querystring.converter;

import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.utils.querystringparser.UrlQueryString;
import minefarts.smarttube.utils.querystringparser.UrlQueryStringFactory;
import minefarts.smarttube.utils.common.helpers.AppConstants;
import minefarts.smarttube.google.common.converters.jsonpath.typeadapter.JsonPathTypeAdapter;
import okhttp3.ResponseBody;
import retrofit2.Converter;

final class QueryStringResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    private final JsonPathTypeAdapter<T> mAdapter;

    QueryStringResponseBodyConverter(JsonPathTypeAdapter<T> adapter) {
        mAdapter = adapter;
    }

    @Override
    public T convert(ResponseBody value) {
        try {
            UrlQueryString queryString = UrlQueryStringFactory.parse(value.byteStream());
            return mAdapter.read(Helpers.toStream(queryString.get(AppConstants.VIDEO_INFO_JSON_CONTENT_PARAM)));
        } finally {
            value.close();
        }
    }
}
