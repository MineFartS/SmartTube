package SmartTubeApp.launchers;

import android.os.Bundle;
import com.liskovsoft.sharedutils.data.MediaGroup;
import SmartTubeApp.app.presenters.BrowsePresenter;
import SmartTubeApp.misc.MotherActivity;

public class SubscriptionsLauncherActivity extends MotherActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BrowsePresenter.instance(this).selectSection(MediaGroup.TYPE_SUBSCRIPTIONS);

        finish();
    }
}
