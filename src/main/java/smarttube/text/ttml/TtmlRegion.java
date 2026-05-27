package minefarts.smarttube.text.ttml;

import minefarts.smarttube.text.Cue;

/**
 * Represents a TTML Region.
 */
/* package */ final class TtmlRegion {

  public final String id;
  public final float position;
  public final float line;
  public final @Cue.LineType int lineType;
  public final @Cue.AnchorType int lineAnchor;
  public final float width;
  public final float height;
  public final @Cue.TextSizeType int textSizeType;
  public final float textSize;

  public TtmlRegion(String id) {
    this(
        id,
        /* position= */ Cue.DIMEN_UNSET,
        /* line= */ Cue.DIMEN_UNSET,
        /* lineType= */ Cue.TYPE_UNSET,
        /* lineAnchor= */ Cue.TYPE_UNSET,
        /* width= */ Cue.DIMEN_UNSET,
        /* height= */ Cue.DIMEN_UNSET,
        /* textSizeType= */ Cue.TYPE_UNSET,
        /* textSize= */ Cue.DIMEN_UNSET);
  }

  public TtmlRegion(
      String id,
      float position,
      float line,
      @Cue.LineType int lineType,
      @Cue.AnchorType int lineAnchor,
      float width,
      float height,
      int textSizeType,
      float textSize) {
    this.id = id;
    this.position = position;
    this.line = line;
    this.lineType = lineType;
    this.lineAnchor = lineAnchor;
    this.width = width;
    this.height = height;
    this.textSizeType = textSizeType;
    this.textSize = textSize;
  }

}
