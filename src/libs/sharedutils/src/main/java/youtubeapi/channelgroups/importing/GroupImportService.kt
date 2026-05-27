package minefarts.sharedutils.channelgroups.importing

import android.net.Uri
import minefarts.sharedutils.data.ItemGroup
import java.io.File

internal interface GroupImportService {
    fun importGroups(url: Uri): List<ItemGroup>?
    fun importGroups(file: File): List<ItemGroup>?
}
