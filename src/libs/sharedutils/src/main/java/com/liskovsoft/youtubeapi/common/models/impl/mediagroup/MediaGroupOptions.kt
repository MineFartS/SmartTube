package com.liskovsoft.sharedutils.common.models.impl.mediagroup

import com.liskovsoft.sharedutils.service.data.MediaGroup
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

            return MediaGroupOptions(
                data.isContentHidden(MediaServiceData.CONTENT_SHORTS),
                (MediaGroup.TYPE_SUBSCRIPTIONS == groupType && data.isContentHidden(MediaServiceData.CONTENT_STREAMS_SUBSCRIPTIONS)),
                data.isContentHidden(MediaServiceData.CONTENT_UPCOMING),
                data.isContentHidden(MediaServiceData.CONTENT_WATCHED),
                groupType
            )

        }
    }
    
}