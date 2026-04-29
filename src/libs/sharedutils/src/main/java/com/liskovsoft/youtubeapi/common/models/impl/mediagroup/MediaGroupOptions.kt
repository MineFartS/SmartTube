package com.liskovsoft.sharedutils.common.models.impl.mediagroup

import com.liskovsoft.sharedutils.data.MediaGroup
import com.liskovsoft.sharedutils.browse.v2.BrowseApiHelper
import com.liskovsoft.sharedutils.common.helpers.AppClient
import com.liskovsoft.sharedutils.service.internal.MediaServiceData

internal class MediaGroupOptions private constructor(
    val removeShorts: Boolean = false,
    val removeLive: Boolean = false,
    val removeUpcoming: Boolean = false,
    val removeWatched: Boolean = false,
    val groupType: Int
) {

    val clientTV by lazy { AppClient.TV }

    companion object {
        fun create(
            groupType: Int, 
            channelId: String? = null
        ): MediaGroupOptions {
            
            val data = MediaServiceData.instance()
            
            val removeShorts = data.isContentHidden(MediaServiceData.CONTENT_SHORTS)

            val removeLive = (MediaGroup.TYPE_SUBSCRIPTIONS == groupType && data.isContentHidden(MediaServiceData.CONTENT_STREAMS_SUBSCRIPTIONS))
            
            val removeUpcoming = data.isContentHidden(MediaServiceData.CONTENT_UPCOMING)
            
            val removeWatched = data.isContentHidden(MediaServiceData.CONTENT_WATCHED)

            return MediaGroupOptions(
                removeShorts,
                removeLive,
                removeUpcoming,
                removeWatched,
                groupType
            )

        }
    }
    
}