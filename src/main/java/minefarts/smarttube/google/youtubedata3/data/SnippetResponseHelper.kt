package minefarts.smarttube.google.youtubedata3.data

private const val TYPE_VIDEO = "youtube#video"
private const val TYPE_CHANNEL = "youtube#channel"
private const val TYPE_PLAYLIST = "youtube#playlist"

public fun SnippetWrapper.getTitle(): String? = snippet?.title
public fun SnippetWrapper.getVideoId(): String? = if (isVideo()) id else null
public fun SnippetWrapper.getChannelId(): String? = snippet?.channelId ?: id
public fun SnippetWrapper.getPlaylistId(): String? = id
public fun SnippetWrapper.getChannelTitle(): String? = snippet?.channelTitle ?: snippet?.title
public fun SnippetWrapper.getPublishedAt(): String? = snippet?.publishedAt
public fun SnippetWrapper.getDescription(): String? = snippet?.description
public fun SnippetWrapper.getChannelUrl(): String? = snippet?.customUrl
public fun SnippetWrapper.getCategoryId(): String? = snippet?.categoryId
public fun SnippetWrapper.getThumbnailUrl(): String? = snippet?.thumbnails?.medium?.url
public fun SnippetWrapper.getDurationIso(): String? = contentDetails?.duration
public fun SnippetWrapper.getItemCount(): Int? = contentDetails?.itemCount
private fun SnippetWrapper.isVideo() = kind == TYPE_VIDEO
private fun SnippetWrapper.isChannel() = kind == TYPE_CHANNEL
private fun SnippetWrapper.isPlaylist() = kind == TYPE_PLAYLIST