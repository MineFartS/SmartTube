package minefarts.smarttube.sabr.parser.models;

import minefarts.smarttube.sabr.protos.videostreaming.FormatId;

public class AudioSelector extends FormatSelector {
    public AudioSelector(String displayName, boolean discardMedia) {
        super(displayName, discardMedia);
    }

    public AudioSelector(String displayName, boolean discardMedia, FormatId... formatIds) {
        super(displayName, discardMedia, formatIds);
    }

    @Override
    public String getMimePrefix() {
        return "audio";
    }
}
