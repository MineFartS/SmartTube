package minefarts.smarttube.utils.yuliskov

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

object YouTubeInfoExtractorShim {
    private val client = OkHttpClient()

    suspend fun downloadWebpage(url: String): String = withContext(Dispatchers.IO) {
        val req = Request.Builder().url(url).get().build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code()}")
            resp.body()?.string() ?: ""
        }
    }

    // Add more shim methods here if other call sites require them.
}