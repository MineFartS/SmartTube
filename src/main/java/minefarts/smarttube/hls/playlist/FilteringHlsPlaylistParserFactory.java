package minefarts.smarttube.hls.playlist;

import minefarts.smarttube.offline.FilteringManifestParser;
import minefarts.smarttube.offline.StreamKey;
import minefarts.smarttube.upstream.ParsingLoadable;
import java.util.List;

/**
 * A {@link HlsPlaylistParserFactory} that includes only the streams identified by the given stream
 * keys.
 */
public final class FilteringHlsPlaylistParserFactory implements HlsPlaylistParserFactory {

  private final HlsPlaylistParserFactory hlsPlaylistParserFactory;
  private final List<StreamKey> streamKeys;

  /**
   * @param hlsPlaylistParserFactory A factory for the parsers of the playlists which will be
   *     filtered.
   * @param streamKeys The stream keys. If null or empty then filtering will not occur.
   */
  public FilteringHlsPlaylistParserFactory(
      HlsPlaylistParserFactory hlsPlaylistParserFactory, List<StreamKey> streamKeys) {
    this.hlsPlaylistParserFactory = hlsPlaylistParserFactory;
    this.streamKeys = streamKeys;
  }

  @Override
  public ParsingLoadable.Parser<HlsPlaylist> createPlaylistParser() {
    return new FilteringManifestParser<>(
        hlsPlaylistParserFactory.createPlaylistParser(), streamKeys);
  }

  @Override
  public ParsingLoadable.Parser<HlsPlaylist> createPlaylistParser(
      HlsMasterPlaylist masterPlaylist) {
    return new FilteringManifestParser<>(
        hlsPlaylistParserFactory.createPlaylistParser(masterPlaylist), streamKeys);
  }
}
