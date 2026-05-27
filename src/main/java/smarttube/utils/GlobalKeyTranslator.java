package minefarts.smarttube.utils;

import android.content.Context;
import android.view.KeyEvent;
import minefarts.smarttube.app.presenters.PlaybackPresenter;
import minefarts.smarttube.app.presenters.SearchPresenter;
import minefarts.smarttube.prefs.GeneralData;

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

        globalKeyMapping.put(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_DPAD_CENTER);

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

    private SearchPresenter getSearchPresenter() {
        return SearchPresenter.instance(mContext);
    }
}
