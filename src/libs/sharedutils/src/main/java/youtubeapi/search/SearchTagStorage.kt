package minefarts.sharedutils.search

import minefarts.sharedutils.helpers.Helpers
import minefarts.sharedutils.service.internal.MediaServicePrefs
import minefarts.sharedutils.helpers.LruList

internal object SearchTagStorage: MediaServicePrefs.ProfileChangeListener {

    private const val SEARCH_TAG_DATA = "search_tag_data"

    private val _tags: MutableList<String> = LruList(50)

    @JvmStatic
    val tags: List<String>
        get() = _tags.reversed()

    init {
        MediaServicePrefs.addListener(this)
        restoreState()
    }

    @JvmStatic
    fun saveTag(tag: String?) {
        if (tag == null)
            return

        _tags.add(tag)

        persistState()
    }

    @JvmStatic
    fun clear() {
        _tags.clear()

        persistState()
    }

    override fun onProfileChanged() {
        restoreState()
    }

    private fun restoreState() {
        _tags.clear()

        val data = MediaServicePrefs.getData(SEARCH_TAG_DATA)

        val split = Helpers.splitData(data)

        val tags = Helpers.parseStrList(split, 0)

        _tags.addAll(tags)
    }

    private fun persistState() {
        MediaServicePrefs.setData(SEARCH_TAG_DATA, Helpers.mergeData(_tags))
    }
}