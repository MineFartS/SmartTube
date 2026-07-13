package minefarts.smarttube.utils.app.potoken

import android.util.Base64

public class PoTokenException(message: String) : RuntimeException(message)

// to be thrown if the WebView provided by the system is broken
public class BadWebViewException(message: String) : RuntimeException(message)

public fun buildExceptionForJsError(error: String): Throwable {
    return if (error.contains("SyntaxError"))
        BadWebViewException(error)
    else
        PoTokenException(error)
}

public data class BotGuardConfig(
    val api: PoTokenApi, 
    val identifier: String, 
    val requestKey: String
)

public open class BGError(
    val code: String,
    message: String,
    val info: Map<String, Any>? = null
) : Exception(message)

public fun u8ToBase64(u8: ByteArray, base64url: Boolean = false): String {
    val flags = if (base64url) {
        Base64.NO_PADDING or Base64.NO_WRAP or Base64.URL_SAFE
    } else {
        Base64.NO_WRAP
    }

    return Base64.encodeToString(u8, flags)
}

