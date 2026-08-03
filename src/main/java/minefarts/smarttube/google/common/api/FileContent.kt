package minefarts.smarttube.google.common.api

import minefarts.smarttube.google.common.converters.regexp.RegExp

public class FileContent {
    @RegExp("[\\w\\W]*")
    private val mContent: String? = null

    val content: String?
        get() = mContent
}
