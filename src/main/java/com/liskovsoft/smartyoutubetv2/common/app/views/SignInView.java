package com.liskovsoft.smartyoutubetv2.common.app.views;

/**
 * View contract for sign-in flows. Shows device/user codes, status and exposes close callbacks.
 * Used by YT/Google sign-in presenters.
 */
public interface SignInView {
    void showCode(String userCode, String signInUrl);
    void close();
}
