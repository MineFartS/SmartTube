package minefarts.smarttube.utils.channelgroups.importing.grayjay

import android.net.Uri
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import minefarts.smarttube.utils.data.ItemGroup
import minefarts.smarttube.utils.data.ItemGroup.Item
import minefarts.smarttube.utils.channelgroups.importing.GroupImportService
import minefarts.smarttube.utils.channelgroups.importing.grayjay.gen.GrayJayGroup
import minefarts.smarttube.utils.channelgroups.models.ItemGroupImpl
import minefarts.smarttube.utils.channelgroups.models.ItemImpl
import minefarts.smarttube.google.common.helpers.YouTubeHelper
import minefarts.smarttube.utils.app.nsigsolver.common.YouTubeInfoExtractor
import java.io.File

public object GrayJayService: GroupImportService {
    
    override fun importGroups(url: Uri): List<ItemGroup>? {
        return try {
            return parseGroups(
                YouTubeInfoExtractor.downloadWebpage(url.toString())
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun importGroups(file: File): List<ItemGroup>? {
        return parseGroups(file.readText())
    }

    private fun parseGroups(grayJayContent: String): List<ItemGroup>? {
        // replace:
        // "{ => {
        // }" => }
        // \" => "

        val grayJayContentFixed = grayJayContent
            .replace("\"{", "{")
            .replace("}\"", "}")
            .replace("\\\"", "\"")

        val gson = Gson()
        val listType = object : TypeToken<List<GrayJayGroup>>() {}.type

        val response: List<GrayJayGroup> = try {
            gson.fromJson(grayJayContentFixed, listType)
        } catch (e: JsonSyntaxException) {
            return null
        }

        val result = mutableListOf<ItemGroup>()

        for (group in response) {
            val items: MutableList<Item> = mutableListOf()

            // channel url: https://www.youtube.com/channel/UCbWcXB0PoqOsAvAdfzWMf0w
            group.urls?.forEach { items.add(ItemImpl(channelId = YouTubeHelper.extractChannelId(Uri.parse(it)))) }

            result.add(ItemGroupImpl(group.id, group.name, group.image?.url, items))
        }

        return result
    }

}