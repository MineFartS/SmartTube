package minefarts.smarttube.utils.browse.gen

import minefarts.smarttube.utils.helpers.Helpers
import minefarts.smarttube.google.common.helpers.YouTubeHelper
import com.liskovsoft.youtubeapi.common.models.gen.ItemWrapper
import com.liskovsoft.youtubeapi.common.models.gen.ThumbnailItem
import com.liskovsoft.youtubeapi.common.models.gen.getBrowseId
import com.liskovsoft.youtubeapi.common.models.gen.getContinuationToken
import com.liskovsoft.youtubeapi.common.models.gen.getParams
import com.liskovsoft.youtubeapi.common.models.gen.getFeedbackTokens
import com.liskovsoft.youtubeapi.common.models.gen.getSubtitle
import com.liskovsoft.youtubeapi.common.models.gen.getSuggestToken
import com.liskovsoft.youtubeapi.common.models.gen.getText
import com.liskovsoft.youtubeapi.common.models.gen.getTitle
import com.liskovsoft.youtubeapi.common.models.gen.isLive
import com.liskovsoft.youtubeapi.common.models.gen.isUpcoming
import minefarts.smarttube.utils.next.v2.gen.EngagementPanel
import minefarts.smarttube.utils.next.v2.gen.getChannelName
import minefarts.smarttube.utils.next.v2.gen.getContinuationToken
import minefarts.smarttube.utils.next.v2.gen.getItemWrappers
import minefarts.smarttube.utils.next.v2.gen.getPublishDate
import minefarts.smarttube.utils.next.v2.gen.getTitle
import minefarts.smarttube.utils.next.v2.gen.getViews
import minefarts.smarttube.utils.next.v2.gen.containsShorts

/**
 *  Always renders first tab
 */
public fun BrowseResult.getItems(): List<ItemWrapper?>? = getRootTab()?.getItems()
public fun BrowseResult.getLiveItems(): List<ItemWrapper?>? =
    getItems()?.filter { it?.isLive() == true || it?.isUpcoming() == true }?.sortedByDescending { it?.isLive() }
public fun BrowseResult.getPastLiveItems(maxItems: Int = -1): List<ItemWrapper?>? =
    getItems()?.filter { it != null && !it.isLive() && !it.isUpcoming() }?.let { if (maxItems > 0) it.take(maxItems) else it }
public fun BrowseResult.getShortItems(): List<ItemWrapper?>? = getRootTab()?.getShortItems()
public fun BrowseResult.getNestedShelves(): List<ShelfListWrapper?>? = getRootTab()?.getNestedShelves()
public fun BrowseResult.getContinuationToken(): String? = getRootTab()?.getContinuationToken()
public fun BrowseResult.getTabs(): List<TabRenderer?>? = (contents?.twoColumnBrowseResultsRenderer ?: contents?.singleColumnBrowseResultsRenderer)
    ?.tabs?.mapNotNull { it?.tabRenderer ?: it?.expandableTabRenderer }
public fun BrowseResult.getSections(): List<RichSectionRenderer?>? = getRootTab()?.getSections()
public fun BrowseResult.getChips(): List<ChipCloudChipRenderer?>? = getRootTab()?.getChips()
/**
 *  Always renders first tab
 *  
 *  First tab on HOME page has no title. Use first chip instead.
 */
public fun BrowseResult.getTitle(): String? =
    getRootTab()?.title ?: (header?.playlistHeaderRenderer ?: header?.musicHeaderRenderer)?.getTitle() ?: getChips()?.getOrNull(0)?.getTitle()
public fun BrowseResult.isPlaylist(): Boolean = header?.playlistHeaderRenderer != null
public fun BrowseResult.isHome(): Boolean = getTabs()?.getOrNull(0)?.getItems() != null
public fun BrowseResult.getRedirectBrowseId(): String? = onResponseReceivedActions?.firstNotNullOfOrNull { it?.navigateAction?.endpoint?.getBrowseId() }
private fun BrowseResult.getRootTab() = getTabs()?.firstNotNullOfOrNull { if (it?.content != null) it else null }

/////

private const val TAB_STYLE_NEW_CONTENT = "NEW_CONTENT"

public fun TabRenderer.getItems(): List<ItemWrapper?>? = getListRenderer()?.getItems()
    ?: getGridRenderer()?.getItems() ?: getTVGridRenderer()?.getItems() ?: getTVListRenderer()?.getItems()
public fun TabRenderer.getShortItems(): List<ItemWrapper?>? = getGridRenderer()?.getShortItems() ?: getTVListRenderer()?.getShortItems()
public fun TabRenderer.getContinuationToken(): String? = getListRenderer()?.getContinuationToken()
    ?: getGridRenderer()?.getContinuationToken()
    ?: getTVGridRenderer()?.getContinuationToken()
    ?: getTVListRenderer()?.getContinuationToken()
public fun TabRenderer.getTitle(): String? = title
public fun TabRenderer.getBrowseId(): String? = endpoint?.getBrowseId()
public fun TabRenderer.getReloadToken(): String? = content?.tvSurfaceContentRenderer?.continuation?.getContinuationToken()
public fun TabRenderer.getParams(): String? = endpoint?.getParams()
public fun TabRenderer.getThumbnails(): ThumbnailItem? = thumbnail
public fun TabRenderer.hasNewContent(): Boolean = presentationStyle?.style == TAB_STYLE_NEW_CONTENT
public fun TabRenderer.getNestedShelves(): List<ShelfListWrapper?>? = getListRenderer()?.getNestedShelves()
public fun TabRenderer.getSections(): List<RichSectionRenderer?>? = getGridRenderer()?.getSections()
public fun TabRenderer.getChips(): List<ChipCloudChipRenderer?>? = getChipRenderer()?.getChips()
private fun TabRenderer.getListRenderer() = content?.sectionListRenderer
private fun TabRenderer.getGridRenderer() = content?.richGridRenderer
private fun TabRenderer.getChipRenderer() = content?.richGridRenderer?.header?.feedFilterChipBarRenderer
private fun TabRenderer.getTVGridRenderer() = content?.tvSurfaceContentRenderer?.content?.gridRenderer
public fun TabRenderer.getTVListRenderer() = content?.tvSurfaceContentRenderer?.content?.sectionListRenderer

/////

private const val CONTINUATION_HEADER = "RELOAD_CONTINUATION_SLOT_HEADER" // channel sorting continuation header

public fun ContinuationResult.getItems(): List<ItemWrapper?>? = getContinuations()?.flatMap { it?.getItems() ?: listOfNotNull(it?.getItem()) }
public fun ContinuationResult.getContinuationToken(): String? =
    getContinuations()?.firstNotNullOfOrNull { it?.getContinuationToken() }
    ?: getContinuations()?.lastOrNull()?.getContinuationToken()
public fun ContinuationResult.getSections(): List<RichSectionRenderer?>? = getContinuations()?.mapNotNull { it?.richSectionRenderer }
private fun ContinuationResult.getContinuations() = onResponseReceivedActions?.firstNotNullOfOrNull {
        it?.appendContinuationItemsAction?.continuationItems
            ?: it?.reloadContinuationItemsCommand?.let { if (it.slot != CONTINUATION_HEADER) it.continuationItems else null }
    }

/////

public fun RichSectionRenderer.getTitle(): String? = content?.richShelfRenderer?.title?.getText()
public fun RichSectionRenderer.getItems(): List<ItemWrapper?>? = getContents()?.mapNotNull { it?.richItemRenderer?.content }
public fun RichSectionRenderer.getContinuationToken(): String? = getContents()?.lastOrNull()?.continuationItemRenderer?.getContinuationToken()
private fun RichSectionRenderer.getContents() = content?.richShelfRenderer?.contents

/////

private const val SHELVE_ROW_SIZE = 3 // the modern grid layout is actually rows with the same size

public fun ShelfListWrapper.getTitle(): String? = getFirstShelfRenderer()?.title?.getText()
public fun ShelfListWrapper.getItems(): List<ItemWrapper?>? =
    // Remain only untitled rows. Helps to filter Subscriptions from "Most relevant" and "Shorts".
    getContents()?.flatMap { it?.takeIf { it.getTitle() == null }?.getItems() ?: emptyList() }
    // The new approach: filter Subscriptions from 'Most relevant' by keeping the same size rows
    //getContents()?.flatMap { it?.getItems()?.takeIf { it.size == SHELVE_ROW_SIZE } ?: emptyList() }
public fun ShelfListWrapper.getShortItems(): List<ItemWrapper?>? =
    getContents()?.firstNotNullOfOrNull { if (it?.containsShorts() == true) it.getItems() else null }
public fun ShelfListWrapper.getContinuationToken() = getContents()?.lastOrNull()?.getContinuationToken() ?: continuations?.getContinuationToken()
public fun ShelfListWrapper.getBrowseId() = getFirstShelfRenderer()?.endpoint?.getBrowseId()
public fun ShelfListWrapper.getParams() = getFirstShelfRenderer()?.endpoint?.getParams()
private fun ShelfListWrapper.getContents() = contents // Contains shelves with items (usually 3 in a row) and single row for shorts
private fun ShelfListWrapper.getFirstShelfRenderer() = contents?.firstNotNullOfOrNull { it?.shelfRenderer }
private fun ShelfListWrapper.getFirstGridRenderer() = contents?.firstNotNullOfOrNull { it?.gridRenderer }

/////

public fun SectionListRenderer.getItems(): List<ItemWrapper?>? = getContents()?.flatMap { it?.getItems() ?: emptyList() }
public fun SectionListRenderer.getNestedShelves(): List<ShelfListWrapper?>? = getContents()?.mapNotNull { it?.itemSectionRenderer }
public fun SectionListRenderer.getContinuationToken(): String? = getContents()?.firstNotNullOfOrNull { it?.getContinuationToken() }
private fun SectionListRenderer.getContents() = contents // Contains shelves with items (3 in a row) and single row for shorts

///////

public fun GridRenderer.getItems(): List<ItemWrapper?>? = items
public fun GridRenderer.getContinuationToken() = continuations?.getContinuationToken() ?: items?.lastOrNull()?.getContinuationToken()

///////

public fun RichGridRenderer.getItems(): List<ItemWrapper?>? = getContents()?.mapNotNull { it?.getItem() }
public fun RichGridRenderer.getContinuationToken(): String? = getContents()?.lastOrNull()?.getContinuationToken()
public fun RichGridRenderer.getShortItems(): List<ItemWrapper?>? = getContents()?.flatMap { it?.getItems() ?: emptyList() }
public fun RichGridRenderer.getSections(): List<RichSectionRenderer?>? = getContents()?.mapNotNull { it?.richSectionRenderer }
private fun RichGridRenderer.getContents() = contents

///////

public fun FeedFilterChipBarRenderer.getChips(): List<ChipCloudChipRenderer?>? = getContents()?.mapNotNull { it?.chipCloudChipRenderer }
private fun FeedFilterChipBarRenderer.getContents() = contents

/////

public fun ChipCloudChipRenderer.getTitle(): String? = text?.getText()

/////

public fun SectionWrapper.getItem() = richItemRenderer?.content ?: playlistVideoRenderer?.let { ItemWrapper(playlistVideoRenderer = it) }
    ?: gridPlaylistRenderer?.let { ItemWrapper(gridPlaylistRenderer = it) } ?: gridVideoRenderer?.let { ItemWrapper(gridVideoRenderer = it) }

public fun SectionWrapper.getItems() = itemSectionRenderer?.getItems() ?: richSectionRenderer?.getItems() ?: gridRenderer?.items
public fun SectionWrapper.getContinuationToken() = continuationItemRenderer?.getContinuationToken() ?: itemSectionRenderer?.getContinuationToken()

/////

public fun ContinuationItemRenderer.getContinuationToken() = continuationEndpoint?.continuationCommand?.token

/////

public fun ChipCloudChipRenderer.getContinuationToken() = navigationEndpoint?.continuationCommand?.token


/////

private const val GUIDE_STYLE_NEW_CONTENT = "GUIDE_ENTRY_PRESENTATION_STYLE_NEW_CONTENT"
private const val GUIDE_STYLE_NONE = "GUIDE_ENTRY_PRESENTATION_STYLE_NONE"

public fun GuideResult.getFirstSubs(): List<GuideItem?>? = getSubsRoot()?.items?.mapNotNull { it?.guideEntryRenderer }
public fun GuideResult.getCollapsibleSubs(): List<GuideItem?>? =
    getSubsRoot()?.items?.firstNotNullOfOrNull { it?.guideCollapsibleEntryRenderer }?.expandableItems?.mapNotNull { it?.guideEntryRenderer }
public fun GuideResult.getRecommended(): List<GuideItem?>? = items?.mapNotNull { it?.guideSectionRenderer }?.getOrNull(1)?.items?.mapNotNull { it?.guideEntryRenderer }
public fun GuideResult.getSuggestToken(): String? = responseContext?.getSuggestToken()
private fun GuideResult.getSubsRoot() = items?.firstNotNullOfOrNull { it?.guideSubscriptionsSectionRenderer }

public fun GuideItem.getBrowseId() = navigationEndpoint?.getBrowseId()
public fun GuideItem.getParams() = navigationEndpoint?.getParams()
public fun GuideItem.getThumbnails() = thumbnail
public fun GuideItem.getTitle() = formattedTitle?.getText()
public fun GuideItem.hasNewContent() = presentationStyle == GUIDE_STYLE_NEW_CONTENT
public fun GuideItem.isLive() = badges?.liveBroadcasting

///////

public fun BrowseResultKids.getSections(): List<AnchoredSectionRenderer?>? = contents?.kidsHomeScreenRenderer?.anchors?.mapNotNull { it?.anchoredSectionRenderer }
public fun BrowseResultKids.getRootSection(): AnchoredSectionRenderer? = getSections()?.getOrNull(0)
public fun AnchoredSectionRenderer.getItems(): List<ItemWrapper?>? = content?.sectionListRenderer?.contents?.getOrNull(0)?.itemSectionRenderer?.contents
public fun AnchoredSectionRenderer.getTitle(): String? = title
public fun AnchoredSectionRenderer.getBrowseId(): String? = navigationEndpoint?.getBrowseId()
public fun AnchoredSectionRenderer.getParams(): String? = navigationEndpoint?.getParams()

//////

private fun ReelResult.getWatchEndpoint(): ReelWatchEndpoint? = replacementEndpoint?.reelWatchEndpoint
private fun ReelResult.getPlayerHeader(): ReelPlayerHeaderRenderer? = overlay?.reelPlayerOverlayRenderer?.reelPlayerHeaderSupportedRenderers?.reelPlayerHeaderRenderer
public fun ReelResult.getVideoId(): String? = getWatchEndpoint()?.videoId
public fun ReelResult.getTitle(): String? = getPlayerHeader()?.reelTitleOnClickCommand?.getTitle() ?: getVideoInfo()?.getTitle()
public fun ReelResult.getSubtitle(): CharSequence? = getPlayerHeader()?.reelTitleOnClickCommand?.getSubtitle() ?:
    YouTubeHelper.createInfo(getVideoInfo()?.getChannelName(), getVideoInfo()?.getViews(), getVideoInfo()?.getPublishDate())
private fun ReelResult.getVideoInfo(): EngagementPanel? = engagementPanels?.firstNotNullOfOrNull { if (it?.getTitle() != null) it else null }
private fun ReelResult.getChannelName(): String? = getPlayerHeader()?.channelTitleText?.getText()
private fun ReelResult.getUploadDate(): String? = getPlayerHeader()?.timestampText?.getText()
public fun ReelResult.getThumbnails(): ThumbnailItem? = getWatchEndpoint()?.thumbnail
public fun ReelResult.getBrowseId(): String? = getPlayerHeader()?.channelNavigationEndpoint?.getBrowseId()
public fun ReelResult.getFeedbackTokens(): List<String?>? = overlay?.reelPlayerOverlayRenderer?.menu?.getFeedbackTokens()
public fun ReelResult.getContinuationToken(): String? = sequenceContinuation ?: continuationEndpoint?.continuationCommand?.token

public fun ReelContinuationResult.getItems(): List<ReelWatchEndpoint?>? = entries?.mapNotNull { it?.command?.reelWatchEndpoint }
public fun ReelContinuationResult.getContinuationToken(): String? = continuation ?: continuationEndpoint?.continuationCommand?.token

public fun ReelWatchEndpoint.getVideoId(): String? = videoId
public fun ReelWatchEndpoint.getThumbnails(): ThumbnailItem? = thumbnail

///////

private const val SUBSCRIPTIONS_BROWSE_ID = "FEsubscriptions"

public fun BrowseResultTV.getShelves(): List<Shelf?>? = getContent()?.sectionListRenderer?.contents
    ?.filter { it?.shelfRenderer != null } // skip promoShelfRenderer
    ?.sortedByDescending { it?.shelfRenderer?.endpoint?.getParams()?.let {
        // Move Live, Past Streams and Videos to the top
        Helpers.startsWithAny(it,"EgZ2aWRlb3MYAyACOAJwA", "EgZ2aWRlb3MYAyAAcA", "EgZ2aWRlb3MYAyACOARwA")
    } ?: false }
public fun BrowseResultTV.getItems(): List<ItemWrapper?>? = getContent()?.gridRenderer?.items
    ?: getContent()?.twoColumnRenderer?.rightColumn?.playlistVideoListRenderer?.contents
    ?: getSubscriptionsTab()?.getItems() // see: getTVListRenderer()?.getItems() for how the actual filter from 'Most relevant' is happening
    ?: getShelves()?.getOrNull(0)?.getItems()
public fun BrowseResultTV.getShortItems(): List<ItemWrapper?>? = getSubscriptionsTab()?.getShortItems()
public fun BrowseResultTV.getContinuationToken(): String? = getSubscriptionsTab()?.getContinuationToken()
    ?: getContent()?.twoColumnRenderer?.rightColumn?.playlistVideoListRenderer?.continuations?.getContinuationToken()
    ?: getContent()?.sectionListRenderer?.continuations?.getContinuationToken()
    ?: getShelves()?.getOrNull(0)?.getContinuationToken()
    ?: getContent()?.gridRenderer?.getContinuationToken()
// Get tabs, e.g. All (Subscriptions), channel1, channel2 etc
public fun BrowseResultTV.getTabs() = getSections()?.getOrNull(0)?.tvSecondaryNavSectionRenderer?.tabs?.mapNotNull { it.tabRenderer ?: it.expandableTabRenderer }
private fun BrowseResultTV.getContent() = contents?.tvBrowseRenderer?.content?.tvSurfaceContentRenderer?.content
private fun BrowseResultTV.getSections() = contents?.tvBrowseRenderer?.content?.tvSecondaryNavRenderer?.sections
private fun BrowseResultTV.getSubscriptionsTab() = getTabs()?.firstOrNull { it.getBrowseId() == SUBSCRIPTIONS_BROWSE_ID } ?: getTabs()?.getOrNull(0)

///////////

public fun Shelf.getTitle(): String? = shelfRenderer?.getTitle()
public fun Shelf.getItems(): List<ItemWrapper?>? = shelfRenderer?.getItemWrappers()
    ?: gridRenderer?.items
    ?: playlistVideoListRenderer?.contents
    ?: videoRenderer?.let { listOf(ItemWrapper(videoRenderer = it)) }
public fun Shelf.getContinuationToken(): String? = shelfRenderer?.getContinuationToken()
    ?: (gridRenderer ?: shelfRenderer?.content?.gridRenderer)?.getContinuationToken()
    ?: playlistVideoListRenderer?.getContinuationToken()
public fun Shelf.containsShorts(): Boolean = shelfRenderer?.containsShorts() == true

///////////

