package minefarts.smarttube.utils.next.v2

import minefarts.smarttube.utils.service.data.MediaGroup
import minefarts.smarttube.utils.data.MediaItem
import minefarts.smarttube.utils.service.data.MediaItemMetadata
import minefarts.smarttube.utils.app.AppService
import minefarts.smarttube.utils.browse.BrowseApiHelper
import minefarts.smarttube.utils.channelgroups.ChannelGroupServiceImpl
import minefarts.smarttube.utils.channelgroups.models.ItemImpl
import minefarts.smarttube.google.common.helpers.RetrofitHelper
import minefarts.smarttube.google.common.helpers.YouTubeHelper
import minefarts.smarttube.utils.common.models.impl.mediagroup.MediaGroupOptions
import minefarts.smarttube.utils.common.models.impl.mediagroup.SuggestionsGroup
import minefarts.smarttube.utils.next.v2.gen.UnlocalizedTitleResult
import minefarts.smarttube.utils.next.v2.gen.WatchNextResult
import minefarts.smarttube.utils.next.v2.gen.WatchNextResultContinuation
import minefarts.smarttube.utils.next.v2.gen.isEmpty
import minefarts.smarttube.utils.next.v2.impl.MediaItemMetadataImpl
import minefarts.smarttube.utils.SignInService
import com.liskovsoft.youtubeapi.common.helpers.AppClient

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

public open class WatchNextService {

    private val mWatchNextApi = RetrofitHelper.create(WatchNextApi::class.java)
    
    private val mAppService = AppService.instance()

    fun getMetadata(videoId: String): MediaItemMetadata? {
        return getMetadata(videoId, null, 0)
    }

    fun getMetadata(item: MediaItem): MediaItemMetadata? {
        return getMetadata(item.videoId, item.playlistId, item.playlistIndex)
    }

    open fun getMetadata(videoId: String?, playlistId: String?, playlistIndex: Int): MediaItemMetadata? {
        return getMetadata(videoId, playlistId, playlistIndex, null)
    }

    open fun getMetadata(videoId: String?, playlistId: String?, playlistIndex: Int, playlistParams: String?): MediaItemMetadata? {
        val watchNext = getWatchNext(videoId, playlistId, playlistIndex, playlistParams) ?: return null

        if (videoId == null && watchNext.isEmpty()) {
            return null
        }

        return MediaItemMetadataImpl(watchNext).apply {
            channelId?.let {
                ChannelGroupServiceImpl.cachedChannel = ItemImpl(it, author, authorImageUrl)
                if (!SignInService.instance().isSigned) {
                    isSubscribedOverrideItem = ChannelGroupServiceImpl.isSubscribed(it)
                } else if (isSubscribed != ChannelGroupServiceImpl.isSubscribed(it)) {
                    ChannelGroupServiceImpl.subscribe(isSubscribed, it)
                }
            }
        }
    }

    fun continueGroup(mediaGroup: MediaGroup?): MediaGroup? {
        val nextKey = YouTubeHelper.extractNextKey(mediaGroup) ?: return null

        var continuation = continueWatchNext(BrowseApiHelper.getContinuationQuery(AppClient.TV, nextKey))

        if (continuation == null || continuation.isEmpty()) {
            continuation = continueWatchNext(BrowseApiHelper.getContinuationQuery(AppClient.TV, nextKey), false)
        }

        return SuggestionsGroup.from(continuation, mediaGroup)
    }

    fun getUnlocalizedTitle(videoId: String?): String? {
        return getUnlocalizedTitleResult(videoId)?.title
    }

    private fun getWatchNext(videoId: String?, playlistId: String?, playlistIndex: Int, playlistParams: String?): WatchNextResult? {
        return getWatchNext(WatchNextApiHelper.getWatchNextQuery(
            AppClient.TV,
            videoId,
            playlistId,
            playlistIndex,
            playlistParams
        ))
    }

    private fun getWatchNext(query: String): WatchNextResult? {
        
        // Force OkHttp to pass the string payload as unescaped, raw JSON
        val requestBody = query.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        
        val wrapper = mWatchNextApi.getWatchNextResult(requestBody, mAppService.visitorData)

        return RetrofitHelper.get(wrapper)
    }

    private fun continueWatchNext(query: String, auth: Boolean = true): WatchNextResultContinuation? {
        val wrapper = mWatchNextApi.continueWatchNextResult(query, mAppService.visitorData)

        return RetrofitHelper.get(wrapper, auth)
    }

    private fun getUnlocalizedTitleResult(videoId: String?): UnlocalizedTitleResult? {
        if (videoId == null) {
            return null
        }

        val wrapper = mWatchNextApi.getUnlocalizedTitle(WatchNextApiHelper.getUnlocalizedTitleQuery(videoId))

        return RetrofitHelper.get(wrapper)
    }

}