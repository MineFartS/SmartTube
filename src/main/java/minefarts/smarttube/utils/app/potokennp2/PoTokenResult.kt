package minefarts.smarttube.utils.app.potokennp2

import javax.annotation.Nonnull
import javax.annotation.Nullable

/**
 * The result of a supported/successful {@code poToken} extraction request by a
 * {@link PoTokenProvider}.
 */
data class PoTokenResult(

    @field:Nonnull
    val videoId: String,

    @field:Nonnull
    val visitorData: String,

    @field:Nonnull
    val playerRequestPoToken: String,

    @field:Nullable
    val streamingDataPoToken: String?

)
