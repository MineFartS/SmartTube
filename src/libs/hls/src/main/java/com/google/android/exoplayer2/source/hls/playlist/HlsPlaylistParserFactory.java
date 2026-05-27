package com.google.android.exoplayer2.source.hls.playlist;

import com.google.android.exoplayer2.upstream.ParsingLoadable;

/** Factory for {@link HlsPlaylist} parsers. */
public interface HlsPlaylistParserFactory {

  /**
   * Returns a stand-alone playlist parser. Playlists parsed by the returned parser do not inherit
   * any attributes from other playlists.
   */
  ParsingLoadable.Parser<HlsPlaylist> createPlaylistParser();

  /**
   * Returns a playlist parser for playlists that were referenced by the given {@link
   * HlsMasterPlaylist}. Returned {@link HlsMediaPlaylist} instances may inherit attributes from
   * {@code masterPlaylist}.
   *
   * @param masterPlaylist The master playlist that referenced any parsed media playlists.
   * @return A parser for HLS playlists.
   */
  ParsingLoadable.Parser<HlsPlaylist> createPlaylistParser(HlsMasterPlaylist masterPlaylist);
}
