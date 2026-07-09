package minefarts.smarttube.utils.app.potokennp2

public class PoTokenException(message: String) : RuntimeException(message)

// to be thrown if the WebView provided by the system is broken
public class BadWebViewException(message: String) : RuntimeException(message)

public fun buildExceptionForJsError(error: String): Throwable {
    return if (error.contains("SyntaxError"))
        BadWebViewException(error)
    else
        PoTokenException(error)
}
