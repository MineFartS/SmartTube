package minefarts.smarttube.google.youtubedata3.impl

import minefarts.smarttube.google.youtubedata3.data.*

public class ItemMetadata(private val snippetWrapper: SnippetWrapper) {
    val title: String? by lazy { snippetWrapper.getTitle() }
    val cardImageUrl: String? by lazy { snippetWrapper.getThumbnailUrl() }
    val channelId: String? by lazy { snippetWrapper.getChannelId() }
    val videoId: String? by lazy { snippetWrapper.getVideoId() }
    val playlistId: String? by lazy { snippetWrapper.getPlaylistId() }
    val channelTitle: String? by lazy { snippetWrapper.getChannelTitle() }
    val publishedAt: String? by lazy { snippetWrapper.getPublishedAt() }
    val durationIso: String? by lazy { snippetWrapper.getDurationIso() }
    val itemCount: Int? by lazy { snippetWrapper.getItemCount() }
}

