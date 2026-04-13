
package com.google.android.exoplayer2.ext.cast;

import android.content.Context;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;
import java.util.Collections;
import java.util.List;

/**
 * A convenience {@link OptionsProvider} to target the default cast receiver app.
 */
public final class DefaultCastOptionsProvider implements OptionsProvider {

  @Override
  public CastOptions getCastOptions(Context context) {
    return new CastOptions.Builder()
        .setReceiverApplicationId(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID)
        .setStopReceiverApplicationWhenEndingSession(true).build();
  }

  @Override
  public List<SessionProvider> getAdditionalSessionProviders(Context context) {
    return Collections.emptyList();
  }

}
