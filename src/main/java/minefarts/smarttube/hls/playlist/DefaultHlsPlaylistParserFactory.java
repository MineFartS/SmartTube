package minefarts.smarttube.hls.playlist;

import minefarts.smarttube.upstream.ParsingLoadable;

/** Default implementation for {@link HlsPlaylistParserFactory}. */
public final class DefaultHlsPlaylistParserFactory implements HlsPlaylistParserFactory {

  @Override
  public ParsingLoadable.Parser<HlsPlaylist> createPlaylistParser() {
    return new HlsPlaylistParser();
  }

  @Override
  public ParsingLoadable.Parser<HlsPlaylist> createPlaylistParser(
      HlsMasterPlaylist masterPlaylist) {
    return new HlsPlaylistParser(masterPlaylist);
  }
}
