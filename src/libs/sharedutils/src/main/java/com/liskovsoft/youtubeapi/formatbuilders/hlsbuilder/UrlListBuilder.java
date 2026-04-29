package com.liskovsoft.sharedutils.formatbuilders.hlsbuilder;

import com.liskovsoft.sharedutils.data.MediaFormat;

import java.util.List;

public interface UrlListBuilder {
    void append(MediaFormat mediaItem);
    boolean isEmpty();
    List<String> buildUriList();
}
