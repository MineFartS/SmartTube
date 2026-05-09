package minefarts.smarttube.prefs;

import android.annotation.SuppressLint;
import android.content.Context;

import com.liskovsoft.sharedutils.prefs.SharedPreferencesBase;
import minefarts.smarttube.utils.Utils;

public class HiddenPrefs extends SharedPreferencesBase {
    @SuppressLint("StaticFieldLeak")
    private static HiddenPrefs sInstance;
    public static final String SHARED_PREFERENCES_NAME = HiddenPrefs.class.getName();
    private static final String UNIQUE_ID = "unique_id";

    private HiddenPrefs(Context context) {
        super(context, SHARED_PREFERENCES_NAME);
    }

    public static HiddenPrefs instance(Context context) {
        if (sInstance == null) {
            sInstance = new HiddenPrefs(context.getApplicationContext());
        }

        return sInstance;
    }

    public String getUniqueId() {
        return getString(UNIQUE_ID, null);
    }

    public void setUniqueId(String id) {
        putString(UNIQUE_ID, id);
    }
}
