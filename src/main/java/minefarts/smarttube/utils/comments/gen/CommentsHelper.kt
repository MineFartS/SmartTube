package minefarts.smarttube.utils.comments.gen

import com.liskovsoft.youtubeapi.common.models.gen.getAccessibilityLabel
import com.liskovsoft.youtubeapi.common.models.gen.getContinuations
import com.liskovsoft.youtubeapi.common.models.gen.getDefaultParams
import com.liskovsoft.youtubeapi.common.models.gen.getToggleParams
import minefarts.smarttube.utils.next.v2.gen.getContinuationToken

public fun CommentsResult.getComments(): List<CommentItemWrapper?>? = continuationContents?.itemSectionContinuation?.contents
public fun CommentsResult.getContinuationKey(): String? = continuationContents?.itemSectionContinuation?.continuations
    ?.getContinuationToken()
public fun CommentsResult.getActiveCommentItem(): CommentRenderer? = getComments()?.getOrNull(0)?.commentRenderer

public fun CommentItemWrapper.getCommentItem() = commentThreadRenderer?.comment?.commentRenderer

public fun CommentRenderer.getContinuationKey() = detailViewEndpoint?.getContinuations()?.getContinuationToken()
public fun CommentRenderer.getContinuationLabel() = repliesCount?.getAccessibilityLabel()
public fun CommentRenderer.getLikeParams() = actionButtons?.commentActionButtonsRenderer?.likeButton?.toggleButtonRenderer?.getDefaultParams()
public fun CommentRenderer.getUnLikeParams() = actionButtons?.commentActionButtonsRenderer?.likeButton?.toggleButtonRenderer?.getToggleParams()
public fun CommentRenderer.getDislikeParams() = actionButtons?.commentActionButtonsRenderer?.dislikeButton?.toggleButtonRenderer?.getDefaultParams()
public fun CommentRenderer.getUnDislikeParams() = actionButtons?.commentActionButtonsRenderer?.dislikeButton?.toggleButtonRenderer?.getToggleParams()