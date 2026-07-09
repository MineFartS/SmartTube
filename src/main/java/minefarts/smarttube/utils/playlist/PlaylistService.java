package minefarts.smarttube.utils.playlist;

import minefarts.smarttube.utils.data.PlaylistInfo;
import minefarts.smarttube.utils.actions.models.ActionResult;
import minefarts.smarttube.google.common.helpers.RetrofitHelper;
import minefarts.smarttube.utils.playlist.impl.YouTubePlaylistInfo;
import minefarts.smarttube.utils.playlist.models.PlaylistsResult;

import java.util.List;

import retrofit2.Call;

public class PlaylistService {

    private final PlaylistApi mPlaylistManager;

    public PlaylistService() {
        mPlaylistManager = RetrofitHelper.create(PlaylistApi.class);
    }

    public List<PlaylistInfo> getPlaylistsInfo(String videoId) {
        
        Call<PlaylistsResult> wrapper = mPlaylistManager.getPlaylistsInfo(PlaylistApiHelper.getPlaylistsInfoQuery(videoId));

        return YouTubePlaylistInfo.from(RetrofitHelper.get(wrapper));
    
    }

    public void addToPlaylist(String playlistId, String videoId) {

        Call<ActionResult> wrapper = mPlaylistManager.editPlaylist(PlaylistApiHelper.getAddToPlaylistQuery(playlistId, videoId));

        RetrofitHelper.get(wrapper);

    }

    public void removeFromPlaylist(String playlistId, String videoId) {
        
        Call<ActionResult> wrapper = mPlaylistManager.editPlaylist(PlaylistApiHelper.getRemoveFromPlaylistsQuery(playlistId, videoId));

        RetrofitHelper.get(wrapper);

    }

    public void renamePlaylist(String playlistId, String newName) {
        
        Call<ActionResult> wrapper = mPlaylistManager.editPlaylist(PlaylistApiHelper.getRenamePlaylistsQuery(playlistId, newName));

        RetrofitHelper.get(wrapper, true, true);

    }

    public void setPlaylistOrder(String playlistId, int playlistOrder) {
        
        Call<ActionResult> wrapper = mPlaylistManager.editPlaylist(PlaylistApiHelper.getPlaylistOrderQuery(playlistId, playlistOrder));

        RetrofitHelper.get(wrapper, true, true);

    }

    public void savePlaylist(String playlistId) {
        
        Call<ActionResult> wrapper = mPlaylistManager.saveForeignPlaylist(PlaylistApiHelper.getSaveRemoveForeignPlaylistQuery(playlistId));

        RetrofitHelper.get(wrapper, true, true);

    }

    public void removePlaylist(String playlistId) {
        
        // Try to remove foreign playlist first
        Call<ActionResult> removeWrapper = mPlaylistManager.removeForeignPlaylist(PlaylistApiHelper.getSaveRemoveForeignPlaylistQuery(playlistId));
        
        RetrofitHelper.get(removeWrapper, true, true);

        // Then, delete user playlist
        Call<ActionResult> deleteWrapper = mPlaylistManager.removePlaylist(PlaylistApiHelper.getRemovePlaylistQuery(playlistId));
        
        RetrofitHelper.get(deleteWrapper, true, true);
    
    }

    public void createPlaylist(String playlistName, String videoId) {
        
        Call<ActionResult> wrapper = mPlaylistManager.createPlaylist(PlaylistApiHelper.getCreatePlaylistQuery(playlistName, videoId));

        RetrofitHelper.get(wrapper, true, true);

    }
    
}
