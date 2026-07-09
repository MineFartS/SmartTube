package minefarts.smarttube.launchers;

import android.os.Bundle;
import minefarts.smarttube.utils.service.data.MediaGroup;
import minefarts.smarttube.app.presenters.BrowsePresenter;
import minefarts.smarttube.utils.MotherActivity;

public class HistoryLauncherActivity extends MotherActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BrowsePresenter.instance(this).selectSection(MediaGroup.TYPE_HISTORY);

        finish();
    }
}
