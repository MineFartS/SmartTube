package minefarts.smarttube.utils.app

import com.google.gson.Gson
import com.google.gson.JsonElement

import minefarts.smarttube.utils.app.potokencloud.PoTokenCloudService
import minefarts.smarttube.utils.app.potokennp2.PoTokenProvider
import minefarts.smarttube.utils.app.potokennp2.PoTokenResult
import minefarts.smarttube.utils.app.potokennp2.PoTokenGenerator
import minefarts.smarttube.utils.app.potokennp2.PoTokenWebView
import minefarts.smarttube.utils.common.helpers.AppClient
import minefarts.smarttube.google.common.helpers.RetrofitHelper
import minefarts.smarttube.utils.app.AppService
import minefarts.smarttube.utils.app.potoken.*

import java.nio.charset.Charset

import kotlin.random.Random

public object PoTokenGate {
    
    @JvmField
    public var mWebPoToken: PoTokenResult? = null

    private const val REQUEST_KEY = "O43z0dpjhgX20SCx4KAo"
    private val appService = AppService.instance()

    init {
        PoTokenProvider.poTokenFactory = PoTokenWebView
    }

    @JvmStatic
    @JvmOverloads
    fun getPoToken(client: AppClient, videoId: String? = null): String? {
        
        if (!client.isWebPotRequired) {
            return null
        
        } else if (videoId == null) {
            
            if (PoTokenProvider.isWebPotSupported()) {
                if (mWebPoToken == null)
                    mWebPoToken = PoTokenProvider.getWebClientPoToken("")
                return mWebPoToken?.streamingDataPoToken
            } else {
                return PoTokenCloudService.getPoToken()
            }
        
        } else {

            if (mWebPoToken?.videoId != videoId || PoTokenProvider.isWebPotExpired()) {

                if (PoTokenProvider.isWebPotSupported()) {
                    mWebPoToken = PoTokenProvider.getWebClientPoToken(videoId)
                } else {
                    mWebPoToken = null
                }

            }

            return mWebPoToken?.playerRequestPoToken
        }

    }

    @JvmStatic
    fun getColdStartPoToken(client: AppClient, videoId: String): String? {
        if (!client.isWebPotRequired) return null
        
        val encodedIdentifier = videoId.toByteArray(Charset.forName("UTF-8"))

        if (encodedIdentifier.size > 118) {
            throw BGError(
                "BAD_INPUT",
                "Content binding is too long.",
                mapOf("identifierLength" to encodedIdentifier.size)
            )
        }

        val timestamp = (System.currentTimeMillis() / 1000).toInt()

        val randomKeys = byteArrayOf(
            Random.nextInt(256).toByte(),
            Random.nextInt(256).toByte()
        )

        val header = ByteArray(2 + 1 + 1 + 4)
        var offset = 0

        randomKeys.copyInto(header, offset)
        offset += 2

        header[offset++] = 0
        header[offset++] = 1.toByte()

        header[offset++] = ((timestamp ushr 24) and 0xFF).toByte()
        header[offset++] = ((timestamp ushr 16) and 0xFF).toByte()
        header[offset++] = ((timestamp ushr 8) and 0xFF).toByte()
        header[offset]   = (timestamp and 0xFF).toByte()

        val packet = ByteArray(2 + header.size + encodedIdentifier.size)

        packet[0] = 34
        packet[1] = (header.size + encodedIdentifier.size).toByte()

        header.copyInto(packet, 2)
        encodedIdentifier.copyInto(packet, 2 + header.size)

        val payloadOffset = 2
        val keyLength = randomKeys.size

        for (i in keyLength until packet.size - payloadOffset) {
            val idx = payloadOffset + i
            packet[idx] =
                (packet[idx].toInt() xor packet[payloadOffset + (i % keyLength)].toInt()).toByte()
        }

        return u8ToBase64(packet)
    }

    @JvmStatic
    fun getVisitorData(client: AppClient): String? {
        if (client.isWebPotRequired) {
            return mWebPoToken?.visitorData
        } else {
            return null
        }
    }
    
    data class Data(val integrityToken: String?,
                    val estimatedTtlSecs: Int?,
                    val mintRefreshThreshold: Int?,
                    val webSafeFallbackToken: String?)

}