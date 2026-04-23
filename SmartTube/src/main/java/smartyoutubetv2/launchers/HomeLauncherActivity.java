package SmartTubeApp.launchers;

import android.os.Bundle;
import com.liskovsoft.mediaserviceinterfaces.data.MediaGroup;
import smartyoutubetv1.app.presenters.BrowsePresenter;
import smartyoutubetv1.misc.MotherActivity;

public class HomeLauncherActivity extends MotherActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BrowsePresenter.instance(this).selectSection(MediaGroup.TYPE_HOME);

        finish();
    }
}
