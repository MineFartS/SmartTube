package minefarts.smarttube.app.views;

import minefarts.smarttube.app.models.data.SettingsGroup;
import minefarts.smarttube.app.models.data.Video;
import minefarts.smarttube.app.models.errors.ErrorFragmentData;
import minefarts.smarttube.app.models.data.BrowseSection;
import minefarts.smarttube.app.models.data.VideoGroup;

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
