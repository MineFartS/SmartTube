package smartyoutubetv2.ui.common;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.KeyEvent;

import com.liskovsoft.sharedutils.mylogger.Log;
import smartyoutubetv1.app.presenters.SearchPresenter;
import smartyoutubetv1.misc.GlobalKeyTranslator;
import smartyoutubetv1.misc.MotherActivity;
import smartyoutubetv1.misc.PlayerKeyTranslator;
import smartyoutubetv1.prefs.GeneralData;
import smartyoutubetv1.prefs.RemoteControlData;
import smartyoutubetv1.utils.Utils;
import smartyoutubetv2.ui.common.keyhandler.DoubleBackManager2;
import smartyoutubetv2.ui.playback.PlaybackActivity;
import smartyoutubetv2.ui.search.tags.SearchTagsActivity;

/**
 * This parent class contains common methods that run in every activity such as search.
 */
public abstract class LeanbackActivity extends MotherActivity {

    private static final String TAG = LeanbackActivity.class.getSimpleName();
    
    private UriBackgroundManager mBackgroundManager;
    private DoubleBackManager2 mDoubleBackManager;
    private GlobalKeyTranslator mGlobalKeyTranslator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
 
        super.onCreate(savedInstanceState);
 
        mBackgroundManager = new UriBackgroundManager(this);
        
        mDoubleBackManager = new DoubleBackManager2(this);
        
        mGlobalKeyTranslator = this instanceof PlaybackActivity ?
            new PlayerKeyTranslator(this) :
            new GlobalKeyTranslator(this);
        
        mGlobalKeyTranslator.apply();
 
    }

    @Override
    public boolean onSearchRequested() {
        SearchPresenter.instance(this).startSearch(null);
        return true;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.d(TAG, event);

        KeyEvent newEvent = mGlobalKeyTranslator.translate(event);
        return super.dispatchKeyEvent(newEvent);
    }

    public UriBackgroundManager getBackgroundManager() {
        return mBackgroundManager;
    }

    @Override
    protected void onStart() {
        super.onStart();

        mBackgroundManager.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // PIP fix: While entering/exiting PIP mode only Pause/Resume is called

        mGlobalKeyTranslator.apply(); // adapt to state changes (like enter/exit from PIP mode)

        getViewManager().addTop(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBackgroundManager.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBackgroundManager.onDestroy();
    }

    @Override // user pressed back key
    public void finish() {
        finishReally();
    }

    @Override
    public void finishReally() {
        // Mandatory line. Fix un-proper view order (especially for playback view).
        getViewManager().startParentView(this);
        super.finishReally();
    }

}
