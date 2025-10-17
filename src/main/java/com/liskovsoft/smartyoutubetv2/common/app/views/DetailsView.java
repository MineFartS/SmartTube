package com.liskovsoft.smartyoutubetv2.common.app.views;

/**
 * View contract for detailed video page (description, metadata, actions and comments).
 * Provides methods to show/hide loading, update video metadata and open links.
 */
public interface DetailsView {
    void openVideo(Video video);
}
