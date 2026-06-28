package minefarts.smarttube.utils.app.nsigsolver.provider

public enum class JsChallengeType(val value: String) {
    N("n"),
    SIG("sig");
}

public data class JsChallengeRequest(
    val type: JsChallengeType,
    val input: ChallengeInput,
    val videoId: String? = null
)

public data class ChallengeInput(
    val playerUrl: String,
    val challenges: List<String>
)

public data class ChallengeOutput(
    val results: Map<String, String>
)

public data class JsChallengeProviderResponse(
    val request: JsChallengeRequest,
    val response: JsChallengeResponse? = null,
    val error: Exception? = null
)

public data class JsChallengeResponse(
    val type: JsChallengeType,
    val output: ChallengeOutput
)
