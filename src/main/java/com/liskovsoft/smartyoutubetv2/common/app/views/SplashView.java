package com.liskovsoft.smartyoutubetv2.common.app.views;

/**
 * Splash screen view shown during app startup/initialization.
 * Delegates routing and initial checks to SplashPresenter.
 */
public interface SplashView {
    Intent getNewIntent();
    void finishView();
}
