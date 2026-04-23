package com.liskovsoft.sharedutils.formatbuilders.mpdbuilder;

import com.liskovsoft.sharedutils.data.MediaFormat;

import java.util.List;

public interface MPDParser {
    List<MediaFormat> parse();
}
