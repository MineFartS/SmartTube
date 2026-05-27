package minefarts.smarttube.okhttp;

import androidx.annotation.Nullable;
import minefarts.smarttube.upstream.HttpDataSource;
import minefarts.smarttube.upstream.HttpDataSource.BaseFactory;
import minefarts.smarttube.upstream.HttpDataSource.Factory;
import minefarts.smarttube.upstream.TransferListener;
import okhttp3.CacheControl;
import okhttp3.Call;

/**
 * A {@link Factory} that produces {@link OkHttpDataSource}.
 */
public final class OkHttpDataSourceFactory extends BaseFactory {

  private final Call.Factory callFactory;
  private final @Nullable String userAgent;
  private final @Nullable TransferListener listener;
  private final @Nullable CacheControl cacheControl;

  /**
   * @param callFactory A {@link Call.Factory} (typically an {@link okhttp3.OkHttpClient}) for use
   *     by the sources created by the factory.
   * @param userAgent An optional User-Agent string.
   */
  public OkHttpDataSourceFactory(Call.Factory callFactory, @Nullable String userAgent) {
    this(callFactory, userAgent, /* listener= */ null, /* cacheControl= */ null);
  }

  /**
   * @param callFactory A {@link Call.Factory} (typically an {@link okhttp3.OkHttpClient}) for use
   *     by the sources created by the factory.
   * @param userAgent An optional User-Agent string.
   * @param cacheControl An optional {@link CacheControl} for setting the Cache-Control header.
   */
  public OkHttpDataSourceFactory(
      Call.Factory callFactory, @Nullable String userAgent, @Nullable CacheControl cacheControl) {
    this(callFactory, userAgent, /* listener= */ null, cacheControl);
  }

  /**
   * @param callFactory A {@link Call.Factory} (typically an {@link okhttp3.OkHttpClient}) for use
   *     by the sources created by the factory.
   * @param userAgent An optional User-Agent string.
   * @param listener An optional listener.
   */
  public OkHttpDataSourceFactory(
      Call.Factory callFactory, @Nullable String userAgent, @Nullable TransferListener listener) {
    this(callFactory, userAgent, listener, /* cacheControl= */ null);
  }

  /**
   * @param callFactory A {@link Call.Factory} (typically an {@link okhttp3.OkHttpClient}) for use
   *     by the sources created by the factory.
   * @param userAgent An optional User-Agent string.
   * @param listener An optional listener.
   * @param cacheControl An optional {@link CacheControl} for setting the Cache-Control header.
   */
  public OkHttpDataSourceFactory(
      Call.Factory callFactory,
      @Nullable String userAgent,
      @Nullable TransferListener listener,
      @Nullable CacheControl cacheControl) {
    this.callFactory = callFactory;
    this.userAgent = userAgent;
    this.listener = listener;
    this.cacheControl = cacheControl;
  }

  @Override
  protected OkHttpDataSource createDataSourceInternal(
      HttpDataSource.RequestProperties defaultRequestProperties) {
    OkHttpDataSource dataSource =
        new OkHttpDataSource(
            callFactory,
            userAgent,
            /* contentTypePredicate= */ null,
            cacheControl,
            defaultRequestProperties);
    if (listener != null) {
      dataSource.addTransferListener(listener);
    }
    return dataSource;
  }
}
