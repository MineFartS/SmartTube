package minefarts.smarttube.launchers;

import android.os.Bundle;
import com.liskovsoft.sharedutils.service.data.MediaGroup;
import minefarts.smarttube.app.presenters.BrowsePresenter;
import minefarts.smarttube.misc.MotherActivity;

public class PlaylistsLauncherActivity extends MotherActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BrowsePresenter.instance(this).selectSection(MediaGroup.TYPE_USER_PLAYLISTS);

        finish();
    }
}
