package minefarts.smarttube.ui.browse;

import android.os.Bundle;
import minefarts.smarttube.prefs.MainUIData;
import minefarts.smarttube.R;
import minefarts.smarttube.ui.common.LeanbackActivity;

public class BrowseActivity extends LeanbackActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);
    }
    
}
