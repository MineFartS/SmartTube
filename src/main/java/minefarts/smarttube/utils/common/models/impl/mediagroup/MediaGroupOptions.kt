package minefarts.smarttube.utils.common.models.impl.mediagroup

import minefarts.smarttube.utils.service.data.MediaGroup
import minefarts.smarttube.utils.browse.BrowseApiHelper
import minefarts.smarttube.utils.service.internal.MediaServiceData

class MediaGroupOptions (
    val groupType: Int
) {

    fun isHidden(type: Int): Boolean {
        return MediaServiceData.instance().isContentHidden(type)
    }

    val removeShorts: Boolean by lazy {
        isHidden(MediaServiceData.CONTENT_SHORTS)
    }

    val removeUpcoming: Boolean by lazy {
        isHidden(MediaServiceData.CONTENT_UPCOMING)
    }

    val removeWatched: Boolean by lazy {
        isHidden(MediaServiceData.CONTENT_WATCHED)
    }

    val removeLive: Boolean by lazy { 
        (MediaGroup.TYPE_SUBSCRIPTIONS == groupType) && isHidden(MediaServiceData.CONTENT_STREAMS_SUBSCRIPTIONS)
    }
    
}