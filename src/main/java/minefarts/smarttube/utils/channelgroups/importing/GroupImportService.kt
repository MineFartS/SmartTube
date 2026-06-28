package minefarts.smarttube.utils.channelgroups.importing

import android.net.Uri
import minefarts.smarttube.utils.data.ItemGroup
import java.io.File

public interface GroupImportService {
    fun importGroups(url: Uri): List<ItemGroup>?
    fun importGroups(file: File): List<ItemGroup>?
}
