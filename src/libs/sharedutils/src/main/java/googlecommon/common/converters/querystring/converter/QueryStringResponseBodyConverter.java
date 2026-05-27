package minefarts.googlecommon.common.converters.querystring.converter;

import minefarts.sharedutils.helpers.Helpers;
import minefarts.sharedutils.querystringparser.UrlQueryString;
import minefarts.sharedutils.querystringparser.UrlQueryStringFactory;
import minefarts.sharedutils.common.helpers.AppConstants;
import minefarts.googlecommon.common.converters.jsonpath.typeadapter.JsonPathTypeAdapter;
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
