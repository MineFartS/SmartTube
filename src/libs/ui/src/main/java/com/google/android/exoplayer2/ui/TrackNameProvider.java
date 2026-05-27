package com.google.android.exoplayer2.ui;

import com.google.android.exoplayer2.Format;

/** Converts {@link Format}s to user readable track names. */
public interface TrackNameProvider {

  /** Returns a user readable track name for the given {@link Format}. */
  String getTrackName(Format format);
}
