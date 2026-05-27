package minefarts.smarttube.ss.offline;

import android.net.Uri;
import minefarts.smarttube.C;
import minefarts.smarttube.offline.DownloaderConstructorHelper;
import minefarts.smarttube.offline.SegmentDownloader;
import minefarts.smarttube.offline.StreamKey;
import minefarts.smarttube.ss.manifest.SsManifest;
import minefarts.smarttube.ss.manifest.SsManifest.StreamElement;
import minefarts.smarttube.ss.manifest.SsManifestParser;
import minefarts.smarttube.ss.manifest.SsUtil;
import minefarts.smarttube.upstream.DataSource;
import minefarts.smarttube.upstream.DataSpec;
import minefarts.smarttube.upstream.ParsingLoadable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A downloader for SmoothStreaming streams.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * SimpleCache cache = new SimpleCache(downloadFolder, new NoOpCacheEvictor(), databaseProvider);
 * DefaultHttpDataSourceFactory factory = new DefaultHttpDataSourceFactory("ExoPlayer", null);
 * DownloaderConstructorHelper constructorHelper =
 *     new DownloaderConstructorHelper(cache, factory);
 * // Create a downloader for the first track of the first stream element.
 * SsDownloader ssDownloader =
 *     new SsDownloader(
 *         manifestUrl,
 *         Collections.singletonList(new StreamKey(0, 0)),
 *         constructorHelper);
 * // Perform the download.
 * ssDownloader.download(progressListener);
 * // Access downloaded data using CacheDataSource
 * CacheDataSource cacheDataSource =
 *     new CacheDataSource(cache, factory.createDataSource(), CacheDataSource.FLAG_BLOCK_ON_CACHE);
 * }</pre>
 */
public final class SsDownloader extends SegmentDownloader<SsManifest> {

  /**
   * @param manifestUri The {@link Uri} of the manifest to be downloaded.
   * @param streamKeys Keys defining which streams in the manifest should be selected for download.
   *     If empty, all streams are downloaded.
   * @param constructorHelper A {@link DownloaderConstructorHelper} instance.
   */
  public SsDownloader(
      Uri manifestUri, List<StreamKey> streamKeys, DownloaderConstructorHelper constructorHelper) {
    super(SsUtil.fixManifestUri(manifestUri), streamKeys, constructorHelper);
  }

  @Override
  protected SsManifest getManifest(DataSource dataSource, DataSpec dataSpec) throws IOException {
    return ParsingLoadable.load(dataSource, new SsManifestParser(), dataSpec, C.DATA_TYPE_MANIFEST);
  }

  @Override
  protected List<Segment> getSegments(
      DataSource dataSource, SsManifest manifest, boolean allowIncompleteList) {
    ArrayList<Segment> segments = new ArrayList<>();
    for (StreamElement streamElement : manifest.streamElements) {
      for (int i = 0; i < streamElement.formats.length; i++) {
        for (int j = 0; j < streamElement.chunkCount; j++) {
          segments.add(
              new Segment(
                  streamElement.getStartTimeUs(j),
                  new DataSpec(streamElement.buildRequestUri(i, j))));
        }
      }
    }
    return segments;
  }

}
