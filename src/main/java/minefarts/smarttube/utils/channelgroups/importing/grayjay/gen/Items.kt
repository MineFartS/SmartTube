package minefarts.smarttube.utils.channelgroups.importing.grayjay.gen

public data class GrayJayGroup(
    val id: String,
    val name: String,
    val image: GrayJayImage?,
    val urls: List<String>?,
    val creationTime: Int?
) {
    data class GrayJayImage(
        val url: String?
    )
}