package minefarts.googleapi.service

import minefarts.googleapi.youtubedata3.YouTubeDataServiceInt
import minefarts.googleapi.youtubedata3.impl.ItemMetadata
import minefarts.sharedutils.rx.RxHelper
import io.reactivex.Observable

object YouTubeDataService {
    @JvmStatic
    fun getVideoMetadata(vararg videoIds: String): Observable<List<ItemMetadata>?> {
        return RxHelper.fromCallable { YouTubeDataServiceInt.getVideoMetadata(*videoIds) }
    }

    @JvmStatic
    fun getChannelMetadata(vararg channelIds: String): Observable<List<ItemMetadata>?> {
        return RxHelper.fromCallable { YouTubeDataServiceInt.getChannelMetadata(*channelIds) }
    }

    @JvmStatic
    fun getPlaylistMetadata(vararg playlistIds: String): Observable<List<ItemMetadata>?> {
        return RxHelper.fromCallable { YouTubeDataServiceInt.getPlaylistMetadata(*playlistIds) }
    }
}