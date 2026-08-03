package minefarts.smarttube.utils.playlist.models;

import minefarts.smarttube.google.common.converters.jsonpath.JsonPath;

import java.util.List;

public class PlaylistsResult {
    @JsonPath("$.contents[0].addToPlaylistRenderer.playlists[*].playlistAddToOptionRenderer")
    private List<PlaylistInfoItem> mPlaylists;

    public List<PlaylistInfoItem> getPlaylists() {
        return mPlaylists;
    }
}
