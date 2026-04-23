package SmartTubeApp.app.models.playback.listener;

import SmartTubeApp.app.models.data.Video;

public interface PlayerUiEventListener {
    void onSuggestionItemClicked(Video item);
    void onSuggestionItemLongClicked(Video item);
    void onScrollEnd(Video item);
    boolean onPreviousClicked();
    boolean onNextClicked();
    void onPlayClicked();
    void onPauseClicked();
    boolean onKeyDown(int keyCode);
    void onButtonClicked(int buttonId, int buttonState);
    void onButtonLongClicked(int buttonId, int buttonState);
    void onControlsShown(boolean shown);
}
