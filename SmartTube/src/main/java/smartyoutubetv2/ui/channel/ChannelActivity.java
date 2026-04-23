package SmartTubeApp.ui.channel;

import android.os.Bundle;
import SmartTubeApp.R;
import SmartTubeApp.ui.common.LeanbackActivity;

public class ChannelActivity extends LeanbackActivity {
    
    private ChannelFragment mFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_channel);
        mFragment = (ChannelFragment) getSupportFragmentManager().findFragmentById(R.id.channel_fragment);
    }

    @Override
    public void finishReally() {
        super.finishReally();

        mFragment.onFinish();
    }
    
}
