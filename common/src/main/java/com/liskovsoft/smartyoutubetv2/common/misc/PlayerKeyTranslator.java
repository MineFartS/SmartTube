package com.liskovsoft.smartyoutubetv2.common.misc;

import android.content.Context;
import android.view.KeyEvent;
import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import com.liskovsoft.smartyoutubetv2.common.R;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.manager.PlayerUI;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.PlaybackPresenter;
import com.liskovsoft.smartyoutubetv2.common.prefs.GeneralData;
import com.liskovsoft.smartyoutubetv2.common.prefs.PlayerData;
import com.liskovsoft.smartyoutubetv2.common.prefs.PlayerTweaksData;
import com.liskovsoft.smartyoutubetv2.common.utils.Utils;

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

    private void speedUp(boolean up) {
        PlayerTweaksData data = PlayerTweaksData.instance(mContext);
        float[] speedSteps = data.isLongSpeedListEnabled() ? Utils.SPEED_LIST_LONG :
                data.isExtraLongSpeedListEnabled() ? Utils.SPEED_LIST_EXTRA_LONG : Utils.SPEED_LIST_SHORT;

        PlaybackPresenter playbackPresenter = getPlaybackPresenter();

        if (playbackPresenter != null && playbackPresenter.getView() != null) {
            float currentSpeed = playbackPresenter.getView().getSpeed();
            int currentIndex = Arrays.binarySearch(speedSteps, currentSpeed);

            if (currentIndex < 0) {
                currentIndex = Arrays.binarySearch(speedSteps, 1.0f);
            }

            int newIndex = up ? currentIndex + 1 : currentIndex - 1;

            float speed = newIndex >= 0 && newIndex < speedSteps.length ? speedSteps[newIndex] : speedSteps[currentIndex];

            PlayerData.instance(mContext).setSpeed(speed);
            playbackPresenter.getView().setSpeed(speed);
            MessageHelpers.showMessage(mContext, String.format("%sx", speed));
        }
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

    private Context getContext() {
        return mContext;
    }
}
