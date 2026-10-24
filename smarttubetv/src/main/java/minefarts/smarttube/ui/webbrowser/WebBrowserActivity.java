package minefarts.smarttube.ui.webbrowser;

import android.os.Bundle;

import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import minefarts.smarttube.R;
import minefarts.smarttube.ui.common.LeanbackActivity;

public class WebBrowserActivity extends LeanbackActivity {
    private static final String TAG = WebBrowserActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Caused by android.app.Fragment$InstantiationException
        // Trying to instantiate a class minefarts.smarttube.ui.webbrowser.WebBrowserFragment that is not a Fragment
        // WebBrowserFragment is not a Fragment: ClassCastException or Fragment.InstantiationException
        try {
            setContentView(R.layout.fragment_webbrowser);
        } catch (Exception e) {
            e.printStackTrace();
            MessageHelpers.showMessage(this, e.getMessage());
            finish();
        }
    }
}
