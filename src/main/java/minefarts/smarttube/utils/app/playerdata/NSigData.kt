package minefarts.smarttube.utils.app.playerdata

import minefarts.smarttube.utils.helpers.Helpers

private const val DELIM: String = "%nsd%"

public data class NSigData(
    val nFuncPlayerUrl: String,
    val nFuncParams: List<String>,
    val nFuncCode: String
) {
    override fun toString(): String {
        return Helpers.merge(DELIM, nFuncPlayerUrl, nFuncParams, nFuncCode)
    }

    companion object {
        @JvmStatic
        fun fromString(spec: String?): NSigData? {
            if (spec == null)
                return null

            val split = Helpers.split(spec, DELIM)

            val nFuncPlayerUrl = Helpers.parseStr(split, 0) ?: return null
            val nFuncParams = Helpers.parseStrList(split, 1).ifEmpty { return null }
            val nFuncCode = Helpers.parseStr(split, 2) ?: return null

            return NSigData(nFuncPlayerUrl, nFuncParams, nFuncCode)
        }
    }
}

