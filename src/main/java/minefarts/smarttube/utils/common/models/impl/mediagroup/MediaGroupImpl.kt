package minefarts.smarttube.utils.common.models.impl.mediagroup

import minefarts.smarttube.utils.service.data.MediaGroup
import minefarts.smarttube.utils.data.MediaItem
import minefarts.smarttube.utils.browse.gen.*
import minefarts.smarttube.utils.common.models.gen.ItemWrapper
import minefarts.smarttube.utils.common.models.gen.getBrowseId
import minefarts.smarttube.utils.common.models.gen.getParams
import minefarts.smarttube.utils.common.models.gen.isLive
import minefarts.smarttube.utils.common.models.impl.mediaitem.GuideMediaItem
import minefarts.smarttube.utils.common.models.impl.mediaitem.NotificationMediaItem
import minefarts.smarttube.utils.common.models.impl.mediaitem.TabMediaItem
import minefarts.smarttube.utils.next.v2.gen.WatchNextResultContinuation
import minefarts.smarttube.utils.next.v2.gen.getItems
import minefarts.smarttube.utils.next.v2.gen.getContinuationToken
import minefarts.smarttube.utils.next.v2.gen.getShelves
import minefarts.smarttube.utils.notifications.gen.NotificationsResult
import minefarts.smarttube.utils.notifications.gen.getItems

/**
 *  Always renders first tab
 */
public data class BrowseMediaGroup(
    private val browseResult: BrowseResult,
    private val options: MediaGroupOptions,
    private val liveResult: BrowseResult? = null
): BaseMediaGroup(options) {
    override fun getItemWrappersInt(): List<ItemWrapper?> =
        listOfNotNull(liveResult?.getLiveItems(), browseResult.getItems()).flatten()
    override fun getNextPageKeyInt(): String? = browseResult.getContinuationToken()
    override fun getTitleInt(): String? = browseResult.getTitle()
}

public data class BrowseMediaGroupTV(
    private val browseResult: BrowseResultTV,
    private val options: MediaGroupOptions,
    private val overrideItems: List<ItemWrapper?>? = null,
    private val overrideKey: String? = null
): BaseMediaGroup(options) {
    override fun getItemWrappersInt(): List<ItemWrapper?>? =
        overrideItems?.sortedByDescending { it?.isLive() ?: false } ?: browseResult.getItems()
    override fun getNextPageKeyInt(): String? = if (overrideItems != null) overrideKey else browseResult.getContinuationToken()
    override fun getTitleInt(): String? = null
}

public data class LiveMediaGroup(
    private val liveResult: BrowseResult,
    private val options: MediaGroupOptions
): BaseMediaGroup(options) {
    override fun getItemWrappersInt(): List<ItemWrapper?> = listOfNotNull(liveResult.getLiveItems(), liveResult.getPastLiveItems()).flatten()
    override fun getNextPageKeyInt(): String? = null
    override fun getTitleInt(): String? = liveResult.getTitle()
}

public data class ContinuationMediaGroup(
    private val continuationResult: ContinuationResult,
    private val options: MediaGroupOptions
): BaseMediaGroup(options) {
    override fun getItemWrappersInt(): List<ItemWrapper?>? = continuationResult.getItems()
    override fun getNextPageKeyInt(): String? = continuationResult.getContinuationToken()
    override fun getTitleInt(): String? = null
}

public data class WatchNexContinuationMediaGroup(
    private val continuation: WatchNextResultContinuation,
    private val options: MediaGroupOptions,
    private val overrideItems: List<ItemWrapper?>? = null,
    private val overrideKey: String? = null
): BaseMediaGroup(options) {
    override fun getItemWrappersInt(): List<ItemWrapper?>? = overrideItems?.sortedByDescending { it?.isLive() ?: false } ?: continuation.getItems() ?: getLastShelf()?.getItems()
    override fun getNextPageKeyInt(): String? = if (overrideItems != null) overrideKey else continuation.getContinuationToken() ?: getLastShelf()?.getContinuationToken()
    override fun getTitleInt(): String? = null
    private fun getLastShelf() = continuation.getShelves()?.lastOrNull() // Get main content of Channels section and skip SHORTS
}

public data class RichSectionMediaGroup(
    private val richSectionRenderer: RichSectionRenderer,
    private val options: MediaGroupOptions
): BaseMediaGroup(options) {
    override fun getItemWrappersInt(): List<ItemWrapper?>? = richSectionRenderer.getItems()
    override fun getNextPageKeyInt(): String? = richSectionRenderer.getContinuationToken()
    override fun getTitleInt(): String? = richSectionRenderer.getTitle()
}

public data class ShelfSectionMediaGroup(
    private val shelf: Shelf,
    private val options: MediaGroupOptions
): BaseMediaGroup(options) {
    override fun getItemWrappersInt(): List<ItemWrapper?>? = shelf.getItems()
    override fun getNextPageKeyInt(): String? = shelf.getContinuationToken()
    override fun getTitleInt(): String? = shelf.getTitle()
}

public data class ItemSectionMediaGroup(
    private val itemSectionRenderer: ShelfListWrapper,
    private val options: MediaGroupOptions
): BaseMediaGroup(options) {
    // Fix row continuation (no next key but has channel) by reporting empty content (will be continued as a chip). Example https://www.youtube.com/@hdtvtest
    private val fixContinuation = nextPageKey == null && channelId != null
    override fun getItemWrappersInt(): List<ItemWrapper?>? = if (fixContinuation) null else itemSectionRenderer.getItems()
    override fun getNextPageKeyInt(): String? = if (fixContinuation) null else itemSectionRenderer.getContinuationToken()
    override fun getTitleInt(): String? = itemSectionRenderer.getTitle()
    override fun getChannelIdInt(): String? = itemSectionRenderer.getBrowseId()
    override fun getParamsInt(): String? = itemSectionRenderer.getParams()
}

public data class TabMediaGroup(
    private val tabRenderer: TabRenderer,
    private val options: MediaGroupOptions
): BaseMediaGroup(options) {
    override fun getItemWrappersInt(): List<ItemWrapper?>? = tabRenderer.getItems()
    override fun getNextPageKeyInt(): String? = tabRenderer.getContinuationToken()
    override fun getTitleInt(): String? = tabRenderer.getTitle()
    override fun getChannelIdInt(): String? = tabRenderer.endpoint?.getBrowseId()
    override fun getParamsInt(): String? = tabRenderer.endpoint?.getParams()
}

public data class KidsSectionMediaGroup(
    private val anchoredSectionRenderer: AnchoredSectionRenderer,
    private val options: MediaGroupOptions
): BaseMediaGroup(options) {
    override fun getItemWrappersInt(): List<ItemWrapper?>? = anchoredSectionRenderer.getItems()
    override fun getNextPageKeyInt(): String? = null
    override fun getTitleInt(): String? = anchoredSectionRenderer.getTitle()
}

public data class ChipMediaGroup(
    private val chipCloudChipRenderer: ChipCloudChipRenderer,
    private val options: MediaGroupOptions
): BaseMediaGroup(options) {
    override fun getItemWrappersInt(): List<ItemWrapper?>? = null
    override fun getNextPageKeyInt(): String? = chipCloudChipRenderer.getContinuationToken()
    override fun getTitleInt(): String? = chipCloudChipRenderer.getTitle()
}

public const val SORT_DEFAULT: Int = 0
public const val SORT_BY_NAME: Int = 1
public const val SORT_BY_NEW_CONTENT: Int = 2

public data class GuideMediaGroup(
    private val guideResult: GuideResult,
    private val options: MediaGroupOptions,
    private val sort: Int = SORT_DEFAULT
): BaseMediaGroup(options) {
    override fun getItemWrappersInt(): List<ItemWrapper?>? = null
    override fun getNextPageKeyInt(): String? = null
    override fun getTitleInt(): String? = null
    override val mediaItemList by lazy {
        val result = mutableListOf<MediaItem>()

        guideResult.getFirstSubs()?.forEach {
            it?.let { if (it.thumbnail != null) result.add(GuideMediaItem(it)) } // exclude 'special' items
        }

        guideResult.getCollapsibleSubs()?.forEach {
            it?.let { if (it.thumbnail != null) result.add(GuideMediaItem(it)) } // exclude 'special' items
        }

        if (sort == SORT_BY_NAME) result.sortBy { it.title?.lowercase() }

        result
    }
}

public data class ChannelListMediaGroup(
    private val tabs: List<TabRenderer>,
    private val options: MediaGroupOptions,
    private val sortBy: Int = SORT_DEFAULT
): BaseMediaGroup(options) {
    override fun getItemWrappersInt(): List<ItemWrapper?>? = null
    override fun getNextPageKeyInt(): String? = null
    override fun getTitleInt(): String? = null
    override val mediaItemList by lazy {
        val result = mutableListOf<MediaItem>()

        tabs.forEachIndexed { idx, it ->
            // Skip All subscriptions tab
            if (idx == 0 && it.getThumbnails() == null) {
                return@forEachIndexed
            }

            result.add(TabMediaItem(it, options.groupType))
        }

        if (sortBy == SORT_BY_NAME) result.sortBy { it.title?.lowercase() }

        if (sortBy == SORT_BY_NEW_CONTENT) {
            result.sortBy { it.title?.lowercase() }
            result.sortByDescending { it.hasNewContent() }
        }

        result
    }
}

public data class RecommendedMediaGroup(
    private val guideItem: GuideItem,
    private val options: MediaGroupOptions
): BaseMediaGroup(options) {
    override fun getItemWrappersInt(): List<ItemWrapper?>? = null
    override fun getNextPageKeyInt(): String? = null
    override fun getTitleInt(): String? = guideItem.getTitle()
    override fun getChannelIdInt(): String? = guideItem.getBrowseId()
    override fun getParamsInt(): String? = guideItem.getParams()
}

public data class ShortsMediaGroup(
    private val items: List<MediaItem?>,
    private val continuation: String? = null,
    private val options: MediaGroupOptions
): BaseMediaGroup(options) {
    override fun getItemWrappersInt(): List<ItemWrapper?>? = null
    override fun getNextPageKeyInt(): String? = continuation
    override fun getTitleInt(): String? = null
    override val mediaItemList = items
}

public data class NotificationsMediaGroup(
    private val result: NotificationsResult
): BaseMediaGroup(MediaGroupOptions(MediaGroup.TYPE_NOTIFICATIONS)) {
    override fun getItemWrappersInt(): List<ItemWrapper?>? = null
    override fun getNextPageKeyInt(): String? = null
    override fun getTitleInt(): String? = null
    override val mediaItemList by lazy { result.getItems()?.mapNotNull { it?.let { NotificationMediaItem(it) } } }
}

public data class SubscribedShortsMediaGroup(
    private val items: List<ItemWrapper?>
): BaseMediaGroup(MediaGroupOptions(MediaGroup.TYPE_SHORTS)) {
    override fun getItemWrappersInt(): List<ItemWrapper?> = items
    override fun getNextPageKeyInt(): String? = null
    override fun getTitleInt(): String? = null
}

public data class EmptyMediaGroup(
    private val reloadPageKey: String,
    private val type: Int,
    private val title: String? = null
): BaseMediaGroup(MediaGroupOptions(type)) {
    override fun getItemWrappersInt(): List<ItemWrapper?>? = null
    override fun getNextPageKeyInt(): String = reloadPageKey
    override fun getTitleInt(): String? = title
}
