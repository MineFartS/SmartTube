package SmartTubeApp.launchers;

import android.os.Bundle;
import SmartTubeApp.app.presenters.SearchPresenter;
import SmartTubeApp.misc.MotherActivity;

public class SearchLauncherActivity extends MotherActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SearchPresenter.instance(this).startSearch(null);

        finish();
    }
}
