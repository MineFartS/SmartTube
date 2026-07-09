package minefarts.smarttube.utils.app.playerdata

import minefarts.smarttube.utils.helpers.Helpers

public data class PlayerExtractorCache(
    val playerUrl: String?,
    val signatureTimestamp: String?
) {
    override fun toString(): String {
        return Helpers.merge(FIELD_DELIM, playerUrl, signatureTimestamp)
    }

    companion object {
        private const val FIELD_DELIM = "%FIELD%"

        @JvmStatic
        fun fromString(data: String): PlayerExtractorCache {
            val split = Helpers.split(data, FIELD_DELIM)

            val playerUrl = Helpers.parseStr(split, 0)
            val signatureTimestamp = Helpers.parseStr(split, 2)

            return PlayerExtractorCache(playerUrl, signatureTimestamp)
        }
    }
}
