package smartyoutubetv2.ui.channeluploads;

import android.os.Bundle;
import smartyoutubetv2.R;
import smartyoutubetv2.ui.common.LeanbackActivity;

public class ChannelUploadsActivity extends LeanbackActivity {
    
    private ChannelUploadsFragment mFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_channel_uploads);
        mFragment = (ChannelUploadsFragment) getSupportFragmentManager().findFragmentById(R.id.channel_uploads_fragment);
    }

    @Override
    public void finishReally() {
        super.finishReally();

        mFragment.onFinish();
    }
    
}
