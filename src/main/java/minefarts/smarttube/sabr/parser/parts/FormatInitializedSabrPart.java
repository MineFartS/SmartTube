package minefarts.smarttube.sabr.parser.parts;

import minefarts.smarttube.sabr.parser.models.FormatSelector;
import minefarts.smarttube.sabr.protos.videostreaming.FormatId;

public class FormatInitializedSabrPart implements SabrPart {
    public final FormatId formatId;
    public final FormatSelector formatSelector;

    public FormatInitializedSabrPart(FormatId formatId, FormatSelector formatSelector) {
        this.formatId = formatId;
        this.formatSelector = formatSelector;
    }
}
