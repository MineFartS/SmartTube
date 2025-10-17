package com.liskovsoft.smartyoutubetv2.common.app.views;

/**
 * View contract for an in-app web browser (used to show external links or activation pages).
 * Implementations should expose loadUrl and navigation callbacks.
 */
public interface WebBrowserView {
    void loadUrl(String url);
}
