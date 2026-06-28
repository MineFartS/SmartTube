package minefarts.smarttube.utils.app.nsigsolver.provider

public class InfoExtractorError(message: String, cause: Exception? = null): Exception(message, cause)

public open class ContentProviderError(message: String, cause: Exception? = null): Exception(message, cause)

/**
 * Reject the JsChallengeRequest (cannot handle the request)
 */
public class JsChallengeProviderRejectedRequest(message: String, cause: Exception? = null): ContentProviderError(message, cause)

/**
 * An error occurred while solving the challenge
 */
public class JsChallengeProviderError(message: String, cause: Exception? = null): ContentProviderError(message, cause)