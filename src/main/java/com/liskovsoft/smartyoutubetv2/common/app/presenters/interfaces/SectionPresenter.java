package com.liskovsoft.smartyoutubetv2.common.app.presenters.interfaces;

/** Presenter interface for a browse section (load/refresh/selection callbacks). */
public interface SectionPresenter {
    void onSectionFocused(int sectionId);
    void onSectionLongPressed(int sectionId);
}
