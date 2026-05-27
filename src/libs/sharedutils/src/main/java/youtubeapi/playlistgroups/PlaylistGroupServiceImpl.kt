package minefarts.sharedutils.playlistgroups

import minefarts.sharedutils.data.ItemGroup
import minefarts.sharedutils.data.ItemGroup.Item
import minefarts.sharedutils.data.MediaItem
import minefarts.sharedutils.helpers.Helpers
import minefarts.sharedutils.rx.RxHelper
import minefarts.sharedutils.channelgroups.models.ItemGroupImpl
import minefarts.sharedutils.service.internal.MediaServicePrefs
import io.reactivex.disposables.Disposable

internal object PlaylistGroupServiceImpl : MediaServicePrefs.ProfileChangeListener {
    private const val PLAYLIST_GROUP_DATA = "playlist_group_data"
    private const val PERSIST_DELAY_MS: Long = 5_000
    private lateinit var mPlaylists: MutableList<ItemGroup>
    private var mPersistAction: Disposable? = null
    @JvmField
    var cachedItem: MediaItem? = null

    init {
        MediaServicePrefs.addListener(this)
        restoreState()
    }

    override fun onProfileChanged() {
        restoreState()
    }

    @JvmStatic
    fun addPlaylistGroup(group: ItemGroup?) {
        if (group == null)
            return

        // Move to the top
        (group as ItemGroupImpl).onChange = { persistState() }
        mPlaylists.remove(group)
        mPlaylists.add(0, group)
        persistState()
    }

    @JvmStatic
    fun removePlaylistGroup(group: ItemGroup) {
        mPlaylists.remove(group)
        persistState()
    }

    @JvmStatic
    fun removePlaylistGroup(id: String?) {
        if (id == null)
            return

        val result = Helpers.removeIf(mPlaylists) { it.id == id }
        if (!result.isNullOrEmpty()) {
            persistState()
        }
    }

    @JvmStatic
    fun renamePlaylistGroup(itemGroup: ItemGroup, title: String) {
        addPlaylistGroup(ItemGroupImpl(itemGroup.id, title, itemGroup.iconUrl, itemGroup.items))
    }

    @JvmStatic
    fun createPlaylistGroup(title: String, iconUrl: String?, items: List<Item>): ItemGroup {
        return ItemGroupImpl(title = title, iconUrl = iconUrl, items = items.toMutableList())
    }

    @JvmStatic
    fun createPlaylistGroup(id: String, title: String, iconUrl: String?): ItemGroup {
        return ItemGroupImpl(id = id, title = title, iconUrl = iconUrl, items = mutableListOf())
    }

    @JvmStatic
    fun getPlaylistGroups(): List<ItemGroup> {
        return mPlaylists
    }

    @JvmStatic
    fun findPlaylistGroup(id: String?): ItemGroup? {
        if (id == null)
            return null

        return mPlaylists.firstOrNull { it.id == id }
    }

    private fun restoreState() {
        val data = MediaServicePrefs.getData(PLAYLIST_GROUP_DATA)
        restoreState(data)
    }

    private fun restoreState(data: String?) {
        val split = Helpers.splitData(data)

        mPlaylists = Helpers.parseList(split, 0, ItemGroupImpl::fromString)
        mPlaylists.forEach {
            it as ItemGroupImpl
            it.onChange = { persistState() }
        }
    }

    private fun persistState() {
        RxHelper.disposeActions(mPersistAction)
        mPersistAction = RxHelper.runAsync(::persistStateReal, PERSIST_DELAY_MS)
    }

    private fun persistStateReal() {
        MediaServicePrefs.setData(PLAYLIST_GROUP_DATA, Helpers.mergeData(mPlaylists))
    }
}