package SmartTubeApp.launchers;

import android.os.Bundle;
import smartyoutubetv1.app.presenters.SearchPresenter;
import smartyoutubetv1.misc.MotherActivity;

public class SearchLauncherActivity extends MotherActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SearchPresenter.instance(this).startSearch(null);

        finish();
    }
}
