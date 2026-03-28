package smartyoutubetv1.prefs;

import android.annotation.SuppressLint;
import android.content.Context;
import com.liskovsoft.sharedutils.helpers.Helpers;
import smartyoutubetv1.app.models.data.Video;
import smartyoutubetv1.prefs.common.DataChangeBase;

public class RemoteControlData extends DataChangeBase {
    private static final String DEVICE_LINK_DATA = "device_link_data";
    @SuppressLint("StaticFieldLeak")
    private static RemoteControlData sInstance;
    private final Context mContext;
    private final AppPrefs mAppPrefs;
    private boolean mIsDeviceLinkEnabled;
    
    private boolean mIsFinishOnDisconnectEnabled;
    private boolean mIsConnectMessagesEnabled;
    private boolean mIsRemoteHistoryDisabled;
    private Video mLastVideo;
    private boolean mIsConnectedBefore;

    private RemoteControlData(Context context) {
        mContext = context;
        mAppPrefs = AppPrefs.instance(mContext);
        restoreState();
    }

    public static RemoteControlData instance(Context context) {
        if (sInstance == null) {
            sInstance = new RemoteControlData(context.getApplicationContext());
        }

        return sInstance;
    }

    public void enableDeviceLink(boolean select) {
        mIsDeviceLinkEnabled = select;
        persistState();
    }

    public boolean isDeviceLinkEnabled() {
        // Merge device link and background service (saves memory)
        return mIsDeviceLinkEnabled;
    }

    public void enableFinishOnDisconnect(boolean enable) {
        mIsFinishOnDisconnectEnabled = enable;
        persistState();
    }

    public boolean isFinishOnDisconnectEnabled() {
        return mIsFinishOnDisconnectEnabled;
    }

    public void enableConnectMessages(boolean enable) {
        mIsConnectMessagesEnabled = enable;
        persistState();
    }

    public boolean isConnectMessagesEnabled() {
        return mIsConnectMessagesEnabled;
    }

    public void disableRemoteHistory(boolean disable) {
        mIsRemoteHistoryDisabled = disable;
        persistState();
    }

    public boolean isRemoteHistoryDisabled() {
        return mIsRemoteHistoryDisabled;
    }

    public Video getLastVideo() {
        return mLastVideo;
    }

    public void setLastVideo(Video video) {
        mLastVideo = video;
        persistState();
    }

    public void setConnectedBefore(boolean connected) {
        mIsConnectedBefore = connected;
        persistState();
    }

    public boolean isConnectedBefore() {
        return mIsConnectedBefore;
    }

    private void restoreState() {

        String data = mAppPrefs.getData(DEVICE_LINK_DATA);
        String[] split = Helpers.splitData(data);

        /* 0 */ mIsDeviceLinkEnabled = Helpers.parseBoolean(split, 0, false);
        /* 1 */ mIsFinishOnDisconnectEnabled = Helpers.parseBoolean(split, 1, false);
        /* 2 */ mIsConnectMessagesEnabled = Helpers.parseBoolean(split, 2, false);
        /* 3 */ mIsRemoteHistoryDisabled = Helpers.parseBoolean(split, 3, false);
        /* 4 */ mLastVideo = Helpers.parseItem(split, 4, Video::fromString);
        /* 5 */ mIsConnectedBefore = Helpers.parseBoolean(split, 5, false);
    
    }

    public void persistState() {
    
        mAppPrefs.setData(
            DEVICE_LINK_DATA, 
            Helpers.mergeData(
            /* 0 */ mIsDeviceLinkEnabled, 
            /* 1 */ mIsFinishOnDisconnectEnabled, 
            /* 2 */ mIsConnectMessagesEnabled,
            /* 3 */ mIsRemoteHistoryDisabled, 
            /* 4 */ mLastVideo, 
            /* 5 */ mIsConnectedBefore
            )
        );

        onDataChange();
    
    }
}
