package com.liskovsoft.youtubeapi.common.models.impl.mediagroup

import com.liskovsoft.mediaserviceinterfaces.data.MediaGroup
import com.liskovsoft.youtubeapi.browse.v2.BrowseApiHelper
import com.liskovsoft.youtubeapi.common.helpers.AppClient
import com.liskovsoft.youtubeapi.service.internal.MediaServiceData

internal class MediaGroupOptions private constructor(
    val removeShorts: Boolean = false,
    val removeLive: Boolean = false,
    val removeUpcoming: Boolean = false,
    val removeWatched: Boolean = false,
    val groupType: Int,
    val enableLegacyUI: Boolean = false
) {

    val clientTV by lazy { if (enableLegacyUI) AppClient.TV_LEGACY else AppClient.TV }

    companion object {
        fun create(groupType: Int, channelId: String? = null): MediaGroupOptions {
            
            val data = MediaServiceData.instance()
            
            val removeShorts = data.isContentHidden(MediaServiceData.CONTENT_SHORTS)

            val removeLive = (MediaGroup.TYPE_SUBSCRIPTIONS == groupType && data.isContentHidden(MediaServiceData.CONTENT_STREAMS_SUBSCRIPTIONS))
            
            val removeUpcoming = data.isContentHidden(MediaServiceData.CONTENT_UPCOMING)
            
            val removeWatched = data.isContentHidden(MediaServiceData.CONTENT_WATCHED)
            
            val isGridSection = MediaGroup.TYPE_SUBSCRIPTIONS == groupType || MediaGroup.TYPE_HISTORY == groupType || MediaGroup.TYPE_CHANNEL_UPLOADS == groupType
            
            val isBrowseSection = groupType != MediaGroup.TYPE_SUGGESTIONS // legacy suggestions ui doesn't have chapters
            
            val enableLegacyUI = (data.isLegacyUIEnabled && isBrowseSection) || (!removeShorts && isGridSection) // the modern grid ui contains shorts on a separate row

            return MediaGroupOptions(
                removeShorts,
                removeLive,
                removeUpcoming,
                removeWatched,
                groupType,
                enableLegacyUI
            )

        }
    }
    
}