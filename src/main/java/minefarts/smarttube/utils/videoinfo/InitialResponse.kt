package minefarts.smarttube.utils.videoinfo

import minefarts.smarttube.utils.helpers.Helpers
import minefarts.smarttube.google.common.api.FileApi
import minefarts.smarttube.google.common.converters.jsonpath.converter.JsonPathConverterFactory
import minefarts.smarttube.google.common.converters.jsonpath.converter.JsonPathResponseBodyConverter
import minefarts.smarttube.google.common.helpers.RetrofitHelper
import minefarts.smarttube.utils.videoinfo.models.VideoInfo
import java.util.regex.Pattern


public object InitialResponse {
    
    private val YT_INITIAL_PLAYER_RESPONSE_RE: Pattern = Pattern.compile("""ytInitialPlayerResponse\s*=""")

    private fun searchJson(startPattern: Pattern, content: String, endPattern: Pattern = Pattern.compile(";"),
                   containsPattern: Pattern = Pattern.compile("""\{(?s:.+?)\}""")): String? {
        val jsonRegex = Pattern.compile("""(?:$startPattern)\s*($containsPattern)\s*(?:$endPattern)""")
        val matcher = jsonRegex.matcher(content)

        if (matcher.find() && matcher.groupCount() == 1) {
            return matcher.group(1)
        }

        return null
    }

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun getVideoInfo(videoId: String, auth: Boolean = true): VideoInfo? {
        val fileApi = RetrofitHelper.create(FileApi::class.java)
        val resultWrapper = fileApi.getContent("https://www.youtube.com/watch?v=$videoId")
        val result = RetrofitHelper.get(resultWrapper, auth)

        result?.content?.let {
            val jsonStr = searchJson(YT_INITIAL_PLAYER_RESPONSE_RE, it)

            val factory = JsonPathConverterFactory()
            val converter = factory.responseBodyConverter(VideoInfo::class.java, null, null)
            converter as JsonPathResponseBodyConverter<VideoInfo>
            return converter.convert(Helpers.toStream(jsonStr))
        }

        return null
    }
}