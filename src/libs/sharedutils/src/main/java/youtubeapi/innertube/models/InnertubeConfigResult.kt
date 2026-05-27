package minefarts.sharedutils.innertube.models

import minefarts.sharedutils.common.models.gen.ResponseContext

internal data class InnertubeConfigResult(
    val responseContext: ResponseContext?,
    val configData: String?
)
