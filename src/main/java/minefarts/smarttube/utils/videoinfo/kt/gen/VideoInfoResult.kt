package minefarts.smarttube.utils.videoinfo.kt.gen

public data class VideoInfoResult(
    val streamingData: StreamingData?
) {
    data class StreamingData(
        val hlsManifestUrl: String?
    )
}
