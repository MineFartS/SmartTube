package minefarts.smarttube.utils.common.models.gen

import minefarts.smarttube.google.common.helpers.YouTubeHelper
import minefarts.smarttube.utils.data.MediaItem
import minefarts.smarttube.utils.helpers.DateHelper
import minefarts.smarttube.utils.browse.gen.getContinuationToken
import minefarts.smarttube.utils.browse.gen.getThumbnails
import minefarts.smarttube.utils.browse.gen.getVideoId
import minefarts.smarttube.google.common.helpers.ServiceHelper
import minefarts.smarttube.utils.next.v2.gen.getContinuationToken

// A badge before the image
private const val BADGE_STYLE_LIVE = "LIVE"
private const val BADGE_STYLE_UPCOMING = "UPCOMING"
private const val BADGE_STYLE_SHORTS = "SHORTS"
private const val BADGE_STYLE_DEFAULT = "DEFAULT"

// A badge before the subtitle
private const val STATUS_STYLE_MOVIE = "BADGE_STYLE_TYPE_YPC" // This mark sometimes presents on regular videos (e.g. fundraiser mark)
private const val STATUS_STYLE_QUALITY = "BADGE_STYLE_TYPE_SIMPLE"
private const val STATUS_STYLE_LIVE = "BADGE_STYLE_TYPE_LIVE_NOW"

///////////

public fun TextItem.getText() = runs?.joinToString("") { it?.text ?: it?.emoji?.getText() ?: "" } ?: simpleText ?: content
public fun TextItem.getAccessibilityLabel() = accessibility?.accessibilityData?.label

/**
 * Use shortcut name as workaround to display custom emoji. Custom emoji are images.
 */
//fun LiveChatEmoji.getText() = if (isCustomEmoji == true) shortcuts?.getOrElse(0) { "" } else emojiId
/**
 * Use empty string as workaround to display custom emoji. Custom emoji are images.
 */
public fun LiveChatEmoji.getText() = if (isCustomEmoji == true) "" else emojiId

/**
 * Find optimal thumbnail for tv screen
 */
public fun ThumbnailItem.getOptimalResThumbnailUrl() = (thumbnails ?: sources)?.getOrElse(YouTubeHelper.OPTIMAL_RES_THUMBNAIL_INDEX) { (thumbnails ?: sources)?.lastOrNull() } ?.getUrl()

public fun ThumbnailItem.getHighResThumbnailUrl() = (thumbnails ?: sources)?.lastOrNull()?.getUrl()

public fun ThumbnailItem.Thumbnail.getUrl(): String? {
    var newUrl = if (url?.startsWith("//") == true) "https:$url" else url
    newUrl = YouTubeHelper.avatarBlockFix(newUrl)
    return newUrl
}

////////

public fun NavigationEndpointItem.getBrowseId() = browseEndpoint?.browseId

public fun NavigationEndpointItem.getParams() = browseEndpoint?.params ?: watchEndpoint?.params

public fun NavigationEndpointItem.getOverlayToggleButton() = getOverlayItems()?.firstNotNullOfOrNull { it?.toggleButtonRenderer }

public fun NavigationEndpointItem.getOverlaySubscribeButton() = getOverlayItems()?.firstNotNullOfOrNull { it?.subscribeButtonRenderer }

public fun NavigationEndpointItem.isSubscribed() = getOverlaySubscribeButton()?.subscribed

public fun NavigationEndpointItem.getContinuations() = getOverlayContent()?.itemSectionRenderer?.continuations
    ?: getEngagementContents()?.firstOrNull()?.itemSectionRenderer?.continuations

public fun NavigationEndpointItem.getTitle() = getOverlayHeader()?.title?.getText()

public fun NavigationEndpointItem.getSubtitle() = getOverlayHeader()?.subtitle?.getText()

public fun NavigationEndpointItem.getStartTimeSeconds() = watchEndpoint?.startTimeSeconds

public fun NavigationEndpointItem.getVideoId() = watchEndpoint?.videoId ?: reelWatchEndpoint?.videoId

public fun NavigationEndpointItem.getPlaylistId() = watchEndpoint?.playlistId ?: watchPlaylistEndpoint?.playlistId

public fun NavigationEndpointItem.getIndex() = watchEndpoint?.index

public fun NavigationEndpointItem.getFeedbackToken() =
    getOverlayItems()?.firstNotNullOfOrNull {
        it?.compactLinkRenderer?.serviceEndpoint?.commandExecutorCommand?.commands?.firstNotNullOfOrNull { it.feedbackEndpoint?.feedbackToken }
    }

private fun NavigationEndpointItem.getOverlayPanel() = openPopupAction?.popup?.overlaySectionRenderer?.overlay
    ?.overlayTwoPanelRenderer?.actionPanel?.overlayPanelRenderer

private fun NavigationEndpointItem.getEngagementPanel() = showEngagementPanelEndpoint?.engagementPanel?.engagementPanelSectionListRenderer

private fun NavigationEndpointItem.getOverlayContent() = getOverlayPanel()?.content

private fun NavigationEndpointItem.getOverlayHeader() = getOverlayPanel()?.header?.overlayPanelHeaderRenderer

private fun NavigationEndpointItem.getOverlayItems() = getOverlayContent()?.overlayPanelItemListRenderer?.items

private fun NavigationEndpointItem.getEngagementContents() = getEngagementPanel()?.content?.sectionListRenderer?.contents

private fun NavigationEndpointItem.getQuery() = searchEndpoint?.query

////////

private const val MENU_ICON_TYPE_NOT_INTERESTED = "NOT_INTERESTED"
private const val MENU_ICON_TYPE_REMOVE = "REMOVE"

public fun MenuWrapper.getBrowseId() = menuRenderer?.items?.firstNotNullOfOrNull { it?.getBrowseId() }

public fun MenuWrapper.getPlaylistId() = menuRenderer?.items?.firstNotNullOfOrNull { it?.getPlaylistId() }

public fun MenuWrapper.getVideoId() = menuRenderer?.items?.firstNotNullOfOrNull { it?.getVideoId() }

public fun MenuWrapper.getNotificationToken() = menuRenderer?.items?.firstNotNullOfOrNull { it?.getNotificationToken() }

public fun MenuWrapper.getFeedbackTokens(): List<String?>? = menuRenderer?.items?.mapNotNull { it?.getFeedbackToken() }

// Filter by icon not robust. Icon item not always present.
public fun MenuWrapper.getVideoToken() = menuRenderer?.items?.firstOrNull {
    it?.getIconType() == MENU_ICON_TYPE_NOT_INTERESTED
}?.getFeedbackToken()

    // Filter by icon not robust. Icon item not always present.
public fun MenuWrapper.getChannelToken() = menuRenderer?.items?.firstOrNull {
    it?.getIconType() == MENU_ICON_TYPE_REMOVE
}?.getFeedbackToken()

//////////

// gridVideoRenderer
public fun VideoItem.getTitle() = title?.getText() ?: headline?.getText()

public fun VideoItem.getVideoId() = videoId

public fun VideoItem.getThumbnails() = thumbnail

public fun VideoItem.getMovingThumbnails() = richThumbnail?.movingThumbnailRenderer?.movingThumbnailDetails

public fun VideoItem.getSubTitle() = badges?.getOrNull(0)?.metadataBadgeRenderer?.label

public fun VideoItem.getLengthText() = lengthText?.getText()

public fun VideoItem.getPercentWatched() = thumbnailOverlays?.firstNotNullOfOrNull { it?.thumbnailOverlayResumePlaybackRenderer?.percentDurationWatched }

public fun VideoItem.getStartTimeSeconds() = navigationEndpoint?.getStartTimeSeconds()

public fun VideoItem.getBadgeText() = thumbnailOverlays?.firstNotNullOfOrNull { it?.thumbnailOverlayTimeStatusRenderer?.text?.getText() } ?:
    badges?.firstNotNullOfOrNull { it?.liveBadge?.label?.getText() ?: it?.upcomingEventBadge?.label?.getText() }

public fun VideoItem.getUserName() = shortBylineText?.getText() ?: longBylineText?.getText()

public fun VideoItem.getPublishedTimeText() = publishedTimeText?.getText()

public fun VideoItem.getViewCount() = shortViewCountText?.getText() ?: viewCountText?.getText() ?: videoInfo?.getText()

// No real date, just placeholder. We should do this themselves.
public fun VideoItem.getUpcomingEventText() = upcomingEventData?.upcomingEventText?.getText()
    ?.replace("DATE_PLACEHOLDER", DateHelper.toShortDate(upcomingEventData.getStartTimeMs(), true, true, true))

public fun VideoItem.getChannelId() =
    shortBylineText?.runs?.firstNotNullOfOrNull { it?.navigationEndpoint?.getBrowseId() } ?:
    longBylineText?.runs?.firstNotNullOfOrNull { it?.navigationEndpoint?.getBrowseId() } ?:
    menu?.getBrowseId()

public fun VideoItem.getPlaylistId() = navigationEndpoint?.getPlaylistId()

public fun VideoItem.getPlaylistIndex() = navigationEndpoint?.getIndex()

public fun VideoItem.isLive(): Boolean = STATUS_STYLE_LIVE == getStatusStyle() || BADGE_STYLE_LIVE == getBadgeStyle()

public fun VideoItem.isUpcoming() = BADGE_STYLE_UPCOMING == getBadgeStyle()

public fun VideoItem.isShorts() = BADGE_STYLE_SHORTS == getBadgeStyle()

public fun VideoItem.isMovie() = STATUS_STYLE_MOVIE == getStatusStyle() && getVideoId() == null

public fun VideoItem.getFeedbackTokens() = menu?.getFeedbackTokens()

private fun VideoItem.getStatusStyle() = badges?.firstNotNullOfOrNull { it?.metadataBadgeRenderer?.style }

private fun VideoItem.getBadgeStyle() = thumbnailOverlays?.firstNotNullOfOrNull { it?.thumbnailOverlayTimeStatusRenderer?.style }

////////////

public fun MusicItem.getTitle() = primaryText?.getText()

public fun MusicItem.getUserName() = secondaryText?.getText()

public fun MusicItem.getThumbnails() = thumbnail

public fun MusicItem.getVideoId() = navigationEndpoint?.getVideoId()

public fun MusicItem.getPlaylistId() = navigationEndpoint?.getPlaylistId()

public fun MusicItem.getBadgeText() = lengthText?.getText()

public fun MusicItem.getLengthText() = lengthText?.getText()

public fun MusicItem.getViewsAndPublished() = tertiaryText?.getText()

public fun MusicItem.getChannelId() = menu?.getBrowseId()

public fun MusicItem.getPlaylistIndex() = navigationEndpoint?.getIndex()

public fun MusicItem.getSubTitle() = null

public fun MusicItem.getViewsCountText() = null

public fun MusicItem.getUpcomingEventText() = null

public fun MusicItem.isLive() = false

public fun MusicItem.isUpcoming() = false

////////////

public fun RadioItem.getTitle() = title?.getText()

public fun RadioItem.getUserName() = subtitle?.getText()

public fun RadioItem.getThumbnails() = thumbnail ?: thumbnailRenderer?.musicThumbnailRenderer?.thumbnail

public fun RadioItem.getVideoId() = navigationEndpoint?.getVideoId() ?: menu?.getVideoId()

public fun RadioItem.getPlaylistId() = navigationEndpoint?.getPlaylistId() ?: menu?.getPlaylistId()

public fun RadioItem.getBadgeText() = null

public fun RadioItem.getLengthText() = null

public fun RadioItem.getViewsAndPublished() = null

//public fun RadioItem.getChannelId() = navigationEndpoint?.getBrowseId() ?: menu?.getBrowseId()
public fun RadioItem.getChannelId() = menu?.getBrowseId()

public fun RadioItem.getPlaylistIndex() = navigationEndpoint?.getIndex()

public fun RadioItem.getSubTitle() = null

public fun RadioItem.getViewsCountText() = null

public fun RadioItem.getUpcomingEventText() = null

public fun RadioItem.isLive() = false

public fun RadioItem.isUpcoming() = false

///////////

// 'tileRenderer.style' values:
private const val TILE_CONTENT_TYPE_UNDEFINED = "UNDEFINED"

private const val TILE_CONTENT_TYPE_CHANNEL = "TILE_CONTENT_TYPE_CHANNEL"

private const val TILE_CONTENT_TYPE_PLAYLIST = "TILE_CONTENT_TYPE_PLAYLIST"

private const val TILE_CONTENT_TYPE_VIDEO = "TILE_CONTENT_TYPE_VIDEO"

private const val TILE_CONTENT_TYPE_EDU = "TILE_CONTENT_TYPE_EDU" // a search query tile in Home section

// 'tileRenderer.contentType' values:
private const val TILE_STYLE_DEFAULT = "TILE_STYLE_YTLR_DEFAULT"
private const val TILE_STYLE_SHORTS = "TILE_STYLE_YTLR_SHORTS"
private const val TILE_STYLE_QUERY = "TILE_STYLE_YTLR_EDU" // a search query tile in Home section

public fun TileItem.getTitle() = metadata?.tileMetadataRenderer?.title?.getText()
    ?: header?.tileHeaderRenderer?.thumbnailOverlays?.firstNotNullOfOrNull { it?.tileMetadataRenderer?.title?.getText() }
    ?: header?.trackTileHeaderRenderer?.title?.getText()

public fun TileItem.getVideoId() = onSelectCommand?.getVideoId()

public fun TileItem.getPlaylistId() = onSelectCommand?.getPlaylistId() ?: getMenu()?.getPlaylistId()

public fun TileItem.getPlaylistIndex() = 0

public fun TileItem.getSubTitle() = metadata?.tileMetadataRenderer?.lines?.map { it?.lineRenderer?.items?.getOrNull(0)?.lineItemRenderer?.badge?.metadataBadgeRenderer?.label }?.firstOrNull()

public fun TileItem.getBadgeText() = header?.tileHeaderRenderer?.thumbnailOverlays?.firstNotNullOfOrNull { it?.thumbnailOverlayTimeStatusRenderer?.text?.getText() }
    ?: header?.trackTileHeaderRenderer?.duration?.getText()

public fun TileItem.getPercentWatched() = header?.tileHeaderRenderer?.thumbnailOverlays?.firstNotNullOfOrNull { it?.thumbnailOverlayResumePlaybackRenderer?.percentDurationWatched }

public fun TileItem.getStartTimeSeconds() = onSelectCommand?.getStartTimeSeconds()

public fun TileItem.getUserName() = null

public fun TileItem.getPublishedTime() = null

public fun TileItem.getViewCountText() =
    YouTubeHelper.createInfo(*metadata?.tileMetadataRenderer?.lines?.map {
        ServiceHelper.combineItems(" ", *it?.lineRenderer?.items?.map { it?.lineItemRenderer?.text }?.toTypedArray() ?: emptyArray())
    }?.toTypedArray() ?: emptyArray())

public fun TileItem.getUpcomingEventText() = null

public fun TileItem.getThumbnails() = header?.tileHeaderRenderer?.thumbnail ?: header?.trackTileHeaderRenderer?.thumbnail

public fun TileItem.getMovingThumbnails() = header?.tileHeaderRenderer?.let { it.movingThumbnail ?: it.onFocusThumbnail }

public fun TileItem.getMovingThumbnailUrl() = header?.tileHeaderRenderer?.movingThumbnail?.thumbnails?.getOrNull(0)?.url

public fun TileItem.getChannelId() = onSelectCommand?.getBrowseId() ?: getMenu()?.getBrowseId()

public fun TileItem.getChannelParams() = onSelectCommand?.getParams()

public fun TileItem.getFeedbackTokens() = getMenu()?.getFeedbackTokens()

public fun TileItem.isLive() = BADGE_STYLE_LIVE == getBadgeStyle()

public fun TileItem.getContentType() = contentType

public fun TileItem.getRichTextTileText() = header?.richTextTileHeaderRenderer?.textContent?.get(0)?.getText()

public fun TileItem.getContinuationToken() = onSelectCommand?.getContinuations()?.getContinuationToken()

public fun TileItem.isUpcoming() = BADGE_STYLE_UPCOMING == getBadgeStyle()

public fun TileItem.isMovie() = STATUS_STYLE_MOVIE == getStatusStyle() && getVideoId() == null // a movie has browseId instead of videoId

public fun TileItem.isShorts() = BADGE_STYLE_SHORTS == getBadgeStyle() || TILE_STYLE_SHORTS == getTileStyle()

public fun TileItem.getQuery() = onSelectCommand?.getQuery()

private fun TileItem.Header.getBadgeStyle() = tileHeaderRenderer?.thumbnailOverlays?.firstNotNullOfOrNull { it?.thumbnailOverlayTimeStatusRenderer?.style }

private fun TileItem.Metadata.getStatusStyle() = tileMetadataRenderer?.lines?.firstNotNullOfOrNull { it?.lineRenderer?.items?.firstNotNullOfOrNull { it?.lineItemRenderer?.badge?.metadataBadgeRenderer?.style } }

private fun TileItem.getMenu() = menu ?: onLongPressCommand?.showMenuCommand?.menu

private fun TileItem.getTileStyle() = style

private fun TileItem.getBadgeStyle() = header?.getBadgeStyle()

private fun TileItem.getStatusStyle() = metadata?.getStatusStyle()

////////////

public fun PlaylistItem.getTitle() = title?.getText()

public fun PlaylistItem.getPlaylistId() = playlistId

public fun PlaylistItem.getThumbnails() = thumbnail ?: thumbnails?.getOrNull(0)

public fun PlaylistItem.getBadgeText() = videoCountText?.getText()

////////////

public fun ChannelItem.getTitle() = title?.getText()

public fun ChannelItem.getThumbnails() = thumbnail

public fun ChannelItem.getChannelId() = channelId

public fun ChannelItem.getBadgeText() = videoCountText?.getText()

public fun ChannelItem.getSubTitle() = subscriberCountText?.getText()

////////////

public fun ShortsItem.getTitle() = overlayMetadata?.primaryText?.getText()

public fun ShortsItem.getSubTitle() = overlayMetadata?.secondaryText?.getText()

public fun ShortsItem.getVideoId() = onTap?.innertubeCommand?.reelWatchEndpoint?.getVideoId()

public fun ShortsItem.getThumbnails() = onTap?.innertubeCommand?.reelWatchEndpoint?.getThumbnails()

////////////

public fun LockupItem.getTitle() = metadata?.lockupMetadataViewModel?.title?.getText()

public fun LockupItem.getSubTitle() = YouTubeHelper.createInfo(
    *metadata?.lockupMetadataViewModel?.metadata?.contentMetadataViewModel?.metadataRows?.mapNotNull {
        it?.metadataParts?.mapNotNull { it?.text?.getText() } }?.flatten()?.toTypedArray() ?: emptyArray<String>()
)

public fun LockupItem.getVideoId() = getWatchEndpoint()?.videoId

public fun LockupItem.getPlaylistId() = getWatchEndpoint()?.playlistId

public fun LockupItem.getThumbnails() = getThumbnailView()?.image

public fun LockupItem.getBadgeText() = getBadge()?.text

public fun LockupItem.isLive() = BADGE_STYLE_LIVE == getBadge()?.badgeStyle

public fun LockupItem.getPercentWatched() = getOverlays()?.firstNotNullOfOrNull {
    it?.thumbnailBottomOverlayViewModel?.progressBar?.thumbnailOverlayProgressBarViewModel?.startPercent }

// The video without a badge, probably Watch again
public fun LockupItem.isEmpty() = getPercentWatched() == 100 && getBadgeText() == null

public fun LockupItem.getFeedbackTokens() =
    metadata?.lockupMetadataViewModel?.menuButton?.buttonViewModel?.onTap?.innertubeCommand?.showSheetCommand?.panelLoadingStrategy
        ?.inlineContent?.sheetViewModel?.content?.listViewModel?.listItems?.mapNotNull {
            it?.listItemViewModel?.rendererContext?.commandContext?.onTap?.innertubeCommand?.feedbackEndpoint?.feedbackToken
        }

private fun LockupItem.getBadge() = getOverlays()?.firstNotNullOfOrNull {
    it?.thumbnailOverlayBadgeViewModel }?.thumbnailBadges?.firstNotNullOfOrNull { it?.thumbnailBadgeViewModel }

private fun LockupItem.getOverlays() = getThumbnailView()?.overlays

private fun LockupItem.getThumbnailView() = contentImage?.thumbnailViewModel ?: contentImage?.collectionThumbnailViewModel?.primaryThumbnail?.thumbnailViewModel

private fun LockupItem.getWatchEndpoint() = rendererContext?.commandContext?.onTap?.innertubeCommand?.watchEndpoint

////////////

private fun ItemWrapper.getVideoItem() = gridVideoRenderer ?: videoRenderer ?: pivotVideoRenderer ?: compactVideoRenderer ?: reelItemRenderer ?: playlistVideoRenderer

private fun ItemWrapper.getMusicItem() = tvMusicVideoRenderer

private fun ItemWrapper.getChannelItem() = gridChannelRenderer ?: pivotChannelRenderer ?: compactChannelRenderer

private fun ItemWrapper.getPlaylistItem() = gridPlaylistRenderer ?: pivotPlaylistRenderer ?: compactPlaylistRenderer ?: playlistRenderer

private fun ItemWrapper.getRadioItem() = gridRadioRenderer ?: pivotRadioRenderer ?: compactRadioRenderer ?: musicTwoRowItemRenderer

private fun ItemWrapper.getTileItem() = tileRenderer

private fun ItemWrapper.getContinuationItem() = continuationItemRenderer

private fun ItemWrapper.getShortsItem() = shortsLockupViewModel

private fun ItemWrapper.getLockupItem() = lockupViewModel

public fun ItemWrapper.getType(): Int {
    if (getChannelItem() != null)
        return MediaItem.TYPE_CHANNEL
    if (getPlaylistItem() != null)
        return MediaItem.TYPE_PLAYLIST
    if (getRadioItem() != null)
        return MediaItem.TYPE_PLAYLIST
    if (getVideoItem() != null)
        return MediaItem.TYPE_VIDEO
    if (getMusicItem() != null)
        return MediaItem.TYPE_MUSIC
    if (getTileItem() != null)
        return when (getTileItem()?.getContentType()) {
            TILE_CONTENT_TYPE_CHANNEL -> MediaItem.TYPE_CHANNEL
            TILE_CONTENT_TYPE_PLAYLIST -> MediaItem.TYPE_PLAYLIST
            TILE_CONTENT_TYPE_VIDEO -> MediaItem.TYPE_VIDEO
            else -> MediaItem.TYPE_UNDEFINED
        }

    return MediaItem.TYPE_UNDEFINED
}

public fun ItemWrapper.getVideoId() = getVideoItem()?.getVideoId() ?: getMusicItem()?.getVideoId() ?: getTileItem()?.getVideoId() ?: getRadioItem()?.getVideoId()
    ?: getShortsItem()?.getVideoId() ?: getLockupItem()?.getVideoId()

public fun ItemWrapper.getTitle() = getVideoItem()?.getTitle() ?: getMusicItem()?.getTitle() ?: getTileItem()?.getTitle() ?: getPlaylistItem()?.getTitle()
    ?: getChannelItem()?.getTitle() ?: getRadioItem()?.getTitle() ?: getShortsItem()?.getTitle() ?: getLockupItem()?.getTitle()

public fun ItemWrapper.getThumbnails() = getVideoItem()?.getThumbnails() ?: getMusicItem()?.getThumbnails() ?: getTileItem()?.getThumbnails()
    ?: getPlaylistItem()?.getThumbnails() ?: getChannelItem()?.getThumbnails() ?: getRadioItem()?.getThumbnails() ?: getShortsItem()?.getThumbnails() ?: getLockupItem()?.getThumbnails()

public fun ItemWrapper.getMovingThumbnails() = getVideoItem()?.getMovingThumbnails() ?: getTileItem()?.getMovingThumbnails()

public fun ItemWrapper.getSubTitle() = getVideoItem()?.getSubTitle() ?: getMusicItem()?.getSubTitle() ?: getTileItem()?.getSubTitle()
    ?: getChannelItem()?.getSubTitle() ?: getShortsItem()?.getSubTitle() ?: getLockupItem()?.getSubTitle()

public fun ItemWrapper.getLengthText() = getVideoItem()?.getLengthText() ?: getMusicItem()?.getLengthText() ?: getTileItem()?.getBadgeText()

public fun ItemWrapper.getBadgeText() = getVideoItem()?.getBadgeText() ?: getMusicItem()?.getBadgeText() ?: getTileItem()?.getBadgeText()
?: getPlaylistItem()?.getBadgeText() ?: getChannelItem()?.getBadgeText() ?: getLockupItem()?.getBadgeText()

public fun ItemWrapper.getPercentWatched() = getVideoItem()?.getPercentWatched() ?: getTileItem()?.getPercentWatched() ?: getLockupItem()?.getPercentWatched()

public fun ItemWrapper.getStartTimeSeconds() = getVideoItem()?.getStartTimeSeconds() ?: getTileItem()?.getStartTimeSeconds()

public fun ItemWrapper.getUserName() = getVideoItem()?.getUserName() ?: getMusicItem()?.getUserName() ?: getTileItem()?.getUserName()
    ?: getRadioItem()?.getUserName()

public fun ItemWrapper.getPublishedTime() = getVideoItem()?.getPublishedTimeText() ?: getMusicItem()?.getViewsAndPublished() ?: getTileItem()?.getPublishedTime()

public fun ItemWrapper.getViewCountText() = getVideoItem()?.getViewCount() ?: getMusicItem()?.getViewsCountText() ?: getTileItem()?.getViewCountText()

public fun ItemWrapper.getUpcomingEventText() = getVideoItem()?.getUpcomingEventText() ?: getMusicItem()?.getUpcomingEventText()
    ?: getTileItem()?.getUpcomingEventText()

public fun ItemWrapper.getPlaylistId() = getVideoItem()?.getPlaylistId() ?: getMusicItem()?.getPlaylistId() ?: getTileItem()?.getPlaylistId() ?: getLockupItem()?.getPlaylistId()
    ?: getPlaylistItem()?.getPlaylistId() ?: getRadioItem()?.getPlaylistId()

public fun ItemWrapper.getChannelId() = getVideoItem()?.getChannelId() ?: getMusicItem()?.getChannelId() ?: getTileItem()?.getChannelId()
    ?: getChannelItem()?.getChannelId() ?: getRadioItem()?.getChannelId()

public fun ItemWrapper.getChannelParams() = getTileItem()?.getChannelParams()

public fun ItemWrapper.getPlaylistIndex() = getVideoItem()?.getPlaylistIndex() ?: getMusicItem()?.getPlaylistIndex() ?: getTileItem()?.getPlaylistIndex()

public fun ItemWrapper.isLive() = getVideoItem()?.isLive() ?: getMusicItem()?.isLive() ?: getTileItem()?.isLive() ?: getLockupItem()?.isLive() ?: false

public fun ItemWrapper.isUpcoming() = getVideoItem()?.isUpcoming() ?: getMusicItem()?.isUpcoming() ?: getTileItem()?.isUpcoming() ?: false

public fun ItemWrapper.isMovie() = getVideoItem()?.isMovie() ?: getTileItem()?.isMovie() ?: false

public fun ItemWrapper.isShorts() = reelItemRenderer != null || shortsLockupViewModel != null || getVideoItem()?.isShorts() ?: getTileItem()?.isShorts() ?: false

public fun ItemWrapper.getDescriptionText() = getTileItem()?.getRichTextTileText()

public fun ItemWrapper.getContinuationToken() = getTileItem()?.getContinuationToken() ?: getContinuationItem()?.getContinuationToken()

public fun ItemWrapper.getFeedbackToken() = getFeedbackTokens()?.getOrNull(0)

public fun ItemWrapper.getFeedbackToken2() = getFeedbackTokens()?.getOrNull(1)

public fun ItemWrapper.isEmpty() = getLockupItem()?.isEmpty() ?: false

public fun ItemWrapper.getQuery() = getTileItem()?.getQuery()

private fun ItemWrapper.getFeedbackTokens() = getVideoItem()?.getFeedbackTokens() ?: getTileItem()?.getFeedbackTokens() ?: getLockupItem()?.getFeedbackTokens()

/////

public fun DefaultServiceEndpoint.getChannelIds() = getSubscribeEndpoint()?.channelIds

public fun DefaultServiceEndpoint.getParams() = getSubscribeEndpoint()?.params

private fun DefaultServiceEndpoint.getSubscribeEndpoint() = authDeterminedCommand?.authenticatedCommand?.subscribeEndpoint

/////

public fun ToggledServiceEndpoint.getParams() = subscribeEndpoint?.params ?: unsubscribeEndpoint?.params ?: performCommentActionEndpoint?.action

/////

public fun ToggleButtonRenderer.getParams() = defaultServiceEndpoint?.getParams() ?: toggledServiceEndpoint?.getParams()

public fun ToggleButtonRenderer.getDefaultParams() = defaultServiceEndpoint?.getParams()

public fun ToggleButtonRenderer.getToggleParams() = toggledServiceEndpoint?.getParams()

//////

public fun SubscribeButtonRenderer.getParams() = serviceEndpoints?.firstNotNullOfOrNull { it?.getParams() } ?: onSubscribeEndpoints?.firstNotNullOfOrNull { it?.getParams() }

//////

public fun VideoItem.UpcomingEvent.getStartTimeMs() = startTime?.toLong()?.let { it * 1_000 } ?: -1

//////

public fun IconItem.getType() = iconType

//////

public fun MenuItem.getIconType() = menuServiceItemRenderer?.icon?.getType()

public fun MenuItem.getFeedbackToken() = menuServiceItemRenderer?.serviceEndpoint?.feedbackEndpoint?.feedbackToken
    ?: menuServiceItemRenderer?.command?.getFeedbackToken()

public fun MenuItem.getNotificationToken() = menuServiceItemRenderer?.serviceEndpoint?.recordNotificationInteractionsEndpoint?.serializedInteractionsRequest

public fun MenuItem.getBrowseId() = menuNavigationItemRenderer?.navigationEndpoint?.getBrowseId()

public fun MenuItem.getPlaylistId() = menuNavigationItemRenderer?.navigationEndpoint?.getPlaylistId()

public fun MenuItem.getVideoId() = menuNavigationItemRenderer?.navigationEndpoint?.getVideoId()

//////

public fun NotificationPreferenceButton.getItems() = subscriptionNotificationToggleButtonRenderer?.states?.filter { it?.getStateParams() != null }

public fun NotificationPreferenceButton.getCurrentStateId() = subscriptionNotificationToggleButtonRenderer?.currentStateId ?: -1

public fun NotificationStateItem.getTitle() = inlineMenuButton?.buttonRenderer?.text?.getText()

public fun NotificationStateItem.getStateId() = stateId

public fun NotificationStateItem.getStateParams() = inlineMenuButton?.buttonRenderer?.serviceEndpoint?.modifyChannelNotificationPreferenceEndpoint?.params

//////

private const val SERVICE_SUGGEST = "SUGGEST"
private const val SERVICE_GFEEDBACK = "GFEEDBACK"

private const val KEY_E = "e"
private const val KEY_LOGGED_IN = "logged_in"
private const val KEY_SUGGESTXP = "sugexp"
private const val KEY_SUGGEST_TOKEN = "suggest_token"

public fun ResponseContext.getSuggestToken(): String? = serviceTrackingParams?.firstNotNullOfOrNull {
    if (it?.service == SERVICE_SUGGEST) {
        it.params?.firstOrNull { it?.key == KEY_SUGGEST_TOKEN }?.value
    } else null
}
