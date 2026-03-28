package smartyoutubetv2.ui.adddevice;

import android.os.Bundle;
import androidx.leanback.app.GuidedStepSupportFragment;
import smartyoutubetv2.ui.common.LeanbackActivity;

public class AddDeviceActivity extends LeanbackActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (null == savedInstanceState) {
            GuidedStepSupportFragment.addAsRoot(this, new AddDeviceFragment(), android.R.id.content);
        }
    }

    @Override
    public void finish() {
        super.finish();

        finishReally();
    }
}
