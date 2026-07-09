package minefarts.smarttube.utils.channelgroups.importing.newpipe

import android.net.Uri
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import minefarts.smarttube.google.youtubedata3.YouTubeDataServiceInt
import minefarts.smarttube.utils.data.ItemGroup
import minefarts.smarttube.utils.data.ItemGroup.Item
import minefarts.smarttube.utils.channelgroups.importing.GroupImportService
import minefarts.smarttube.utils.channelgroups.importing.newpipe.gen.NewPipeSubscriptionsGroup
import minefarts.smarttube.utils.channelgroups.models.ItemGroupImpl
import minefarts.smarttube.utils.channelgroups.models.ItemImpl
import minefarts.smarttube.google.common.helpers.YouTubeHelper
import minefarts.smarttube.utils.app.nsigsolver.common.YouTubeInfoExtractor
import java.io.File

public object NewPipeService: GroupImportService {

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

    private fun parseGroups(newPipeContent: String): List<ItemGroup>? {
        val gson = Gson()
        val myType = object : TypeToken<NewPipeSubscriptionsGroup>() {}.type

        val response: NewPipeSubscriptionsGroup = try {
            gson.fromJson(newPipeContent, myType)
        } catch (e: JsonSyntaxException) {
            return null
        }

        val result = mutableListOf<ItemGroup>()

        val items: MutableList<Item> = mutableListOf()

        // channel url: https://www.youtube.com/channel/UCbWcXB0PoqOsAvAdfzWMf0w
        response.subscriptions?.forEach { items.add(ItemImpl(channelId = YouTubeHelper.extractChannelId(Uri.parse(it.url)), title = it.name)) }

        // Get channels thumbs and titles
        val metadata = YouTubeDataServiceInt.getChannelMetadata(*items.mapNotNull { it.channelId }.toTypedArray())
        val newItems = metadata?.map { ItemImpl(it.channelId, it.title, it.cardImageUrl) }

        result.add(ItemGroupImpl(title = NewPipeSubscriptionsGroup::subscriptions.name, items = newItems?.toMutableList() ?: items))

        return result
    }

}