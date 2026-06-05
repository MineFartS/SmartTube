package minefarts.smarttube.google.common.api

import minefarts.smarttube.google.common.converters.regexp.RegExp

internal class FileContent {
    @RegExp("[\\w\\W]*")
    private val mContent: String? = null

    val content: String?
        get() = mContent
}
