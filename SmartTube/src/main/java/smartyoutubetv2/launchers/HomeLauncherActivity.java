package SmartTubeApp.launchers;

import android.os.Bundle;
import com.liskovsoft.youtubeapi.data.MediaGroup;
import SmartTubeApp.app.presenters.BrowsePresenter;
import SmartTubeApp.misc.MotherActivity;

public class HomeLauncherActivity extends MotherActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BrowsePresenter.instance(this).selectSection(MediaGroup.TYPE_HOME);

        finish();
    }
}
