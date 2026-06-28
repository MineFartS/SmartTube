package minefarts.smarttube.utils.innertube.models

import minefarts.smarttube.utils.common.models.gen.ResponseContext

public data class InnertubeConfigResult(
    val responseContext: ResponseContext?,
    val configData: String?
)
