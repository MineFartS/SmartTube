package com.liskovsoft.smartyoutubetv2.common.misc;

/**
 * Contract for lightweight processors that can modify/inspect VideoGroup items.
 * Implementations should be fast; heavy work must be offloaded. Use OnItemReady to report per-item results.
 */
public interface BrowseProcessor {
    interface OnItemReady {
        void onItemReady(Video video);
    }
    void process(VideoGroup videoGroup);
    void dispose();
}
