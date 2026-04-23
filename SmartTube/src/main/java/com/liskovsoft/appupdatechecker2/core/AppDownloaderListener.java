package SmartTubeApp.core;

public interface AppDownloaderListener {
    void onApkDownloaded(String path);
    void onDownloadError(Exception e);
}
