package minefarts.smarttube.utils.next.v2.gen

import minefarts.smarttube.google.common.helpers.YouTubeHelper
import minefarts.smarttube.utils.browse.gen.PlaylistVideoListRenderer
import minefarts.smarttube.utils.browse.gen.Shelf
import minefarts.smarttube.utils.browse.gen.getItems
import com.liskovsoft.youtubeapi.common.models.gen.*

//////

public fun VideoOwnerItem.isSubscribed() = subscriptionButton?.subscribed ?: subscribed ?: subscribeButton?.subscribeButtonRenderer?.subscribed
    ?: navigationEndpoint?.getOverlayToggleButton()?.isToggled ?: navigationEndpoint?.getOverlaySubscribeButton()?.subscribed

public fun VideoOwnerItem.getChannelId() = navigationEndpoint?.getBrowseId() ?: subscribeButton?.subscribeButtonRenderer?.channelId

public fun VideoOwnerItem.getThumbnails() = thumbnail

public fun VideoOwnerItem.getParams() =
    navigationEndpoint?.getOverlayToggleButton()?.getParams() ?: navigationEndpoint?.getOverlaySubscribeButton()?.getParams()
    ?: subscribeButton?.subscribeButtonRenderer?.getParams()

public fun VideoOwnerItem.getNotificationPreference() = subscribeButton?.subscribeButtonRenderer?.notificationPreferenceButton

public fun VideoOwnerItem.getSubscriberCount() = subscriberCountText?.getText()
    ?: subscribeButton?.subscribeButtonRenderer?.longSubscriberCountText?.getText()

public fun VideoOwnerItem.getShortSubscriberCount() = subscribeButton?.subscribeButtonRenderer?.shortSubscriberCountText?.getText()

/////

private const val MARKER_TYPE_HEATMAP = "MARKER_TYPE_HEATMAP"
private const val MARKER_TYPE_CHAPTERS = "MARKER_TYPE_CHAPTERS"

private const val ICON_TYPE_SHUFFLE = "SHUFFLE"

private fun WatchNextResult.getWatchNextResults() = contents?.singleColumnWatchNextResults

private fun WatchNextResult.getPlayerOverlays() = playerOverlays?.playerOverlayRenderer

public fun WatchNextResult.getSuggestedSections() =
    getWatchNextResults()?.pivot?.let { it.pivot ?: it.sectionListRenderer }?.contents?.mapNotNull { it?.shelfRenderer }

public fun WatchNextResult.getVideoMetadata() = getWatchNextResults()?.results?.results?.contents?.getOrNull(0)
    ?.itemSectionRenderer?.contents?.map { it?.videoMetadataRenderer ?: it?.musicWatchMetadataRenderer }?.firstOrNull()

public fun WatchNextResult.getNextVideoItem() = getAutoplaySet()?.nextVideoRenderer?.getNextVideoItem()

public fun WatchNextResult.getAutoplayVideoItem() = getAutoplaySet()?.autoplayVideoRenderer?.getNextVideoItem()

public fun WatchNextResult.getShuffleVideoItem() =
    getWatchNextResults()?.pivot?.sectionListRenderer?.contents
        ?.firstOrNull()?.shelfRenderer?.headerRenderer?.shelfHeaderRenderer?.buttons
        ?.firstOrNull { it?.buttonRenderer?.icon?.iconType == ICON_TYPE_SHUFFLE }?.buttonRenderer?.navigationEndpoint

public fun WatchNextResult.getVideoDetails() = getReplayItemWrapper()?.pivotVideoRenderer

public fun WatchNextResult.getReplayItemWrapper() = getWatchNextResults()?.autoplay?.autoplay?.replayVideoRenderer

public fun WatchNextResult.getButtonStateItem() = transportControls?.transportControlsRenderer

public fun WatchNextResult.getLiveChatToken() =
    getWatchNextResults()?.conversationBar?.liveChatRenderer?.continuations?.getOrNull(0)?.reloadContinuationData?.continuation

public fun WatchNextResult.getPlaylistInfo() = getWatchNextResults()?.playlist?.playlist

public fun WatchNextResult.getChapters() = getPlayerOverlays()?.decoratedPlayerBarRenderer?.decoratedPlayerBarRenderer
    ?.playerBar?.multiMarkersPlayerBarRenderer?.markersMap?.firstOrNull()?.value?.chapters
    ?: engagementPanels?.firstNotNullOfOrNull { it?.engagementPanelSectionListRenderer?.content?.macroMarkersListRenderer?.contents }
    ?: frameworkUpdates?.entityBatchUpdate?.mutations
        ?.firstNotNullOfOrNull { it?.payload?.macroMarkersListEntity?.markersList?.takeIf { it.markerType == MARKER_TYPE_CHAPTERS }?.markers }

public fun WatchNextResult.getCommentPanel() = engagementPanels?.firstOrNull { it?.isCommentsSection() == true }

public fun WatchNextResult.getDescriptionPanel() = engagementPanels?.firstOrNull { it?.isDescriptionSection() == true }

public fun WatchNextResult.getCollaboratorPanel() = engagementPanels?.firstOrNull { it?.isCollaboratorSection() == true }

public fun WatchNextResult.isEmpty(): Boolean = getSuggestedSections()?.isEmpty() ?: true

private fun WatchNextResult.getAutoplaySet() = getWatchNextResults()?.autoplay?.autoplay?.sets?.getOrNull(0)

///////

public fun WatchNextResultContinuation.isEmpty(): Boolean = getItems() == null

public fun WatchNextResultContinuation.getItems(): List<ItemWrapper?>? = getGridContinuation()?.getItems()
    ?: getSectionContinuation()?.getItems()

public fun WatchNextResultContinuation.getContinuationToken(): String? = getGridContinuation()?.getContinuationToken()
    ?: getSectionContinuation()?.getContinuationToken()

public fun WatchNextResultContinuation.getShelves(): List<Shelf?>? = getSectionContinuation()?.getShelves()

private fun WatchNextResultContinuation.getGridContinuation() = continuationContents?.horizontalListContinuation
    ?: continuationContents?.gridContinuation
    ?: continuationContents?.playlistVideoListContinuation
    ?: continuationContents?.tvSurfaceContentContinuation?.content?.gridRenderer

private fun WatchNextResultContinuation.getSectionContinuation() = continuationContents?.sectionListContinuation
    ?: continuationContents?.tvSurfaceContentContinuation?.content?.sectionListRenderer

///////

public fun SectionListContinuation.getItems(): List<ItemWrapper?>? = getShelves()?.flatMap { it?.getItems() ?: emptyList() }

public fun SectionListContinuation.getContinuationToken(): String? = continuations?.getContinuationToken()

public fun SectionListContinuation.getShelves(): List<Shelf?>? = contents

///////

public fun GridContinuationWrapper.getItems(): List<ItemWrapper?>? = items ?: contents

public fun GridContinuationWrapper.getContinuationToken(): String? = continuations?.getContinuationToken()

///////

const val LIKE_STATUS_LIKE = "LIKE"
const val LIKE_STATUS_DISLIKE = "DISLIKE"
const val LIKE_STATUS_INDIFFERENT = "INDIFFERENT"

public fun VideoMetadataRenderer.getVideoOwner() = owner?.videoOwnerRenderer

public fun VideoMetadataRenderer.getTitle() = title?.getText()

public fun VideoMetadataRenderer.getLongViewCountText() = viewCount?.videoViewCountRenderer?.viewCount?.getText() ?: viewCountText?.getText()

public fun VideoMetadataRenderer.getViewCountText() = viewCount?.videoViewCountRenderer?.shortViewCount?.getText() ?: shortViewCountText?.getText()

public fun VideoMetadataRenderer.isLive() = viewCount?.videoViewCountRenderer?.isLive

public fun VideoMetadataRenderer.getDateText() = dateText?.getAccessibilityLabel() // contains relative published date (e.g. 1 hour ago)

public fun VideoMetadataRenderer.getPublishedTime() = publishedTimeText?.getText() ?: publishedTime?.getText()

public fun VideoMetadataRenderer.getAlbumName() = albumName?.getText()

public fun VideoMetadataRenderer.getLikeStatus() = likeStatus ?: likeButton?.likeButtonRenderer?.likeStatus

public fun VideoMetadataRenderer.getLikeCount() = likeStatus ?: likeButton?.likeButtonRenderer?.likeCountText?.getText()

public fun VideoMetadataRenderer.getLikeCountInt() = likeButton?.likeButtonRenderer?.likeCount

public fun VideoMetadataRenderer.isUpcoming() = badges?.firstNotNullOfOrNull { it?.upcomingEventBadge?.label?.getText() }?.let { true } ?: false

public fun VideoMetadataRenderer.getPercentWatched() =
    thumbnailOverlays?.firstNotNullOfOrNull { it?.thumbnailOverlayResumePlaybackRenderer?.percentDurationWatched } ?: 0

////////

private const val TYPE_CHANNEL = "TRANSPORT_CONTROLS_BUTTON_TYPE_CHANNEL_BUTTON"
private const val TYPE_SKIP_PREVIOUS = "TRANSPORT_CONTROLS_BUTTON_TYPE_SKIP_PREVIOUS"
private const val TYPE_SKIP_NEXT = "TRANSPORT_CONTROLS_BUTTON_TYPE_SKIP_NEXT"
private const val TYPE_LIKE = "TRANSPORT_CONTROLS_BUTTON_TYPE_LIKE_BUTTON"
private const val TYPE_DISLIKE = "TRANSPORT_CONTROLS_BUTTON_TYPE_DISLIKE_BUTTON"
private const val TYPE_ADD_TO_PLAYLIST = "TRANSPORT_CONTROLS_BUTTON_TYPE_ADD_TO_PLAYLIST"

private const val TVHTML5_SHELF_RENDERER_TYPE_SHORTS = "TVHTML5_SHELF_RENDERER_TYPE_SHORTS"

public fun ButtonStateItem.isLikeToggled() = likeButton?.toggleButtonRenderer?.isToggled ?: getButton(TYPE_LIKE)?.toggleButtonRenderer?.isToggled

public fun ButtonStateItem.isDislikeToggled() =
    dislikeButton?.toggleButtonRenderer?.isToggled ?: getButton(TYPE_DISLIKE)?.toggleButtonRenderer?.isToggled

public fun ButtonStateItem.isSubscribeToggled() = subscribeButton?.toggleButtonRenderer?.isToggled

public fun ButtonStateItem.getChannelId() = getChannelOwner()?.getChannelId()

public fun ButtonStateItem.getChannelOwner() = channelButton?.videoOwnerRenderer ?: getButton(TYPE_CHANNEL)?.videoOwnerRenderer

public fun ButtonStateItem.getLikeStatus() = getButton(TYPE_LIKE)?.likeButtonRenderer?.likeStatus

private fun ButtonStateItem.getButton(type: String) = buttons?.firstOrNull { it?.type == type }?.button

///////

public fun ShelfRenderer.getTitle() = title?.getText() ?: getShelf()?.title?.getText() ?: getShelf()?.avatarLockup?.avatarLockupRenderer?.title?.getText()

public fun ShelfRenderer.getItemWrappers() =
    content?.let { it.horizontalListRenderer?.items ?: it.expandedShelfContentsRenderer?.items ?: it.gridRenderer?.items }

public fun ShelfRenderer.getContinuationToken() = content?.horizontalListRenderer?.continuations?.getContinuationToken()

public fun ShelfRenderer.getChipItems() = headerRenderer?.chipCloudRenderer?.chips

public fun ShelfRenderer.containsShorts() = tvhtml5ShelfRendererType == TVHTML5_SHELF_RENDERER_TYPE_SHORTS

private fun ShelfRenderer.getShelf() = headerRenderer?.shelfHeaderRenderer

///////

public fun PlaylistVideoListRenderer.getContinuationToken() = continuations?.firstOrNull()?.getContinuationToken() ?: contents?.lastOrNull()?.getContinuationToken()

////////

/**
 * In some cases chip item contains multiple shelfs<br/>
 * Other regular shelfs in this case is empty
 */
public fun ChipItem.getShelfItems() = chipCloudChipRenderer?.content?.sectionListRenderer?.contents?.map { it?.shelfRenderer }

public fun ChipItem.getTitle() = chipCloudChipRenderer?.text?.getText()

//////

public fun NextVideoItem.getVideoId() = endpoint?.watchEndpoint?.videoId

public fun NextVideoItem.getTitle() = item?.previewButtonRenderer?.title?.getText()

public fun NextVideoItem.getAuthor() = item?.previewButtonRenderer?.byline?.getText()

public fun NextVideoItem.getThumbnails() = item?.previewButtonRenderer?.thumbnail

public fun NextVideoItem.getPlaylistId() = endpoint?.watchEndpoint?.playlistId

public fun NextVideoItem.getPlaylistIndex() = endpoint?.watchEndpoint?.index

public fun NextVideoItem.getParams() = endpoint?.watchEndpoint?.params

/////// Chapters wrapper

public fun ChapterItemWrapper.getTitle() = chapterRenderer?.getTitle() ?: macroMarkersListItemRenderer?.getTitle() ?: title?.toString()

public fun ChapterItemWrapper.getStartTimeMs() = chapterRenderer?.getStartTimeMs() ?: macroMarkersListItemRenderer?.getStartTimeMs() ?: startMillis?.toLong()

public fun ChapterItemWrapper.getThumbnailUrl() =
    chapterRenderer?.getThumbnailUrl() ?: macroMarkersListItemRenderer?.getThumbnailUrl() ?: thumbnailDetails?.getOptimalResThumbnailUrl()

/////// Chapters V1

public fun ChapterRenderer.getTitle() = title?.toString()

public fun ChapterRenderer.getStartTimeMs() = timeRangeStartMillis

public fun ChapterRenderer.getThumbnailUrl() = thumbnail?.getOptimalResThumbnailUrl()

/////// Chapters V2

public fun MacroMarkersListItemRenderer.getTitle() = title?.toString()

public fun MacroMarkersListItemRenderer.getStartTimeMs(): Long? = onTap?.watchEndpoint?.startTimeSeconds?.let { it.toLong() * 1_000 }

public fun MacroMarkersListItemRenderer.getThumbnailUrl() = thumbnail?.getOptimalResThumbnailUrl()

/////// Chapters V3 (replaced with ChapterItemWrapper)

public fun Marker.getTitle(): String? = title?.toString()

public fun Marker.getStartTimeMs(): Long? = startMillis?.toLong()

public fun Marker.getDurationTimeMs(): Long? = durationMillis?.toLong()

public fun Marker.getThumbnailUrl(): String? = thumbnailDetails?.getOptimalResThumbnailUrl()

///////

public fun ContinuationItem.getContinuationToken(): String? =
    nextContinuationData?.continuation ?: nextRadioContinuationData?.continuation ?: reloadContinuationData?.continuation

public fun ContinuationItem.getLabel(): String? = nextContinuationData?.label?.getText()

public fun List<ContinuationItem?>.getContinuationToken(): String? = firstNotNullOfOrNull { it?.getContinuationToken() }

///////

public fun EngagementPanel.getTopCommentsToken(): String? = getSubMenuItems()?.getOrNull(0)?.continuation?.getContinuationToken() ?:
    getSections()?.firstNotNullOfOrNull { it?.itemSectionRenderer?.continuations?.getContinuationToken() }

public fun EngagementPanel.getNewCommentsToken(): String? = getSubMenuItems()?.getOrNull(1)?.continuation?.getContinuationToken()

public fun EngagementPanel.isCommentsSection(): Boolean = engagementPanelSectionListRenderer?.panelIdentifier == "comment-item-section"

public fun EngagementPanel.isDescriptionSection(): Boolean = engagementPanelSectionListRenderer?.panelIdentifier == "video-description-ep-identifier"

public fun EngagementPanel.isCollaboratorSection(): Boolean = engagementPanelSectionListRenderer?.identifier?.tag?.startsWith("channel-actions-panel-") ?: false

public fun EngagementPanel.getTitle(): String? = getDescriptionHeader()?.title?.getText()

public fun EngagementPanel.getChannelName(): String? = getDescriptionHeader()?.channel?.getText()

public fun EngagementPanel.getViews(): String? = getDescriptionHeader()?.views?.getText()

public fun EngagementPanel.getPublishDate(): String? = getDescriptionHeader()?.publishDate?.getText()

public fun EngagementPanel.getBrowseId(): String? = getDescriptionHeader()?.channelNavigationEndpoint?.getBrowseId()

public fun EngagementPanel.getLikeCount(): String? = getDescriptionHeader()?.factoid?.firstOrNull()?.getValue()

public fun EngagementPanel.getDescriptionText(): String? = getDescriptionBody()?.descriptionBodyText?.getText()

public fun EngagementPanel.getThumbnails(): ThumbnailItem? = getHeader()?.image

public fun EngagementPanel.getSubscribersCount(): String? = getHeader()?.subtitle?.getText()?.split(YouTubeHelper.TEXT_DELIM_ALT)?.last()

private fun EngagementPanel.getDescriptionHeader(): VideoDescriptionHeaderRenderer? =
    getDescriptionItems()?.firstNotNullOfOrNull { it?.videoDescriptionHeaderRenderer }

private fun EngagementPanel.getDescriptionBody(): ExpandableVideoDescriptionBodyRenderer? =
    getDescriptionItems()?.firstNotNullOfOrNull { it?.expandableVideoDescriptionBodyRenderer }

private fun EngagementPanel.getSections() = engagementPanelSectionListRenderer?.content?.sectionListRenderer?.contents

private fun EngagementPanel.getSubMenuItems() =
    engagementPanelSectionListRenderer?.header?.engagementPanelTitleHeaderRenderer?.menu?.sortFilterSubMenuRenderer?.subMenuItems

private fun EngagementPanel.getDescriptionItems() = engagementPanelSectionListRenderer?.content?.structuredDescriptionContentRenderer?.items

private fun EngagementPanel.getHeader() = engagementPanelSectionListRenderer?.header?.overlayPanelHeaderRenderer

///////

// Presents on Album Music videos (array of: likes count, view count, published date)
public fun Factoid.getValue(): String? = factoidRenderer?.value?.getText()

public fun Factoid.getLabel(): String? = factoidRenderer?.label?.getText()

public fun Factoid.getAccessibilityText(): String? = factoidRenderer?.accessibilityText

///////

public fun NextVideoRenderer.getNextVideoItem() =
    maybeHistoryEndpointRenderer ?: autoplayEndpointRenderer ?: autoplayVideoWrapperRenderer?.primaryEndpointRenderer?.autoplayEndpointRenderer
