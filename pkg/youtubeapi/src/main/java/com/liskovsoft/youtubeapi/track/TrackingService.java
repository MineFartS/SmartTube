package com.liskovsoft.youtubeapi.track;

import android.util.Pair;

import androidx.annotation.NonNull;

import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.youtubeapi.app.AppService;
import com.liskovsoft.googlecommon.common.helpers.RetrofitHelper;
import com.liskovsoft.youtubeapi.track.models.WatchTimeEmptyResult;

import retrofit2.Call;

public class TrackingService {
    
    private static final int START_THRESHOLD_SEC = 3 * 60;
    
    private static TrackingService sInstance;
    
    private final TrackingApi mTrackingApi;
    private Pair<String, Float> mPosition;

    private TrackingService() {
        mTrackingApi = RetrofitHelper.create(TrackingApi.class);
    }

    public static TrackingService instance() {
        if (sInstance == null) {
            sInstance = new TrackingService();
        }

        return sInstance;
    }

    public void updateWatchTime(
        String videoId, 
        float positionSec, 
        float lengthSec,
        String eventId, 
        String visitorMonitoringData, 
        String ofParam
    ) {

        Call<WatchTimeEmptyResult> wrapper = mTrackingApi.createWatchRecord(
            videoId, 
            lengthSec, 
            (positionSec < START_THRESHOLD_SEC) ? 0 : positionSec,
            getAppService().getClientPlaybackNonce(),
            eventId,
            visitorMonitoringData,
            ofParam
        );

        RetrofitHelper.get(wrapper); // execute

        mPosition = new Pair<>(videoId, positionSec);

    }

    @NonNull
    private static AppService getAppService() {
        return AppService.instance();
    }

}
