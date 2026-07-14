package minefarts.smarttube.utils.channelgroups

import android.net.Uri
import minefarts.smarttube.google.youtubedata3.YouTubeDataServiceInt
import minefarts.smarttube.utils.ChannelGroupService
import minefarts.smarttube.utils.data.ItemGroup
import minefarts.smarttube.utils.data.ItemGroup.Item
import minefarts.smarttube.utils.helpers.Helpers
import com.liskovsoft.sharedutils.rx.RxHelper
import minefarts.smarttube.utils.channelgroups.importing.grayjay.GrayJayService
import minefarts.smarttube.utils.channelgroups.importing.newpipe.NewPipeService
import minefarts.smarttube.utils.channelgroups.importing.pockettube.PocketTubeService
import minefarts.smarttube.utils.channelgroups.models.ItemGroupImpl
import minefarts.smarttube.utils.channelgroups.models.ItemImpl
import minefarts.smarttube.utils.service.internal.MediaServicePrefs
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.io.File

object ChannelGroupServiceImpl: MediaServicePrefs.ProfileChangeListener,
    ChannelGroupService {
    private const val SUBSCRIPTIONS_GROUP_ID: String = "1000"
    private const val SUBSCRIPTIONS_GROUP_NAME: String = "Subscriptions"
    private const val NOTIFICATIONS_GROUP_ID: String = "1001"
    private const val NOTIFICATIONS_GROUP_NAME: String = "Notifications"
    private const val CHANNEL_GROUP_DATA = "channel_group_data"
    private val mImportServices = listOf(PocketTubeService, GrayJayService, NewPipeService)
    private lateinit var mChannelGroups: MutableList<ItemGroup>
    private var mPersistAction: Disposable? = null
    var cachedChannel: Item? = null

    init {
        MediaServicePrefs.addListener(this)
        restoreState()
    }

    override fun onProfileChanged() {
        restoreState()
    }

    override fun getChannelGroups(): List<ItemGroup> {
        return mChannelGroups.filter { it.id != SUBSCRIPTIONS_GROUP_ID && it.id != NOTIFICATIONS_GROUP_ID }
    }

    override fun addChannelGroup(group: ItemGroup) {
        // Move to the top
        mChannelGroups.remove(group)
        mChannelGroups.add(0, group)
        persistState()
    }

    override fun removeChannelGroup(group: ItemGroup?) {
        if (mChannelGroups.contains(group)) {
            mChannelGroups.remove(group)
            persistState()
        }
    }

    override fun findChannelGroupById(channelGroupId: String?): ItemGroup? {
        if (channelGroupId == null) {
            return null
        }

        for (group in mChannelGroups) {
            if (group.id == channelGroupId) {
                return group
            }
        }

        return null
    }

    override fun findChannelGroupByTitle(title: String): ItemGroup? {
        for (group in mChannelGroups) {
            if (group.title == title) {
                return group
            }
        }

        return null
    }

    fun getSubscribedChannelGroup(): ItemGroup {
        return findOrInitGroup(SUBSCRIPTIONS_GROUP_ID, SUBSCRIPTIONS_GROUP_NAME)
    }

    fun getSubscribedChannelIds(): Array<String>? {
        return findChannelIdsForGroup(SUBSCRIPTIONS_GROUP_ID)
    }

    fun getNotificationChannelGroup(): ItemGroup {
        return findOrInitGroup(NOTIFICATIONS_GROUP_ID, NOTIFICATIONS_GROUP_NAME)
    }

    fun getNotificationChannelIds(): Array<String>? {
        return findChannelIdsForGroup(NOTIFICATIONS_GROUP_ID)
    }

    override fun findChannelIdsForGroup(channelGroupId: String?): Array<String>? {
        if (channelGroupId == null) {
            return null
        }

        val result: MutableList<String> = ArrayList()

        var itemGroup: ItemGroup? = null

        for (group in mChannelGroups) {
            if (group.id == channelGroupId) {
                itemGroup = group
                break
            }
        }

        itemGroup?.let {
            for (channel in it.items) {
                channel.channelId?.let { result.add(it) }
            }
        }

        return result.toTypedArray().ifEmpty { null }
    }

    override fun isEmpty(): Boolean {
        return mChannelGroups.isEmpty()
    }

    override fun importGroupsObserve(uri: Uri): Observable<List<ItemGroup>> {
        return RxHelper.fromCallable { importGroupsReal(uri) }
    }

    override fun importGroupsObserve(file: File): Observable<List<ItemGroup>> {
        return RxHelper.fromCallable { importGroupsReal(file) }
    }

    override fun createChannelGroup(title: String, iconUrl: String?, items: List<Item>): ItemGroup {
        return ItemGroupImpl(title = title, iconUrl = iconUrl, items = items.toMutableList())
    }

    override fun renameChannelGroup(itemGroup: ItemGroup, title: String) {
        addChannelGroup(ItemGroupImpl(itemGroup.id, title, itemGroup.iconUrl, itemGroup.items))
    }

    override fun createChannel(channelId: String, title: String?, iconUrl: String?): Item {
        return ItemImpl(channelId = channelId, title = title, iconUrl = iconUrl)
    }

    private fun importGroupsReal(uri: Uri): List<ItemGroup>? {
        val groups = mImportServices.firstNotNullOfOrNull { it.importGroups(uri) } ?: return null
        return persistGroups(groups)
    }

    private fun importGroupsReal(file: File): List<ItemGroup>? {
        val groups = mImportServices.firstNotNullOfOrNull {
            val result = it.importGroups(file)
            if (it is NewPipeService && result != null) {
                // NewPipe can export only subscribed channels
                result.firstOrNull()?.items?.let {
                    getSubscribedChannelGroup().addAll(it)
                }
                emptyList()
            } else
                result
        } ?: return null
        return persistGroups(groups)
    }

    private fun persistGroups(groups: List<ItemGroup>): List<ItemGroup> {
        val result = mutableListOf<ItemGroup>()

        groups.forEach {
            //val idx = mChannelGroups?.indexOf(it) ?: -1
            val contains = Helpers.containsIf(mChannelGroups) { item -> item.title == it.title }
            if (contains) { // already exists
                //mChannelGroups?.add(ChannelGroupImpl(title = "${it.title} 2", iconUrl = it.iconUrl, channels = it.channels))
                return@forEach
            }

            mChannelGroups.add(it)
            result.add(it)
        }

        if (result.isNotEmpty()) {
            persistState()
        }

        return result
    }

    override fun exportData(data: String?) {
        data?.let {
            restoreState(it)
            persistState()
        }
    }

    @JvmStatic
    fun subscribe(subscribe: Boolean, channelId: String) {
        
        val group: ItemGroup = getSubscribedChannelGroup()

        if (subscribe) {
            
            val metadata = YouTubeDataServiceInt.getChannelMetadata(channelId)?.firstOrNull()
            
            val channel = ItemImpl(
                channelId, 
                metadata?.title, 
                metadata?.cardImageUrl
            )

            cachedChannel = channel

            group.add(channel)
        
        } else {
            group.remove(channelId)
        }

    }

    fun isSubscribed(channelId: String): Boolean {
        val group: ItemGroup? = findChannelGroupById(SUBSCRIPTIONS_GROUP_ID)

        return group?.contains(channelId) ?: false
    }

    private fun restoreState() {
        val data = MediaServicePrefs.getData(CHANNEL_GROUP_DATA)
        restoreState(data)
    }

    private fun restoreState(data: String?) {
        val split = Helpers.splitData(data)

        mChannelGroups = Helpers.parseList(split, 0, ItemGroupImpl::fromString)
    }

    fun persistState() {
        RxHelper.disposeActions(mPersistAction)
        mPersistAction = RxHelper.runAsync(::persistStateReal, 5_000)
    }

    private fun persistStateReal() {
        MediaServicePrefs.setData(CHANNEL_GROUP_DATA, Helpers.mergeData(mChannelGroups))
    }

    private fun findOrInitGroup(id: String, title: String): ItemGroup {
        return findChannelGroupById(id) ?: initGroup(id, title)
    }

    private fun initGroup(id: String, title: String): ItemGroup {
        val group = ItemGroupImpl(id, title, null, mutableListOf())
        addChannelGroup(group)

        return group
    }
}