package minefarts.smarttube.utils.innertube.models

import com.liskovsoft.youtubeapi.common.models.gen.ResponseContext

public data class InnertubeConfigResult(
    val responseContext: ResponseContext?,
    val configData: String?
)
