package minefarts.smarttube.ui.channel;

import android.os.Bundle;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import minefarts.sharedutils.helpers.Helpers;
import minefarts.smarttube.app.presenters.ChannelPresenter;
import minefarts.smarttube.app.presenters.interfaces.VideoGroupPresenter;
import minefarts.smarttube.app.views.ChannelView;
import minefarts.smarttube.prefs.MainUIData;
import minefarts.smarttube.presenter.ChannelHeaderPresenter.ChannelHeaderCallback;
import minefarts.smarttube.ui.browse.video.MultipleRowsFragment;
import minefarts.smarttube.ui.mod.leanback.misc.ProgressBarManager;
import minefarts.googlecommon.common.helpers.YouTubeHelper;

public class ChannelFragment extends MultipleRowsFragment implements ChannelView {
    
    private static final String SELECTED_ITEM_INDEX = "SelectedItemIndex";
    private ChannelPresenter mChannelPresenter;
    private ProgressBarManager mProgressBarManager;
    private boolean mIsFragmentCreated;
    private int mRestoredItemIndex = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(null); // Real restore takes place in the presenter
        
        mRestoredItemIndex = savedInstanceState != null ? savedInstanceState.getInt(SELECTED_ITEM_INDEX, -1) : -1;
        mIsFragmentCreated = true;
        mChannelPresenter = ChannelPresenter.instance(getContext());
        mChannelPresenter.setView(this);

        mProgressBarManager = new ProgressBarManager();
        
        // Channel Seach Bar
        addHeader(
            new ChannelHeaderCallback() {
            
                @Override
                public void onSearchSettingsClicked() {
                    mChannelPresenter.onSearchSettingsClicked();
                }

                @Override
                public boolean onSearchSubmit(String query) {
                    return mChannelPresenter.onSearchSubmit(query);
                }

                @Override
                public String getChannelTitle() {

                    if (mChannelPresenter.getChannel() == null) {
                        return Helpers.startsWith(mChannelPresenter.getChannelId(), "@") ? mChannelPresenter.getChannelId() : null;
                    }

                    String author = mChannelPresenter.getChannel().getAuthor();
                    String title = mChannelPresenter.getChannel().getTitle();
                    String subs = mChannelPresenter.getChannel().subscriberCount;

                    return Helpers.toString(YouTubeHelper.createInfo(Helpers.firstNonNull(author, title), subs));
                }
            
            }
        );

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Not robust. Because tab content often changed after reloading.
        outState.putInt(SELECTED_ITEM_INDEX, getPosition());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Don't move to onCreateView
        mProgressBarManager.setRootView((ViewGroup) getActivity().findViewById(android.R.id.content).getRootView());

        mChannelPresenter.onViewInitialized();

        // Restore state after crash
        setPosition(mRestoredItemIndex);
        mRestoredItemIndex = -1;
    }

    @Override
    protected VideoGroupPresenter getMainPresenter() {
        return ChannelPresenter.instance(getContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mChannelPresenter.onViewDestroyed();
    }

    public void onFinish() {
        mChannelPresenter.onFinish();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!mIsFragmentCreated) {
            mChannelPresenter.onViewResumed();
        }

        mIsFragmentCreated = false;
    }

    @Override
    public void showProgressBar(boolean show) {
        if (show) {
            mProgressBarManager.show();
        } else {
            mProgressBarManager.hide();
        }
    }
}
