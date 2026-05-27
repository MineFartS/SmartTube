package minefarts.smarttube.utils.videoinfo.kt.gen

internal data class VideoInfoResult(
    val streamingData: StreamingData?
) {
    data class StreamingData(
        val hlsManifestUrl: String?
    )
}
