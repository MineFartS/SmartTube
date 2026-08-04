package minefarts.smarttube.launchers;

import android.os.Bundle;
import com.liskovsoft.mediaserviceinterfaces.data.MediaGroup;
import minefarts.smarttube.app.presenters.BrowsePresenter;
import minefarts.smarttube.utils.MotherActivity;

public class HomeLauncherActivity extends MotherActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BrowsePresenter.instance(this).selectSection(MediaGroup.TYPE_HOME);

        finish();
    }
}
