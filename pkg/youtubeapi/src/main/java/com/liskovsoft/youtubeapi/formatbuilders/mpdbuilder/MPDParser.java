package com.liskovsoft.youtubeapi.formatbuilders.mpdbuilder;

import com.liskovsoft.youtubeapi.data.MediaFormat;

import java.util.List;

public interface MPDParser {
    List<MediaFormat> parse();
}
