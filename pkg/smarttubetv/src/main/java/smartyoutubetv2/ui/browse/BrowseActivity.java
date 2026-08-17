package smartyoutubetv2.ui.browse;

import android.os.Bundle;
import smartyoutubetv1.prefs.MainUIData;
import smartyoutubetv2.R;
import smartyoutubetv2.ui.common.LeanbackActivity;

public class BrowseActivity extends LeanbackActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);
    }
    
}
