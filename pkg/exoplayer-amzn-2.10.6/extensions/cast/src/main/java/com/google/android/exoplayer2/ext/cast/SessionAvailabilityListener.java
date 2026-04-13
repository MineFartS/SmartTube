
package com.google.android.exoplayer2.ext.cast;

/** Listener of changes in the cast session availability. */
public interface SessionAvailabilityListener {

  /** Called when a cast session becomes available to the player. */
  void onCastSessionAvailable();

  /** Called when the cast session becomes unavailable. */
  void onCastSessionUnavailable();
}
