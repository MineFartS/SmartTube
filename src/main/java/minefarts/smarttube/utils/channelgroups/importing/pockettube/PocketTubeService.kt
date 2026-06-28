package minefarts.smarttube.utils.channelgroups.importing.pockettube

import android.net.Uri
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import minefarts.smarttube.utils.data.ItemGroup
import minefarts.smarttube.utils.data.ItemGroup.Item
import minefarts.smarttube.utils.channelgroups.importing.GroupImportService
import minefarts.smarttube.utils.channelgroups.models.ItemGroupImpl
import minefarts.smarttube.utils.channelgroups.models.ItemImpl
import minefarts.smarttube.utils.app.nsigsolver.common.YouTubeInfoExtractor
import java.io.File

public object PocketTubeService: GroupImportService {
    override fun importGroups(url: Uri): List<ItemGroup>? {
        val pocketTubeContent = YouTubeInfoExtractor.downloadWebpageSilent(url.toString()) ?: return null

        return parseGroups(pocketTubeContent)
    }

    override fun importGroups(file: File): List<ItemGroup>? {
        return parseGroups(file.readText())
    }

    private fun parseGroups(pocketTubeContent: String): List<ItemGroup>? {
        // Find group names
        val groupNames: List<String> = try {
            JsonPath.read(pocketTubeContent, "$.ysc_collection.*~")
        } catch (e: PathNotFoundException) {
            return null
        }

        val result = mutableListOf<ItemGroup>()

        for (groupName in groupNames) {
            // Get groups content
            val channelIds: List<String> = JsonPath.read(pocketTubeContent, "$['$groupName']")

            val items: MutableList<Item> = mutableListOf()

            // channel id: UCsjTlfV61bBwzLLmenR5zmg
            channelIds.forEach { items.add(ItemImpl(channelId = it)) }

            result.add(ItemGroupImpl(title = groupName, items = items))
        }

        return result
    }
}