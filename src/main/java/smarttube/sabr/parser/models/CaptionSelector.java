package minefarts.smarttube.sabr.parser.models;

import minefarts.smarttube.sabr.protos.videostreaming.FormatId;

public class CaptionSelector extends FormatSelector {
    public CaptionSelector(String displayName, boolean discardMedia) {
        super(displayName, discardMedia);
    }

    public CaptionSelector(String displayName, boolean discardMedia, FormatId... formatIds) {
        super(displayName, discardMedia, formatIds);
    }

    @Override
    public String getMimePrefix() {
        return "text";
    }
}
