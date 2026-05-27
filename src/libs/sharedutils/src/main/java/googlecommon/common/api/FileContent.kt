package minefarts.googlecommon.common.api

import minefarts.googlecommon.common.converters.regexp.RegExp

internal class FileContent {
    @RegExp("[\\w\\W]*")
    private val mContent: String? = null

    val content: String?
        get() = mContent
}
