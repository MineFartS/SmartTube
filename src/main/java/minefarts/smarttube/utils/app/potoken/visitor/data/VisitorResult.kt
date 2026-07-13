package minefarts.smarttube.utils.app.potoken.visitor.data

public data class VisitorResult(val responseContext: ResponseContext?) {
    data class ResponseContext(val visitorData: String?)
}
