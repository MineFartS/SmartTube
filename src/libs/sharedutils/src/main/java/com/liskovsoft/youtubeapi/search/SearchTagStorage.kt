package com.liskovsoft.sharedutils.search

import com.liskovsoft.sharedutils.helpers.Helpers
import com.liskovsoft.sharedutils.service.internal.MediaServicePrefs

import java.util.ArrayList;

internal object SearchTagStorage: MediaServicePrefs.ProfileChangeListener {

    private const val SEARCH_TAG_DATA = "search_tag_data"
    
    private val _tags: MutableList<String> = ArrayList<String>()

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