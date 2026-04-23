package SmartTubeApp.app.models.playback.manager;

import SmartTubeApp.app.models.data.Video;
import SmartTubeApp.app.models.data.VideoGroup;
import SmartTubeApp.app.models.playback.ui.SeekBarSegment;
import SmartTubeApp.app.models.playback.ui.ChatReceiver;

import java.util.List;

public interface PlayerUI {

    int BUTTON_OFF = 0;
    int BUTTON_ON = 1;
    int BUTTON_DISABLED = -1;

    void updateSuggestions(VideoGroup group);
    void removeSuggestions(VideoGroup group);
    int getSuggestionsIndex(VideoGroup group);
    VideoGroup getSuggestionsByIndex(int index);
    void focusSuggestedItem(int index);
    void focusSuggestedItem(Video video);
    void resetSuggestedPosition();
    boolean isSuggestionsEmpty();
    void clearSuggestions();
    void showOverlay(boolean show);
    boolean isOverlayShown();
    void showSuggestions(boolean show);
    boolean isSuggestionsShown();
    void showControls(boolean show);
    boolean isControlsShown();
    void setButtonState(int buttonId, int buttonState);
    void setChannelIcon(String iconUrl);
    void setSeekPreviewTitle(String title);
    void setNextTitle(Video nextVideo);
    void showSubtitles(boolean show);
    void loadStoryboard();
    void setTitle(String title);
    void showProgressBar(boolean show);
    void setSeekBarSegments(List<SeekBarSegment> segments);
    void setChatReceiver(ChatReceiver chatReceiver);
    
}
