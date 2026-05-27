package minefarts.smarttube.utils.formatbuilders.mpdbuilder;

import minefarts.smarttube.utils.service.data.MediaFormat;

import java.util.List;

public interface MPDParser {
    List<MediaFormat> parse();
}
