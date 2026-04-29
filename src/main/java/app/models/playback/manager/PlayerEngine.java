package SmartTubeApp.app.models.playback.manager;

import com.liskovsoft.sharedutils.data.MediaItemFormatInfo;
import SmartTubeApp.exoplayer.selector.FormatItem;

import java.io.InputStream;
import java.util.List;

public interface PlayerEngine extends PlayerConstants {
    void openSabr(MediaItemFormatInfo formatInfo);
    void openDash(MediaItemFormatInfo formatInfo);
    void openDash(InputStream dashManifest);
    void openDashUrl(String dashManifestUrl);
    void openHlsUrl(String hlsPlaylistUrl);
    void openUrlList(List<String> urlList);
    void openMerged(MediaItemFormatInfo formatInfo, String hlsPlaylistUrl);
    void openMerged(InputStream dashManifest, String hlsPlaylistUrl);
    long getPositionMs();
    void setPositionMs(long positionMs);
    long getDurationMs();
    void setPlayWhenReady(boolean play);
    boolean getPlayWhenReady();
    boolean isPlaying();
    boolean isLoading();
    List<FormatItem> getVideoFormats();
    List<FormatItem> getAudioFormats();
    List<FormatItem> getSubtitleFormats();
    void setFormat(FormatItem option);
    FormatItem getVideoFormat();
    FormatItem getAudioFormat();
    FormatItem getSubtitleFormat();
    boolean isEngineInitialized();
    void restartEngine();
    void reloadPlayback();
    void blockEngine(boolean block);
    boolean isEngineBlocked();
    boolean isInPIPMode();
    boolean containsMedia();
    void setSpeed(float speed);
    float getSpeed();
    void setVolume(float volume);
    float getVolume();
    void setZoomPercents(int percents);
    void setVideoGravity(int gravity);
}
