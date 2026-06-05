package minefarts.smarttube.core;

public interface AppDownloaderListener {
    void onApkDownloaded(String path);
    void onDownloadError(Exception e);
}
