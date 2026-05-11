package minefarts.smarttube.app.models.playback;

import com.liskovsoft.sharedutils.data.MediaItemFormatInfo;
import minefarts.smarttube.exoplayer.selector.FormatItem;
import minefarts.smarttube.app.models.playback.ui.SeekBarSegment;
import minefarts.smarttube.app.models.playback.ui.ChatReceiver;
import minefarts.smarttube.app.models.data.Video;
import minefarts.smarttube.app.models.data.VideoGroup;

import java.util.List;
import java.io.InputStream;
import java.util.List;

public interface PlayerEngine {

    int PLAYBACK_MODE_PAUSE = 0;
    int PLAYBACK_MODE_CLOSE = 1;
    int PLAYBACK_MODE_ALL = 2;
    int PLAYBACK_MODE_ONE = 3;
    int PLAYBACK_MODE_SHUFFLE = 4;
    int PLAYBACK_MODE_LIST = 5;
    int PLAYBACK_MODE_REVERSE_LIST = 6;
    
    float ASPECT_RATIO_1_1 = 1f;
    float ASPECT_RATIO_4_3 = 1.33f;
    float ASPECT_RATIO_5_4 = 1.25f;
    float ASPECT_RATIO_16_9 = 1.77f;
    float ASPECT_RATIO_16_10 = 1.6f;
    float ASPECT_RATIO_21_9 = 2.33f;
    float ASPECT_RATIO_64_27 = 2.37f;
    float ASPECT_RATIO_221_1 = 2.21f;
    float ASPECT_RATIO_235_1 = 2.35f;
    float ASPECT_RATIO_239_1 = 2.39f;

    default void openSabr(MediaItemFormatInfo formatInfo) {
        // NOP
    }
    
    default void openDash(MediaItemFormatInfo formatInfo) {
        // NOP
    }
    
    default void openDash(InputStream dashManifest) {
        // NOP
    }
    
    default void openDashUrl(String dashManifestUrl) {
        // NOP
    }
    
    default void openHlsUrl(String hlsPlaylistUrl) {
        // NOP
    }
    
    default void openUrlList(List<String> urlList) {
        // NOP
    }
    
    default void openMerged(MediaItemFormatInfo formatInfo, String hlsPlaylistUrl) {
        // NOP
    }
    
    default void openMerged(InputStream dashManifest, String hlsPlaylistUrl) {
        // NOP
    }
    
    default Long getPositionMs() {
        return null;
    }
    
    default void setPositionMs(long positionMs) {
        // NOP
    }
    
    default Long getDurationMs() {
        return null;
    }
    
    default void setPlayWhenReady(boolean play) {
        // NOP
    }
    
    default Boolean getPlayWhenReady() {
        return null;
    }
    
    default Boolean isPlaying() {
        return null;
    }
    
    default Boolean isLoading() {
        return null;
    }
    
    default List<FormatItem> getVideoFormats() {
        return null;
    }
    
    default List<FormatItem> getAudioFormats() {
        return null;
    }
    
    default List<FormatItem> getSubtitleFormats() {
        return null;
    }
    
    default void setFormat(FormatItem option) {
        // NOP
    }
    
    default FormatItem getVideoFormat() {
        return null;
    }
    
    default FormatItem getAudioFormat() {
        return null;
    }
    
    default FormatItem getSubtitleFormat() {
        return null;
    }
    
    default Boolean isEngineInitialized() {
        return null;
    }
    
    default void restartEngine() {
        // NOP
    }
    
    default void reloadPlayback() {
        // NOP
    }
    
    default void blockEngine(boolean block) {
        // NOP
    }
    
    default Boolean isEngineBlocked() {
        return null;
    }
    
    default Boolean containsMedia() {
        return null;
    }
    
    default void setSpeed(float speed) {
        // NOP
    }
    
    default Float getSpeed() {
        return null;
    }
    
    default void setVolume(float volume) {
        // NOP
    }
    
    default Float getVolume() {
        return null;
    }

    default void setVideoGravity(int gravity) {
        // NOP
    }

    default void showProgressBar(boolean show) {
        // NOP
    }
    
    default void setVideo(Video item) {
        // NOP
    }
    
    default Video getVideo() {
        return null;
    }
    
    default void finish() {
        // NOP
    }
    
    default void finishReally() {
        // NOP
    }
    
    default void showBackground(String url) {
        // NOP
    }
    
    default void showBackgroundColor(int colorResId) {
        // NOP
    }
    
    default void resetPlayerState() {
        // NOP
    }
    
    default Boolean isEmbed() {
        return null;
    }

    default void updateSuggestions(VideoGroup group) {
        // NOP
    }
    
    default void removeSuggestions(VideoGroup group) {
        // NOP
    }
    
    default Integer getSuggestionsIndex(VideoGroup group) {
        return null;
    }
    
    default VideoGroup getSuggestionsByIndex(int index) {
        return null;
    }
    
    default void focusSuggestedItem(int index) {
        // NOP
    }
    
    default void focusSuggestedItem(Video video) {
        // NOP
    }
    
    default void resetSuggestedPosition() {
        // NOP
    }
    
    default Boolean  isSuggestionsEmpty() {
        return null;
    }
    
    default void clearSuggestions() {
        // NOP
    }
    
    default void showOverlay(boolean show) {
        // NOP
    }
    
    default Boolean  isOverlayShown() {
        return null;
    }
    
    default void showSuggestions(boolean show) {
        // NOP
    }
    
    default Boolean  isSuggestionsShown() {
        return null;
    }
    
    default void showControls(boolean show) {
        // NOP
    }
    
    default Boolean  isControlsShown() {
        return null;
    }
    
    default void setButtonState(int buttonId, int buttonState) {
        // NOP
    }
    
    default void setChannelIcon(String iconUrl) {
        // NOP
    }
    
    default void setSeekPreviewTitle(String title) {
        // NOP
    }
    
    default void setNextTitle(Video nextVideo) {
        // NOP
    }
    
    default void showSubtitles(boolean show) {
        // NOP
    }
    
    default void loadStoryboard() {
        // NOP
    }
    
    default void setTitle(String title) {
        // NOP
    }
    
    default void setSeekBarSegments(List<SeekBarSegment> segments) {
        // NOP
    }
    
    default void setChatReceiver(ChatReceiver chatReceiver) {
        // NOP
    }

}
