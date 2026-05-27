package minefarts.smarttube.utils.innertube.models

import minefarts.smarttube.utils.common.models.gen.ResponseContext

internal data class InnertubeConfigResult(
    val responseContext: ResponseContext?,
    val configData: String?
)
