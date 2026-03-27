package com.liskovsoft.smartyoutubetv2.common.prefs;

import android.annotation.SuppressLint;
import android.content.Context;

import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.Video;
import com.liskovsoft.smartyoutubetv2.common.prefs.common.DataChangeBase;
import com.liskovsoft.smartyoutubetv2.common.utils.DataStore;

public class RemoteControlData extends DataChangeBase {
    
    @SuppressLint("StaticFieldLeak")
    private static RemoteControlData sInstance;

    private final Context mContext;
    private final DataStore mDataStore;

    private boolean mIsDeviceLinkEnabled; 
    private boolean mIsFinishOnDisconnectEnabled;
    private boolean mIsConnectMessagesEnabled;
    private boolean mIsRemoteHistoryDisabled;
    private Video mLastVideo;
    private boolean mIsConnectedBefore;

    private RemoteControlData(Context context) {

        mContext = context;
        
        mDataStore = new DataStore("device_link_data");
        
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

        /* 0 */ mIsDeviceLinkEnabled = mDataStore.get(0, false);
        /* 1 */ mIsFinishOnDisconnectEnabled = mDataStore.get(1, false);
        /* 2 */ mIsConnectMessagesEnabled = mDataStore.get(2, false);
        /* 3 */ mIsRemoteHistoryDisabled = mDataStore.get(3, false);
        /* 4 */ mLastVideo = mDataStore.get(4, Video::fromString);
        /* 5 */ mIsConnectedBefore = mDataStore.get(5, false);
    
    }

    private void persistState() {
    
        /* 0 */ mDataStore.put(0, mIsDeviceLinkEnabled);
        /* 1 */ mDataStore.put(1, mIsFinishOnDisconnectEnabled); 
        /* 2 */ mDataStore.put(2, mIsConnectMessagesEnabled);
        /* 3 */ mDataStore.put(3, mIsRemoteHistoryDisabled); 
        /* 4 */ mDataStore.put(4, mLastVideo);
        /* 5 */ mDataStore.put(5, mIsConnectedBefore);

        onDataChange();
    
    }
}
