package minefarts.smarttube.utils

import com.liskovsoft.googlecommon.common.helpers.RetrofitHelper

public object UtilsService {
    private val mUtilsApi = RetrofitHelper.create(UtilsApi::class.java)

    @JvmStatic
    fun canonicalChannelId(channelId: String?): String? {
        if (channelId == null || !channelId.startsWith("@")) return channelId

        val canonicalChannelId = mUtilsApi.canonicalChannelId(channelId.substring(1))

        return RetrofitHelper.get(canonicalChannelId)?.channelId
    }
}