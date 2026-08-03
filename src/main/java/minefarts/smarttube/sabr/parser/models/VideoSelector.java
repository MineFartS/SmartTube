package minefarts.smarttube.sabr.parser.models;

import minefarts.smarttube.sabr.protos.videostreaming.FormatId;

public class VideoSelector extends FormatSelector {
    public VideoSelector(String displayName, boolean discardMedia) {
        super(displayName, discardMedia);
    }

    public VideoSelector(String displayName, boolean discardMedia, FormatId... formatIds) {
        super(displayName, discardMedia, formatIds);
    }

    @Override
    public String getMimePrefix() {
        return "video";
    }
}
