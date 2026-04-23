package SmartTubeApp.launchers;

import android.os.Bundle;
import com.liskovsoft.youtubeapi.data.MediaGroup;
import smartyoutubetv1.app.presenters.BrowsePresenter;
import smartyoutubetv1.misc.MotherActivity;

public class SubscriptionsLauncherActivity extends MotherActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BrowsePresenter.instance(this).selectSection(MediaGroup.TYPE_SUBSCRIPTIONS);

        finish();
    }
}
