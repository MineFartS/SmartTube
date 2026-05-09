package minefarts.smarttube.launchers;

import android.os.Bundle;
import minefarts.smarttube.app.presenters.SearchPresenter;
import minefarts.smarttube.misc.MotherActivity;

public class SearchLauncherActivity extends MotherActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SearchPresenter.instance(this).startSearch(null);

        finish();
    }
}
