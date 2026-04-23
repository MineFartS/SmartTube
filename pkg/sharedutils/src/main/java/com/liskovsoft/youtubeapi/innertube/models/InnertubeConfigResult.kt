package com.liskovsoft.sharedutils.innertube.models

import com.liskovsoft.sharedutils.common.models.gen.ResponseContext

internal data class InnertubeConfigResult(
    val responseContext: ResponseContext?,
    val configData: String?
)
