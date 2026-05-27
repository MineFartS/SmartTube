package minefarts.exoplayer2.source.hls;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import androidx.annotation.IntDef;
import java.lang.annotation.Retention;

/**
 * The types of metadata that can be extracted from HLS streams.
 *
 * <p>See {@link HlsMediaSource.Factory#setMetadataType(int)}.
 */
@Retention(SOURCE)
@IntDef({HlsMetadataType.ID3, HlsMetadataType.EMSG})
public @interface HlsMetadataType {
  int ID3 = 1;
  int EMSG = 3;
}
