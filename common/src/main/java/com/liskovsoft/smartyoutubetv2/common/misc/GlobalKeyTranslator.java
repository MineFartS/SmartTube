package com.liskovsoft.smartyoutubetv2.common.misc;

import android.content.Context;
import android.view.KeyEvent;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.PlaybackPresenter;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.SearchPresenter;
import com.liskovsoft.smartyoutubetv2.common.prefs.GeneralData;

import java.util.Map;

public class GlobalKeyTranslator extends KeyTranslator {
    private final Context mContext;

    public GlobalKeyTranslator(Context context) {
        mContext = context;
    }

    @Override
    protected void initKeyMapping() {
        Map<Integer, Integer> globalKeyMapping = getKeyMapping();

        // Fix rare situations with some remotes. E.g. Shield.
        // NOTE: 'sendKey' won't work with Android 13
        globalKeyMapping.put(KeyEvent.KEYCODE_BUTTON_B, KeyEvent.KEYCODE_BACK);
        // Fix for the unknown usb remote controller: https://smartyoutubetv.github.io/#comment-3742343397
        globalKeyMapping.put(KeyEvent.KEYCODE_ESCAPE, KeyEvent.KEYCODE_BACK);

        // May help on buggy firmwares (where Enter key is used as OK)
        if (!getPlaybackPresenter().isInPipMode()) {
            globalKeyMapping.put(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_DPAD_CENTER);
        } else {
            globalKeyMapping.remove(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
        }
    }

    @Override
    protected void initActionMapping() {
        addSearchAction();
    }

    private void addSearchAction() {
        Runnable searchAction = () -> getSearchPresenter().startSearch(null);

        Map<Integer, Runnable> actionMapping = getActionMapping();

        actionMapping.put(KeyEvent.KEYCODE_AT, searchAction);

    }

    private PlaybackPresenter getPlaybackPresenter() {
        return PlaybackPresenter.instance(mContext);
    }

    private SearchPresenter getSearchPresenter() {
        return SearchPresenter.instance(mContext);
    }
}
