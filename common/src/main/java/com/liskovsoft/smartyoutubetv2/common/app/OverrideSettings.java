
import com.liskovsoft.smartyoutubetv2.common.prefs.GeneralData;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.BrowsePresenter;
import com.liskovsoft.mediaserviceinterfaces.data.MediaGroup;
import com.liskovsoft.youtubeapi.service.internal.MediaServiceData;
import com.liskovsoft.smartyoutubetv2.common.prefs.PlayerData;

import android.content.Context;

public class OverrideSettings {

    private MediaServiceData mediaServiceData;
    private BrowsePresenter browsePresenter;
    private GeneralData generalData;
    private PlayerData playerData;

    public void OverrideSettings(Context context) {

        this.mediaServiceData = MediaServiceData.instance(context);

        this.browsePresenter = BrowsePresenter.instance(context);

        this.generalData = GeneralData.instance(context);

        this.playerData = PlayerData.instance(context);

        Update_Notifications();

        Hide_Content();

        Hide_Sidebar_Tabs();

        Highest_Buffer();

    }

    private void Update_Notifications() {
        generalData.setOldUpdateNotificationsEnabled(true);
    }

    private void Hide_Content() {

        mMediaServiceData.setContentHidden(
            MediaServiceData.CONTENT_MIXES, 
            true
        );
        
        mMediaServiceData.setContentHidden(
            MediaServiceData.CONTENT_WATCHED_HOME, 
            true
        );
        
        mMediaServiceData.setContentHidden(
            MediaServiceData.CONTENT_SHORTS_CHANNEL, 
            true
        );

        mMediaServiceData.setContentHidden(
            MediaServiceData.CONTENT_SHORTS_HISTORY, 
            true
        );

        mMediaServiceData.setContentHidden(
            MediaServiceData.CONTENT_SHORTS_HOME, 
            true
        );

        mMediaServiceData.setContentHidden(
            MediaServiceData.CONTENT_SHORTS_SEARCH, 
            true
        );

        mMediaServiceData.setContentHidden(
            MediaServiceData.CONTENT_SHORTS_SUBSCRIPTIONS, 
            true
        );

        mMediaServiceData.setContentHidden(
            MediaServiceData.CONTENT_SHORTS_TRENDING, 
            true
        );
    
    }

    private void Hide_Sidebar_Tabs() {

        browsePresenter.enableSection(
            MediaGroup.TYPE_SHORTS,
            false
        );

        browsePresenter.enableSection(
            MediaGroup.TYPE_TRENDING,
            false
        );

        browsePresenter.enableSection(
            MediaGroup.TYPE_KIDS_HOME,
            false
        );

        browsePresenter.enableSection(
            MediaGroup.TYPE_SPORTS,
            false
        );

        browsePresenter.enableSection(
            MediaGroup.TYPE_LIVE,
            false
        );

        browsePresenter.enableSection(
            MediaGroup.TYPE_GAMING,
            false
        );

        browsePresenter.enableSection(
            MediaGroup.TYPE_NEWS,
            false
        );

        browsePresenter.enableSection(
            MediaGroup.TYPE_MUSIC,
            false
        );

        browsePresenter.enableSection(
            MediaGroup.TYPE_CHANNEL_UPLOADS,
            false
        );

        browsePresenter.enableSection(
            MediaGroup.TYPE_MY_VIDEOS,
            false
        );

        browsePresenter.enableSection(
            MediaGroup.TYPE_PLAYBACK_QUEUE,
            false
        );

    }

    private void Highest_Buffer() {
        playerData.setVideoBufferType(PlayerData.BUFFER_HIGHEST);
    }

}