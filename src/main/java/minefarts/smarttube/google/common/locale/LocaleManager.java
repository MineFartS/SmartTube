package minefarts.smarttube.google.common.locale;

import android.os.Build.VERSION;
import minefarts.smarttube.utils.locale.LocaleUpdater;
import minefarts.smarttube.utils.locale.LocaleUtility;
import minefarts.smarttube.utils.prefs.GlobalPreferences;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class LocaleManager {

    public static LocaleManager sInstance;
    
    private String mLang;
    private String mCountry;
    private int mOffsetFromUtcMinutes;

    public static LocaleManager instance() {
        if (sInstance == null) {
            sInstance = new LocaleManager();
            sInstance.initLang();
        }

        return sInstance;
    }

    public String getLanguage() {
        return mLang;
    }

    public String getCountry() {
        return mCountry;
    }

    public int getUtcOffsetMinutes() {
        return mOffsetFromUtcMinutes;
    }

    protected void initLang() {
        Locale locale = getLocale();

        if (VERSION.SDK_INT >= 21) {
            // Use BCP-47 code for language code to get content with correct language.
            // For example, BCP-47 has zn-CN for simplified Chinese and zh-TW for traditional Chinese.
            mLang = locale.toLanguageTag();
        } else {
            mLang = locale.getLanguage();
        }

        mCountry = locale.getCountry();

        TimeZone tz = TimeZone.getDefault();
        Date now = new Date();
        mOffsetFromUtcMinutes = tz.getOffset(now.getTime()) / 1_000 / 60;
    }

    private Locale getLocale() {
        Locale locale;

        // Proper locale
        if (GlobalPreferences.isInitialized()) {
            locale = LocaleUpdater.getSavedLocale(GlobalPreferences.sInstance.getContext());

            if (locale == null) {
                locale = LocaleUtility.getCurrentLocale(GlobalPreferences.sInstance.getContext());
            }
        } else { // Fallback locale
            locale = Locale.getDefault();
        }

        return locale;
    }
    
}
