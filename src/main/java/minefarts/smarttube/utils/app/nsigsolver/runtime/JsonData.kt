package minefarts.smarttube.utils.app.nsigsolver.runtime

import com.google.gson.reflect.TypeToken

public val solverOutputType = object : TypeToken<SolverOutput>() {}.type

public data class SolverOutput(
    val type: String,
    val error: String?,
    val preprocessed_player: String?,
    val responses: List<ResponseData>
)

public data class ResponseData(
    val type: String,
    val error: String?,
    val data: Map<String, String>
)