package minefarts.smarttube;

import android.content.Context;

import androidx.annotation.Nullable;

public class ContextManager {

    private static Context mContext = null;

    public static Context set(@Nullable Context context) {
        
        if (mContext == null)
            mContext = context;

        return context != null ? context : mContext;
    }

    @Nullable
    public static Context get() {
        return mContext;
    }

}