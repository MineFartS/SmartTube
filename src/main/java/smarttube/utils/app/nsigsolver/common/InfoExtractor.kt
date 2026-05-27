package minefarts.smarttube.utils.app.nsigsolver.common

import minefarts.smarttube.utils.okhttp.OkHttpManager
import minefarts.smarttube.utils.app.nsigsolver.provider.InfoExtractorError
import kotlinx.coroutines.delay
import okhttp3.Request

internal abstract class InfoExtractor {
    suspend fun downloadWebpage(url: String, tries: Int = 1, timeoutMs: Long = 1_000, errorMsg: String? = null): String {
        var tryCount = 0

        while (true) {
            try {
                val request = Request.Builder().url(url).build()
                val content = OkHttpManager.instance().client.newCall(request).execute().use {
                    if (!it.isSuccessful) throw InfoExtractorError(formatError(errorMsg, "Unexpected code $it"))
                    it.body?.string()
                }
                return content ?: throw InfoExtractorError(formatError(errorMsg, "Empty content received for the $url"))
            } catch (e: Exception) {
                tryCount++
                if (tryCount >= tries)
                    throw InfoExtractorError(formatError(errorMsg, "Can't load the $url"), e)
                if (timeoutMs > 0)
                    delay(timeoutMs)
            }
        }
    }
}