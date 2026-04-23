package SmartTubeApp.misc;

import android.content.Context;
import android.view.KeyEvent;
import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import SmartTubeApp.R;
import SmartTubeApp.app.models.playback.manager.PlayerUI;
import SmartTubeApp.app.presenters.PlaybackPresenter;
import SmartTubeApp.prefs.GeneralData;
import SmartTubeApp.prefs.PlayerData;
import SmartTubeApp.prefs.PlayerTweaksData;
import SmartTubeApp.utils.Utils;

import java.util.Arrays;
import java.util.Map;

public class PlayerKeyTranslator extends GlobalKeyTranslator {
    private final GeneralData mGeneralData;
    private final Context mContext;

    public PlayerKeyTranslator(Context context) {
        super(context);
        mContext = context;
        mGeneralData = GeneralData.instance(context);
    }

    @Override
    protected void initKeyMapping() {
        super.initKeyMapping();

        Map<Integer, Integer> globalKeyMapping = getKeyMapping();

        // Reset global mapping to default
        globalKeyMapping.remove(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
        globalKeyMapping.remove(KeyEvent.KEYCODE_MEDIA_REWIND);
        globalKeyMapping.remove(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD);

        globalKeyMapping.put(KeyEvent.KEYCODE_DEL, KeyEvent.KEYCODE_0); // reset position of the video (if enabled number key handling in the settings)

    }

    private void volumeUp(boolean up) {
        PlaybackPresenter playbackPresenter = getPlaybackPresenter();

        if (playbackPresenter != null && playbackPresenter.getView() != null) {
            Utils.volumeUp(mContext, playbackPresenter.getView(), up);
        }
    }

    private PlaybackPresenter getPlaybackPresenter() {
        return PlaybackPresenter.instance(mContext);
    }

}
