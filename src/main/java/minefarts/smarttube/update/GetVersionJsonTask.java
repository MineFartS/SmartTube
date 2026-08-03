package minefarts.smarttube.update;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import minefarts.smarttube.other.downloadmanager.DownloadManager;
import minefarts.smarttube.other.downloadmanager.DownloadManager.MyRequest;
import minefarts.smarttube.utils.mylogger.Log;
import minefarts.smarttube.utils.helpers.DeviceHelpers;
import minefarts.smarttube.utils.locale.LocaleUtility;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

public class GetVersionJsonTask extends AsyncTask<Uri[], Integer, JSONObject> {

    private final static String TAG = GetVersionJsonTask.class.getSimpleName();
    
    private Exception mLastException;
    private AppVersionChecker mVC;

    public GetVersionJsonTask(AppVersionChecker versionChecker) {
        mVC = versionChecker;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        Log.d(TAG, "update check progress: " + values[0]);
        super.onProgressUpdate(values);
    }

    @Override
    protected JSONObject doInBackground(Uri[]... params) {
        mVC.mInProgress = true;
        publishProgress(0);

        final Uri[] urls = params[0];
        JSONObject jo = null;

        publishProgress(50);

        for (Uri url : urls) {
            jo = getJSON(url);
            if (jo != null)
                break;
        }

        return jo;
    }

    private JSONObject getJSON(Uri urlStr) {
        JSONObject jo = null;
        try {
            DownloadManager manager = new DownloadManager(mVC.mContext);
            MyRequest request = new MyRequest(urlStr);
            long reqId = manager.enqueue(request);

            InputStream content = manager.getStreamForDownloadedFile(reqId);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = content.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            String result = new String(buffer.toByteArray(), StandardCharsets.UTF_8);

            jo = new JSONObject(result);

        } catch (final Exception ex) {
            // IllegalStateException | IllegalArgumentException | JSONException | SocketTimeoutException |
            // SocketException | StreamResetException | SSLException | ProtocolException
            Log.e(TAG, ex.getMessage(), ex);
            mLastException = ex;
        } finally {
            publishProgress(100);
        }

        return jo;
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        if (result != null) {
            try {
                triggerFromJson(result);
            } catch (final JSONException e) {
                String msg = "Error in JSON version file.";
                Log.e(TAG, msg, e);
                mVC.mListener.onCheckError(new IllegalStateException(msg));
            }
        } else {
            mVC.mListener.onCheckError(mLastException != null ? mLastException : new Exception("Unknown error. JSON content is null"));
        }

        mVC.mInProgress = false;
        mVC.mJsonUpdateTask = null;
    }

    @SuppressWarnings("unchecked")
    private void triggerFromJson(JSONObject jo) throws JSONException {

        final ArrayList<String> changelog = new ArrayList<String>();

        // keep a sorted map of versionCode to the version information objects.
        // Most recent is at the top.
        final TreeMap<Integer, JSONObject> versionMap = new TreeMap<Integer, JSONObject>(new Comparator<Integer>() {
            public int compare(Integer object1, Integer object2) {
                return object2.compareTo(object1);
            }
        });

        for (final Iterator<String> i = jo.keys(); i.hasNext(); ) {
            final String versionName = i.next();
            if (versionName.equals("package")) {
                mVC.mVersionInfo = jo.getJSONObject(versionName);
                continue;
            }
            final JSONObject versionInfo = jo.getJSONObject(versionName);
            versionInfo.put("versionName", versionName);

            final int versionCode = versionInfo.getInt("versionCode");
            versionMap.put(versionCode, versionInfo);
        }
        final int latestVersionNumber = versionMap.firstKey();
        final String latestVersionName = versionMap.get(latestVersionNumber).getString("versionName");

        final Uri[] downloadUrls;

        if (mVC.mVersionInfo.has("downloadUrlList_" + DeviceHelpers.getPrimaryAbi())) {
            JSONArray urls = mVC.mVersionInfo.getJSONArray("downloadUrlList_" + DeviceHelpers.getPrimaryAbi());
            downloadUrls = parse(urls);
        } else if (mVC.mVersionInfo.has("downloadUrlList")) {
            JSONArray urls = mVC.mVersionInfo.getJSONArray("downloadUrlList");
            downloadUrls = parse(urls);
        } else {
            String url = mVC.mVersionInfo.getString("downloadUrl");
            downloadUrls = new Uri[]{Uri.parse(url)};
        }

        if (downloadUrls != null) {
            mVC.mListener.processDownloadUrls(downloadUrls);
        }

        if (mVC.mCurrentAppVersion > latestVersionNumber) {
            Log.d(TAG, "We're newer than the latest published version (" + latestVersionName + "). Living in the future...");
            mVC.mListener.onChangelogReceived(true, latestVersionName, latestVersionNumber, null, downloadUrls);
            return;
        }

        if (mVC.mCurrentAppVersion == latestVersionNumber) {
            Log.d(TAG, "We're at the latest version (" + mVC.mCurrentAppVersion + ")");
            mVC.mListener.onChangelogReceived(true, latestVersionName, latestVersionNumber, null, downloadUrls);
            return;
        }

        // construct the changelog. Newest entries are at the top.
        for (final Entry<Integer, JSONObject> version : versionMap.headMap(mVC.mCurrentAppVersion).entrySet()) {
            final JSONObject versionInfo = version.getValue();

            JSONArray versionChangelog = versionInfo.optJSONArray("changelog_" + LocaleUtility.getCurrentLanguage(mVC.mContext));

            if (versionChangelog == null) {
                versionChangelog = versionInfo.optJSONArray("changelog");
            }

            if (versionChangelog != null) {
                final int len = versionChangelog.length();
                for (int i = 0; i < len; i++) {
                    changelog.add(versionChangelog.getString(i));
                }
            }
        }

        mVC.mListener.onChangelogReceived(false, latestVersionName, latestVersionNumber, changelog, downloadUrls);
    }

    private Uri[] parse(JSONArray urls) {
        List<Uri> res = new ArrayList<>();
        for (int i = 0; i < urls.length(); i++) {
            String url = null;
            try {
                url = urls.getString(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (url != null)
                res.add(Uri.parse(url));
        }
        return res.toArray(new Uri[] {});
    }

}