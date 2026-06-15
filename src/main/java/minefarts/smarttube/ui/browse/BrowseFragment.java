package minefarts.smarttube.ui.browse;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.core.content.ContextCompat;
import minefarts.smarttube.fragment.app.Fragment;
import minefarts.smarttube.fragment.app.FragmentTransaction;
import minefarts.smarttube.leanback.app.BrowseSupportFragment;
import minefarts.smarttube.leanback.app.HeadersSupportFragment;
import minefarts.smarttube.leanback.widget.ArrayObjectAdapter;
import minefarts.smarttube.leanback.widget.HeaderItem;
import minefarts.smarttube.leanback.widget.ListRowPresenter;
import minefarts.smarttube.leanback.widget.PageRow;
import minefarts.smarttube.leanback.widget.Presenter;
import minefarts.smarttube.leanback.widget.PresenterSelector;
import minefarts.smarttube.leanback.widget.TitleHelper;
import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.app.models.data.BrowseSection;
import minefarts.smarttube.app.models.data.SettingsGroup;
import minefarts.smarttube.app.models.data.Video;
import minefarts.smarttube.app.models.data.VideoGroup;
import minefarts.smarttube.app.models.errors.ErrorFragmentData;
import minefarts.smarttube.app.models.playback.service.VideoStateService;
import minefarts.smarttube.app.models.playback.service.State;
import minefarts.smarttube.app.presenters.BrowsePresenter;
import minefarts.smarttube.app.presenters.PlaybackPresenter;
import minefarts.smarttube.app.presenters.SearchPresenter;
import minefarts.smarttube.app.presenters.SplashPresenter;
import minefarts.smarttube.app.views.BrowseView;
import minefarts.smarttube.app.views.ViewManager;
import minefarts.smarttube.utils.Utils;
import minefarts.smarttube.R;
import minefarts.smarttube.presenter.IconHeaderItemPresenter;
import minefarts.smarttube.ui.browse.dialog.ErrorDialogFragment;
import minefarts.smarttube.ui.mod.leanback.headers.ExtendedHeadersSupportFragment;
import minefarts.smarttube.ui.mod.leanback.misc.ProgressBarManager;
import minefarts.smarttube.leanback.widget.TitleView;

import java.util.HashMap;
import java.util.Map;

/*
 * Main class to show BrowseFragment with header and rows of videos
 */
public class BrowseFragment extends BrowseSupportFragment implements BrowseView {

    private static final String SELECTED_HEADER_INDEX = "SelectedHeaderIndex";
    private static final String SELECTED_VIDEO = "SelectedVideo";
    private static final String IS_PLAYER_IN_FOREGROUND = "IsPlayerInForeground";
    private ArrayObjectAdapter mSectionRowAdapter;
    private BrowsePresenter mBrowsePresenter;
    private Map<Integer, BrowseSection> mSections;
    private BrowseSectionFragmentFactory mSectionFragmentFactory;
    private Handler mHandler;
    private ProgressBarManager mProgressBarManager;
    private TitleView mTitleView;
    private boolean mIsFragmentCreated;
    private int mSelectedHeaderIndex = -1;
    private Video mSelectedVideo;
    private boolean mIsPlayerInForeground;
    private boolean mFocusOnContent;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);

        if (savedInstanceState != null) {
            mSelectedHeaderIndex = savedInstanceState.getInt(SELECTED_HEADER_INDEX, -1);
            mSelectedVideo = Video.fromString(savedInstanceState.getString(SELECTED_VIDEO));
            mIsPlayerInForeground = savedInstanceState.getBoolean(IS_PLAYER_IN_FOREGROUND, false);
        }
        mIsFragmentCreated = true;

        mSections = new HashMap<>();
        mHandler = new Handler();
        mBrowsePresenter = BrowsePresenter.instance(getContext());
        mBrowsePresenter.setView(this);
        mProgressBarManager = new ProgressBarManager();

        setupAdapter();
        setupFragmentFactory();
        setupUi();

        enableMainFragmentScaling(false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Store position in case activity is crashed
        outState.putInt(SELECTED_HEADER_INDEX, getSelectedPosition());
        if (mBrowsePresenter.getCurrentVideo() != null) {
            outState.putString(SELECTED_VIDEO, mBrowsePresenter.getCurrentVideo().toString());
            outState.putBoolean(IS_PLAYER_IN_FOREGROUND,
                    ViewManager.instance(getContext()).isPlayerInForeground());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);

        mProgressBarManager.setRootView((ViewGroup) root);
        mTitleView = root.findViewById(R.id.browse_title_group);

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupEventListeners();

        prepareEntranceTransition();

        mBrowsePresenter.onViewInitialized();

        if (mSelectedHeaderIndex != -1) {
            // Restore state after crash
            selectSection(mSelectedHeaderIndex, true);
            mSelectedHeaderIndex = -1;

            // Restore state after crash
            selectSectionItem(mSelectedVideo);
            if (PlaybackPresenter.instance(getContext()).getPlayer() == null
                    && mIsPlayerInForeground) {
                VideoStateService stateService = VideoStateService.instance(getContext());
                boolean isVideoStateSynced = mSelectedVideo == null
                        || stateService.getByVideoId(mSelectedVideo.videoId) != null;
                State lastState = stateService.getLastState();
                PlaybackPresenter.instance(getContext()).openVideo(
                        lastState != null && isVideoStateSynced ? lastState.video : mSelectedVideo);
            }
            mSelectedVideo = null;
        }
    }

    @Override
    public HeadersSupportFragment onCreateHeadersSupportFragment() {
        return new ExtendedHeadersSupportFragment();
    }

    private void setupEventListeners() {
        getHeadersSupportFragment().setOnHeaderClickedListener((viewHolder, row) -> {
            long headerId = row.getHeaderItem().getId();
            int newPosition = indexOf(headerId);

            if (getHeadersSupportFragment().getSelectedPosition() != newPosition) {
                // touch screen support
                getHeadersSupportFragment().setSelectedPosition(newPosition);
            } else {
                // update section when clicked or pressed
                mBrowsePresenter.onSectionFocused((int) headerId);
                startHeadersTransitionSafe(false);
            }
        });

        ((ExtendedHeadersSupportFragment) getHeadersSupportFragment())
                .setOnHeaderLongPressedListener((viewHolder, row) -> {
                    long headerId = row.getHeaderItem().getId();

                    mBrowsePresenter.onSectionLongPressed((int) headerId);
                });

        setOnSearchClickedListener(
                view -> SearchPresenter.instance(getActivity()).startSearch(null));
    }

    private void setupFragmentFactory() {
        mSectionFragmentFactory = new BrowseSectionFragmentFactory((row) -> {
            focusOnContentIfNeeded();
            mBrowsePresenter.onSectionFocused(getSelectedHeaderId());
        });

        getMainFragmentRegistry().registerFragment(PageRow.class, mSectionFragmentFactory);
    }

    private int indexOf(long headerId) {
        for (int i = 0; i < mSectionRowAdapter.size(); i++) {
            PageRow row = (PageRow) mSectionRowAdapter.get(i);
            HeaderItem header = row.getHeaderItem();
            if (header.getId() == headerId) {
                return i;
            }
        }

        return 0;
    }

    private void setupAdapter() {
        // Map category results from the database to ListRow objects.
        // This Adapter is used to render the MainFragment sidebar labels.
        mSectionRowAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        setAdapter(mSectionRowAdapter);
    }

    private void setupUi() {
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);

        int brandColorRes = Helpers.getThemeAttr(getActivity(), R.attr.brandColor);
        int brandAccentColorRes = Helpers.getThemeAttr(getActivity(), R.attr.brandAccentColor);
        int appLogoRes = Helpers.getThemeAttr(getActivity(), R.attr.appLogo);

        Drawable bridgeIcon = Utils.getDrawable(getActivity(),
                SplashPresenter.instance(getActivity()).getBridgePackageName(), "app_icon");

        // Top right corner logo
        setBadgeDrawable(bridgeIcon != null ? bridgeIcon
                : appLogoRes > 0 ? ContextCompat.getDrawable(getActivity(), appLogoRes) : null);

        // This title replaces badge in case one is null
        // setTitle(getString(R.string.browse_title));

        // Set fastLane (or headers) background color
        setBrandColor(ContextCompat.getColor(getActivity(), brandColorRes));

        // Set search icon color.
        setSearchAffordanceColor(ContextCompat.getColor(getActivity(), brandAccentColorRes));

        setHeaderPresenterSelector(new PresenterSelector() {
            private final Map<Integer, Presenter> mPresenterMap = new HashMap<>();

            @Override
            public Presenter getPresenter(Object o) {
                Presenter presenter = mPresenterMap.get(o.hashCode());

                if (presenter == null) {
                    presenter = new IconHeaderItemPresenter(getHeaderResId(o), getIconUrl(o));
                    mPresenterMap.put(o.hashCode(), presenter);
                }

                return presenter;
            }

            private int getHeaderResId(Object o) {
                if (o instanceof PageRow) {
                    return ((SectionHeaderItem) ((PageRow) o).getHeaderItem()).getResId();
                }

                return -1;
            }

            private String getIconUrl(Object o) {
                if (o instanceof PageRow) {
                    return ((SectionHeaderItem) ((PageRow) o).getHeaderItem()).getIconUrl();
                }

                return null;
            }
        });
    }

    private int getSelectedHeaderId() {
        if (getSelectedPosition() >= mSectionRowAdapter.size()) {
            return -1;
        }

        return (int) ((PageRow) mSectionRowAdapter.get(getSelectedPosition())).getHeaderItem()
                .getId();
    }

    public void updateErrorIfEmpty(ErrorFragmentData data) {
        mHandler.postDelayed(() -> showErrorIfEmpty(data), 500); // need delay because header may be
                                                                 // not updated
    }

    @Override
    public void showError(ErrorFragmentData data) {
        replaceMainFragment(new ErrorDialogFragment(data));
    }

    private void showErrorIfEmpty(ErrorFragmentData data) {
        if (isEmpty()) {
            replaceMainFragment(new ErrorDialogFragment(data));
        }
    }

    private void replaceMainFragment(Fragment fragment) {
        // Object mainFragment = Helpers.getField(this,"mMainFragment");
        Fragment mainFragment = getMainFragment();

        if (mainFragment != null && fragment != null && mainFragment != fragment) {
            Helpers.setField(this, "mMainFragment", fragment);

            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            ft.replace(R.id.scale_frame, fragment);
            // mFocusOnContent = !isShowingHeaders(); // Fix focus lost when error fragment shown
            // and sidebar is hidden
            mFocusOnContent = hasFocus(); // Maintain focus
            ft.runOnCommit(this::focusOnContentIfNeeded);
            ft.commitAllowingStateLoss(); // FIX: "Can not perform this action after
                                          // onSaveInstanceState"
        }
    }

    @Override
    public void addSection(int index, BrowseSection section) {
        if (section == null) return;

        if (mSections.get(section.getId()) != null
                && (index == -1 || indexOf(section.getId()) == index)) return;

        removeSection(section);

        mSections.put(section.getId(), section);
        createHeader(index, section);
    }

    @Override
    public void removeSection(BrowseSection section) {
        if (section == null) return;

        mSections.remove(section.getId());
        removeHeader(section);
    }

    @Override
    public void removeAllSections() {
        mSections.clear();
        mSectionRowAdapter.clear();
    }

    @Override
    public void updateSection(VideoGroup group) {
        restoreMainFragment();

        mSectionFragmentFactory.updateCurrentFragment(group);

        fixInvisibleSearchOrb();
    }

    @Override
    public void updateSection(SettingsGroup group) {
        restoreMainFragment();

        mSectionFragmentFactory.updateCurrentFragment(group);
    }

    @Override
    public void selectSection(int index, boolean focusOnContent) {
        if (index >= 0 && mSectionRowAdapter.size() > 0) {
            mFocusOnContent = focusOnContent; // focus after header transition

            // Fix refresh current section
            if (getSelectedPosition() == index) {
                // update section manually
                // headers transition event not fired on the same index
                focusOnContentIfNeeded();
                mBrowsePresenter.onSectionFocused(getSelectedHeaderId());
            }

            // Need select again if current header is removed previously (can't check for it right
            // now)
            // Fallback to the last section if index above size
            setSelectedPosition(
                    index < mSectionRowAdapter.size() ? index : mSectionRowAdapter.size() - 1,
                    false);
        }
    }

    @Override
    public void focusOnContent() {
        startHeadersTransitionSafe(false);
        if (getMainFragment() != null && getMainFragment().getView() != null) {
            getMainFragment().getView().requestFocus();
        }
    }

    /**
     * Usually called after header transition or fragment transaction
     */
    private void focusOnContentIfNeeded() {
        if (mFocusOnContent) {
            focusOnContent();
            mFocusOnContent = false;
        }
    }

    private boolean hasFocus() {
        if (getMainFragment() == null || getMainFragment().getView() == null) {
            return false;
        }

        return getMainFragment().getView().hasFocus();
    }

    @Override
    public void selectSectionItem(int index) {
        if (index >= 0) {
            mSectionFragmentFactory.setCurrentFragmentItemIndex(index);
        }
    }

    @Override
    public void selectSectionItem(Video item) {
        if (item != null) {
            mSectionFragmentFactory.selectCurrentFragmentItem(item);
        }
    }

    /**
     * Fix: IllegalStateException: "Can not perform this action after onSaveInstanceState"
     */
    private void startHeadersTransitionSafe(boolean withHeaders) {
        // Fix: IllegalStateException: "Can not perform this action after onSaveInstanceState"
        if (!Utils.checkActivity(getActivity())) return;

        try {
            startHeadersTransition(withHeaders);
        } catch (IllegalStateException e) {
            // NOP
        }
    }

    /**
     * Restore after the error fragment
     */
    private void restoreMainFragment() {
        Fragment currentFragment = mSectionFragmentFactory.getCurrentFragment();

        if (currentFragment != null) {
            replaceMainFragment(currentFragment);
        }
    }

    private void createHeader(int index, BrowseSection header) {
        HeaderItem headerItem = new SectionHeaderItem(header);

        PageRow pageRow = new PageRow(headerItem);
        if (index == -1 || mSectionRowAdapter.size() < index) {
            mSectionRowAdapter.add(pageRow); // add to the end
        } else {
            mSectionRowAdapter.add(index, pageRow);
        }
    }

    private void removeHeader(BrowseSection header) {
        Object foundHeader = null;

        for (Object item : mSectionRowAdapter.unmodifiableList()) {
            if (((PageRow) item).getHeaderItem().getId() == header.getId()) {
                foundHeader = item;
                break;
            }
        }

        if (foundHeader != null) {
            mSectionRowAdapter.remove(foundHeader);
        }
    }

    @Override
    public void clearSection(BrowseSection section) {
        mSectionFragmentFactory.clearCurrentFragment();
    }

    @Override
    public void onDestroyView() {
        mSectionFragmentFactory.cleanup();

        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBrowsePresenter.onViewDestroyed();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (!mIsFragmentCreated) {
            mBrowsePresenter.onViewPaused();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!mIsFragmentCreated) {
            mBrowsePresenter.onViewResumed();
        }

        mIsFragmentCreated = false;
    }

    /**
     * Fix suddenly invisible search orb<br/>
     * Could happen on topmost category when the page partially scrolled<br/>
     * More info: {@link TitleHelper}
     */
    private void fixInvisibleSearchOrb() {
        if (isShowingTitle() && getTitleView() != null
                && getTitleView().getVisibility() != View.VISIBLE) {
            getTitleView().setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showProgressBar(boolean show) {
        Runnable callback;

        if (show) {
            callback = mProgressBarManager::show;
        } else {
            callback = mProgressBarManager::hide;
        }

        // Essential. Need to run on the main thread.
        new Handler(Looper.getMainLooper()).post(callback);
    }

    @Override
    public boolean isProgressBarShowing() {
        return mProgressBarManager.isShowing();
    }

    @Override
    public boolean isEmpty() {
        return mSectionFragmentFactory == null || mSectionFragmentFactory.isEmpty();
    }
}
