package minefarts.exoplayer2.source.sabr.parser.parts;

import minefarts.exoplayer2.source.sabr.parser.models.FormatSelector;
import minefarts.exoplayer2.source.sabr.protos.videostreaming.FormatId;

public class FormatInitializedSabrPart implements SabrPart {
    public final FormatId formatId;
    public final FormatSelector formatSelector;

    public FormatInitializedSabrPart(FormatId formatId, FormatSelector formatSelector) {
        this.formatId = formatId;
        this.formatSelector = formatSelector;
    }
}
