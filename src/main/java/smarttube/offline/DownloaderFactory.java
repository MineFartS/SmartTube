package minefarts.smarttube.offline;

/** Creates {@link Downloader Downloaders} for given {@link DownloadRequest DownloadRequests}. */
public interface DownloaderFactory {

  /**
   * Creates a {@link Downloader} to perform the given {@link DownloadRequest}.
   *
   * @param action The action.
   * @return The downloader.
   */
  Downloader createDownloader(DownloadRequest action);
}
