package minefarts.smarttube.update;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;

import minefarts.smarttube.utils.mylogger.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class AppVersionChecker {

    private final static String TAG = AppVersionChecker.class.getSimpleName();
    
    public int mCurrentAppVersion;
    public JSONObject mVersionInfo;
    public final Context mContext;
    public boolean mInProgress;
    public final AppVersionCheckerListener mListener;
    public GetVersionJsonTask mJsonUpdateTask;

    public AppVersionChecker(Context context, AppVersionCheckerListener listener) {
        
        mContext = context;
        mListener = listener;

        try {
            mCurrentAppVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (final NameNotFoundException e) {
            String msg = "Cannot get version for self!";
            Log.e(TAG, msg);
            mListener.onCheckError(new IllegalStateException(msg));
        }
    }

    /**
     * Checks for updates regardless of when the last check happened or if checking for updates is enabled.<br/>
     * URL pointing to a JSON file with the update list <br/>
     * @param versionListUrls url array, tests url by access, first worked is used
     */
    public void checkForUpdates(Uri[] versionListUrls) {
        Log.d(TAG, "Checking for updates...");

        if (mInProgress) {
            Log.i(TAG, "Another update is running. Cancelling...");
            return;
        }

        if (versionListUrls == null || versionListUrls.length == 0) {
            Log.w(TAG, "Supplied url update list is null or empty");
        } else if (mJsonUpdateTask == null) {
            mListener.processDownloadUrls(versionListUrls);

            mJsonUpdateTask = new GetVersionJsonTask(this);
            mJsonUpdateTask.execute(versionListUrls);
        } else {
            String msg = "checkForUpdates() called while already checking for updates. Ignoring...";
            Log.e(TAG, msg);
            mListener.onCheckError(new IllegalStateException(msg));
        }
    }

    private class VersionCheckException extends Exception {
        private static final long serialVersionUID = 397593559982487816L;

        public VersionCheckException(String msg) {
            super(msg);
        }
    }

    /**
     * Send off an intent to start the download of the app.
     */
    public void startUpgrade() {
        try {
            final Uri downloadUri = Uri.parse(mVersionInfo.getString("downloadUrl"));
            mContext.startActivity(new Intent(Intent.ACTION_VIEW, downloadUri));
        } catch (final JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean isInProgress() {
        return mInProgress;
    }

}
