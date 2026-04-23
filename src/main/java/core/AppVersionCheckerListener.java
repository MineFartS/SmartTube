package SmartTubeApp.core;

import android.net.Uri;

import java.util.ArrayList;

public interface AppVersionCheckerListener {

	void onChangelogReceived(
        boolean isLatestVersion, 
        String latestVersionName, 
        int latestVersionNumber, 
        ArrayList<String> changelog, 
        Uri[] downloadUris
    );
	
    void onCheckError(Exception e);
	
    void processDownloadUrls(Uri[] downloadUrls);

}