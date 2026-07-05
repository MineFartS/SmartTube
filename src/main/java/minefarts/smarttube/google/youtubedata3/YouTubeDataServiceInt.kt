package minefarts.smarttube.google.youtubedata3

import com.liskovsoft.googlecommon.common.helpers.RetrofitHelper
import minefarts.smarttube.google.youtubedata3.data.SnippetResponse
import minefarts.smarttube.google.youtubedata3.impl.ItemMetadata
import retrofit2.Call

public object YouTubeDataServiceInt {
    
    private val mYouTubeDataApi = RetrofitHelper.create(YouTubeDataApi::class.java)

    @JvmStatic
    fun getVideoMetadata(vararg videoIds: String): List<ItemMetadata>? {
        return mergeResult(videoIds) { getMetadata(it) { mYouTubeDataApi.getVideoMetadata(it) } }
    }

    @JvmStatic
    fun getChannelMetadata(vararg channelIds: String): List<ItemMetadata>? {
        return mergeResult(channelIds) { getMetadata(it) { mYouTubeDataApi.getChannelMetadata(it) } }
    }

    private fun getMetadata(ids: List<String>, callback: (String) -> Call<SnippetResponse?>): List<ItemMetadata>? {
        val mergedIds = ids.joinToString(",")
        val response = RetrofitHelper.get(callback(mergedIds))
        return response?.items?.mapNotNull { it?.let { ItemMetadata(it) } }
    }

    private fun mergeResult(ids: Array<out String>, callback: (List<String>) -> List<ItemMetadata>?): List<ItemMetadata>? {
        val result = mutableListOf<ItemMetadata>()

        ids.toList().chunked(50).forEach {
            callback(it)?.let { result.addAll(it) }
        }

        return result.ifEmpty { null }
    }
    
}