package smartyoutubetv1.app.views;

import smartyoutubetv1.app.models.data.SettingsGroup;
import smartyoutubetv1.app.models.data.Video;
import smartyoutubetv1.app.models.errors.ErrorFragmentData;
import smartyoutubetv1.app.models.data.BrowseSection;
import smartyoutubetv1.app.models.data.VideoGroup;

public interface BrowseView {
    void addSection(int index, BrowseSection section);
    void removeSection(BrowseSection category);
    void removeAllSections();
    void selectSection(int index, boolean focusOnContent);
    void updateSection(VideoGroup group);
    void updateSection(SettingsGroup group);
    void clearSection(BrowseSection section);
    void selectSectionItem(int index);
    void selectSectionItem(Video item);
    void showError(ErrorFragmentData data);
    void showProgressBar(boolean show);
    boolean isProgressBarShowing();
    void focusOnContent();
    boolean isEmpty();
}
