package minefarts.smarttube.utils.channelgroups.models

import minefarts.smarttube.google.youtubedata3.impl.ItemMetadata
import minefarts.smarttube.utils.data.ItemGroup
import minefarts.smarttube.utils.data.ItemGroup.Item
import minefarts.smarttube.utils.data.MediaItem
import minefarts.smarttube.utils.helpers.Helpers
import minefarts.smarttube.utils.channelgroups.ChannelGroupServiceImpl
import minefarts.smarttube.google.common.helpers.ServiceHelper

private const val ITEM_DELIM = "&sgi;"
private const val LIST_DELIM = "&sga;"

internal data class ItemGroupImpl(
    //private val id: String = Helpers.getRandomNumber(ChannelGroupServiceImpl.SUBSCRIPTION_GROUP_ID + 100, Integer.MAX_VALUE).toString(),
    private val id: String = ServiceHelper.generateRandomId(12),
    private val title: String,
    private val iconUrl: String? = null,
    private val items: MutableList<Item>,
    private val badge: String? = null,
    var onChange: (ItemGroup) -> Unit = { ChannelGroupServiceImpl.persistState() }
): ItemGroup {
    override fun getId(): String {
        return id
    }

    override fun getTitle(): String {
        return title
    }

    override fun getIconUrl(): String? {
        return iconUrl
    }

    override fun getItems(): List<Item> {
        return items
    }

    override fun getBadge(): String? {
        return badge
    }

    override fun findItem(channelOrVideoId: String): Item? {
        return Helpers.findFirst(items) { mediaItem -> mediaItem.channelId == channelOrVideoId || mediaItem.videoId == channelOrVideoId }
    }

    override fun add(item: Item) {
        items.remove(item)
        items.add(0, item)
        onChange.invoke(this)
    }

    override fun addAll(newItems: List<Item>) {
        items.removeAll(newItems)
        items.addAll(newItems)
        onChange.invoke(this)
    }

    override fun remove(channelOrVideoId: String) {
        val removed = Helpers.removeIf(items) { mediaItem -> mediaItem.channelId == channelOrVideoId || mediaItem.videoId == channelOrVideoId }
        if (!removed.isNullOrEmpty()) {
            onChange.invoke(this)
        }
    }

    override fun contains(channelOrVideoId: String): Boolean {
        return Helpers.containsIf(items) { mediaItem -> mediaItem.channelId == channelOrVideoId || mediaItem.videoId == channelOrVideoId }
    }

    override fun isEmpty(): Boolean {
        return items.isEmpty()
    }

    override fun toString(): String {
        return Helpers.merge(ITEM_DELIM, id, title, iconUrl, Helpers.mergeList(LIST_DELIM, items), badge)
    }

    override fun equals(other: Any?): Boolean {
        if (other is ItemGroup) {
            return other.id == id
        }

        return super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    companion object {
        @JvmStatic
        fun fromString(spec: String?): ItemGroup? {
            if (spec == null)
                return null

            val split = Helpers.split(spec, ITEM_DELIM)

            val id = Helpers.parseStr(split, 0) ?: return null
            val title = Helpers.parseStr(split, 1) ?: return null
            val groupIconUrl = Helpers.parseStr(split, 2)
            val items: MutableList<Item> = Helpers.parseList(split, 3, LIST_DELIM, ItemImpl::fromString)
            val badge = Helpers.parseStr(split, 4)

            return ItemGroupImpl(id, title, groupIconUrl, items, badge)
        }

        @JvmStatic
        fun fromMetadata(metadata: ItemMetadata): ItemGroup? {
            val playlistId = metadata.playlistId
            val title = metadata.title
            if (playlistId == null || title == null)
                return null

            return ItemGroupImpl(
                playlistId, title, metadata.cardImageUrl, mutableListOf(), metadata.itemCount?.let { "$it videos" }
            )
        }

        @JvmStatic
        fun fromMediaItem(mediaItem: MediaItem): ItemGroup {
            return ItemGroupImpl(mediaItem.playlistId, mediaItem.title, mediaItem.cardImageUrl, mutableListOf(), mediaItem.badgeText)
        }
    }
}
