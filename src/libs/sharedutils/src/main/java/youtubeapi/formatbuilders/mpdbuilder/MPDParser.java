package minefarts.sharedutils.formatbuilders.mpdbuilder;

import minefarts.sharedutils.service.data.MediaFormat;

import java.util.List;

public interface MPDParser {
    List<MediaFormat> parse();
}
