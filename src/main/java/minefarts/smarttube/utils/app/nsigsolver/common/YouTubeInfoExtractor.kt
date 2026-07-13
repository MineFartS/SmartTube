package minefarts.smarttube.utils.app.nsigsolver.common

import okhttp3.OkHttpClient
import okhttp3.Request

import java.io.IOException

import minefarts.smarttube.utils.okhttp.OkHttpManager

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

import com.eclipsesource.v8.V8

public class InfoExtractorError(message: String, cause: Exception? = null): Exception(message, cause)

object YouTubeInfoExtractor {

    private val httpClient = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    fun loadPlayer(playerUrl: String): String {
        if (playerUrl.isBlank()) throw IllegalArgumentException("Player URL cannot be empty")

        val targetUrl = if (playerUrl.startsWith("http")) playerUrl else "https://youtube.com$playerUrl"

        val request = Request.Builder()
            .url(targetUrl)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .header("Accept", "*/*")
            .header("Accept-Language", "en-US,en;q=0.9")
            .header("Sec-Fetch-Mode", "cors")
            .header("X-Youtube-Client-Name", "1")
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful)
                throw IOException("Unexpected HTTP code: ${response.code()}")

            val bodyText = response.body()?.string() 
                ?: throw IOException("Empty response body from player URL")

            if (bodyText.trim().startsWith("<!DOCTYPE html") || bodyText.contains("<html"))
                throw IOException("YouTube returned an HTML verification/captcha page instead of base.js code.")

            return bodyText
        }
    }

    @Synchronized
    fun downloadWebpage(url: String): String {
        val request = Request.Builder().url(url).build()
        
        return OkHttpManager.instance().client.newCall(request).execute().use {
            if (!it.isSuccessful) throw InfoExtractorError("Unexpected code $it")
            it.body()?.string()
        } ?: throw InfoExtractorError("Empty content received for the $url")
    }

}
