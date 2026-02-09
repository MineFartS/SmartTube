
import com.liskovsoft.smartyoutubetv2.common.prefs.GeneralData;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.BrowsePresenter;
import com.liskovsoft.smartyoutubetv2.common.misc.MediaServiceManager;
import com.liskovsoft.mediaserviceinterfaces.data.MediaGroup;
import com.liskovsoft.youtubeapi.service.internal.MediaServiceData;
import com.liskovsoft.smartyoutubetv2.common.prefs.PlayerData;

import android.content.Context;

import java.util.LinkedList;
import java.lang.Integer;

public class OverrideSettings {

    public void Run(Context context) {

        Update_Notifications(context);

        Hide_Content(context);

        Hide_Sidebar_Tabs(context);

        Highest_Buffer(context);

    }

    private void Update_Notifications(Context context) {

        GeneralData GD = GeneralData.instance(context);

        GD.setOldUpdateNotificationsEnabled(true);
    
    }

    private void Hide_Content(Context context) {

        MediaServiceData MSD = MediaServiceData.instance();

        LinkedList<Integer> content_types = new LinkedList<Integer>();

        content_types.add(MediaServiceData.CONTENT_MIXES);

        content_types.add(MediaServiceData.CONTENT_WATCHED_HOME);

        content_types.add(MediaServiceData.CONTENT_SHORTS_CHANNEL);

        content_types.add(MediaServiceData.CONTENT_SHORTS_HISTORY);

        content_types.add(MediaServiceData.CONTENT_SHORTS_HOME);

        content_types.add(MediaServiceData.CONTENT_SHORTS_SEARCH);

        content_types.add(MediaServiceData.CONTENT_SHORTS_SUBSCRIPTIONS);

        content_types.add(MediaServiceData.CONTENT_SHORTS_TRENDING);

        for (int type : content_types) {

            MSD.setContentHidden(type, true);

        }
    
    }

    private void Hide_Sidebar_Tabs(Context context) {

        BrowsePresenter BP = BrowsePresenter.instance(context);

        LinkedList<Integer> sections = new LinkedList<Integer>();

        sections.add(MediaGroup.TYPE_SHORTS);

        sections.add(MediaGroup.TYPE_TRENDING);

        sections.add(MediaGroup.TYPE_KIDS_HOME);

        sections.add(MediaGroup.TYPE_SPORTS);

        sections.add(MediaGroup.TYPE_LIVE);

        sections.add(MediaGroup.TYPE_GAMING);

        sections.add(MediaGroup.TYPE_NEWS);

        sections.add(MediaGroup.TYPE_MUSIC);

        sections.add(MediaGroup.TYPE_CHANNEL_UPLOADS);

        sections.add(MediaGroup.TYPE_MY_VIDEOS);

        sections.add(MediaGroup.TYPE_PLAYBACK_QUEUE);

        //sections.add(MediaGroup.TYPE_SETTINGS);

        for (Integer section : sections) {

            BP.enableSection(0, false);

        }

    }

    private void Highest_Buffer(Context context) {

        PlayerData PD = PlayerData.instance(context);

        PD.setVideoBufferType(PlayerData.BUFFER_HIGHEST);
    
    }

}