package minefarts.sharedutils.common.models.impl.mediagroup

import minefarts.sharedutils.service.data.MediaGroup
import minefarts.sharedutils.browse.v2.BrowseApiHelper
import minefarts.sharedutils.common.helpers.AppClient
import minefarts.sharedutils.service.internal.MediaServiceData

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