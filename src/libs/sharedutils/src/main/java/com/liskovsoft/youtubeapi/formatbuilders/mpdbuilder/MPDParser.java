package com.liskovsoft.sharedutils.formatbuilders.mpdbuilder;

import com.liskovsoft.sharedutils.service.data.MediaFormat;

import java.util.List;

public interface MPDParser {
    List<MediaFormat> parse();
}
