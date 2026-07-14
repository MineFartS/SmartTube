package minefarts.smarttube.app.presenters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import minefarts.smarttube.utils.oauth.Account;
import minefarts.smarttube.utils.service.data.MediaGroup;
import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.utils.locale.LocaleUtility;
import minefarts.smarttube.utils.mylogger.Log;
import com.liskovsoft.sharedutils.rx.RxHelper;
import minefarts.smarttube.R;
import minefarts.smarttube.app.models.data.BrowseSection;
import minefarts.smarttube.app.models.data.Queue;
import minefarts.smarttube.app.models.data.SettingsGroup;
import minefarts.smarttube.app.models.data.SettingsItem;
import minefarts.smarttube.app.models.data.Video;
import minefarts.smarttube.app.models.data.VideoGroup;
import minefarts.smarttube.app.models.errors.CategoryEmptyError;
import minefarts.smarttube.app.models.errors.ErrorFragmentData;
import minefarts.smarttube.app.models.errors.SignInError;
import minefarts.smarttube.app.models.playback.service.VideoStateService;
import minefarts.smarttube.app.models.playback.service.State;
import minefarts.smarttube.app.presenters.base.BasePresenter;
import minefarts.smarttube.app.models.playback.controllers.VideoLoaderController;
import minefarts.smarttube.app.presenters.dialogs.menu.ChannelUploadsMenuPresenter;
import minefarts.smarttube.app.presenters.dialogs.menu.SectionMenuPresenter;
import minefarts.smarttube.app.presenters.dialogs.menu.VideoMenuPresenter;
import minefarts.smarttube.app.presenters.dialogs.menu.VideoMenuPresenter.VideoMenuCallback;
import minefarts.smarttube.app.presenters.dialogs.menu.providers.channelgroup.ChannelGroupServiceWrapper;
import minefarts.smarttube.app.views.BrowseView;
import minefarts.smarttube.utils.BrowseProcessorManager;
import minefarts.smarttube.app.models.playback.BasePlayerController;
import minefarts.smarttube.app.models.playback.BasePlayerController.AccountChangeListener;
import minefarts.smarttube.prefs.AccountsData;
import minefarts.smarttube.prefs.MainUIData;
import minefarts.smarttube.prefs.PlayerTweaksData;
import minefarts.smarttube.utils.Utils;
import minefarts.smarttube.utils.browse.BrowseService2Wrapper;
import minefarts.smarttube.app.presenters.settings.AboutSettingsPresenter;
import minefarts.smarttube.app.presenters.settings.AccountSettingsPresenter;
import minefarts.smarttube.app.presenters.settings.ContentBlockSettingsPresenter;
import minefarts.smarttube.app.presenters.settings.GeneralSettingsPresenter;
import minefarts.smarttube.app.presenters.settings.MainUISettingsPresenter;
import minefarts.smarttube.app.presenters.settings.PlayerSettingsPresenter;
import minefarts.smarttube.app.presenters.settings.RemoteControlSettingsPresenter;
import minefarts.smarttube.exoplayer.selector.FormatItem.VideoPreset;
import minefarts.smarttube.app.presenters.PlaybackPresenter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

public class BrowsePresenter extends BasePresenter<BrowseView> implements AccountChangeListener {
    
    private static final String TAG = BrowsePresenter.class.getSimpleName();
    
    @SuppressLint("StaticFieldLeak")
    private static BrowsePresenter sInstance;
    
    List<BrowseSection> mSections;
    List<BrowseSection> mErrorSections;
    Map<Integer, Observable<MediaGroup>> mGridMapping;
    Map<Integer, Observable<List<MediaGroup>>> mRowMapping;
    Map<Integer, Callable<List<SettingsItem>>> mSettingsGridMapping;
    Map<Integer, Callable<List<Video>>> mLocalGridMappings;
    Map<Integer, BrowseSection> mSectionsMapping;
    
    static final BrowseService2Wrapper mBrowseService = BrowseService2Wrapper.INSTANCE;
    BrowseProcessorManager mBrowseProcessor;
    ChannelUploadsPresenter mChannelUploadsPresenter;
    VideoStateService mVideoStateService;
    VideoLoaderController mVideoLoaderController;
    
    List<Disposable> mActions;
    Runnable mRefreshSection = this::refresh;
    BrowseSection mCurrentSection;
    Video mCurrentVideo;
    long mLastUpdateTimeMs = -1;
    int mBootSectionIndex;
    int mBootstrapSectionId = -1;

    public static BrowsePresenter instance(Context context) {
        if (sInstance == null) {
            sInstance = new BrowsePresenter();

            sInstance.mSections = new ArrayList<>();
            sInstance.mErrorSections = new ArrayList<>();
            sInstance.mGridMapping = new HashMap<>();
            sInstance.mRowMapping = new HashMap<>();
            sInstance.mSettingsGridMapping = new HashMap<>();
            sInstance.mLocalGridMappings = new HashMap<>();
            sInstance.mSectionsMapping = new HashMap<>();

            BasePlayerController.addAccountListener(sInstance);

            sInstance.mBrowseProcessor = new BrowseProcessorManager(context, sInstance::syncItem);
            sInstance.mChannelUploadsPresenter = ChannelUploadsPresenter.instance(context);
            sInstance.mVideoStateService = VideoStateService.instance(context);
            sInstance.mVideoLoaderController = PlaybackPresenter.instance(context).getController(VideoLoaderController.class);
            
            sInstance.mActions = new ArrayList<>();

            sInstance.initSectionMappings();
            sInstance.updatePlaylistsStyle();
        }

        sInstance.setContext(context);

        return sInstance;
    }

    @Override
    public void onViewInitialized() {
        super.onViewInitialized();

        if (getView() == null) return;

        updateSections();

        // Move default focus
        int selectedSectionIndex = findSectionIndex(mCurrentSection != null ? mCurrentSection.getId() : mBootstrapSectionId);
        mBootstrapSectionId = -1;
        getView().selectSection(selectedSectionIndex != -1 ? selectedSectionIndex : mBootSectionIndex, true);
    }

    @Override
    public void onViewPaused() {
        super.onViewPaused();

        saveSelectedItems();
    }

    @Override
    public void onViewResumed() {
        super.onViewResumed();

        refreshIfNeeded();
    }

    private void refreshIfNeeded() {
        if (getView() == null || !isHomeSection() || mLastUpdateTimeMs == -1 || System.currentTimeMillis() - mLastUpdateTimeMs < 3 * 60 * 60 * 1_000) return;

        refresh(false);
    }

    private void saveSelectedItems() {
        // Fix position reset when jumping between sections
        if (mCurrentVideo != null && mCurrentVideo.getPositionInsideGroup() == 0 && (System.currentTimeMillis() - mCurrentVideo.timestamp) < 10_000) return;

        if (isSubscriptionsSection() && getGeneralData().isRememberSubscriptionsPositionEnabled()) {
            getGeneralData().setSelectedItem(mCurrentSection.getId(), mCurrentVideo);
        }
    }

    void initSectionMappings() {
        
        //===================================================================
        // mSectionsMapping

        mSectionsMapping.put(
            MediaGroup.TYPE_HOME, 
            new BrowseSection(
                MediaGroup.TYPE_HOME, 
                "Home", 
                BrowseSection.TYPE_ROW, 
                R.drawable.icon_home, 
                false
            )
        );
        
        mSectionsMapping.put(
            MediaGroup.TYPE_CHANNEL_UPLOADS, 
            new BrowseSection(
                MediaGroup.TYPE_CHANNEL_UPLOADS, 
                getContext().getString(R.string.header_channels), 
                BrowseSection.TYPE_MULTI_GRID,
                R.drawable.icon_channels, 
                false
            )
        );

        mSectionsMapping.put(
            MediaGroup.TYPE_SUBSCRIPTIONS, 
            new BrowseSection(
                MediaGroup.TYPE_SUBSCRIPTIONS, 
                "Subscriptions", 
                BrowseSection.TYPE_GRID, 
                R.drawable.icon_subscriptions, 
                false
            )
        );

        mSectionsMapping.put(
            MediaGroup.TYPE_HISTORY, 
            new BrowseSection(
                MediaGroup.TYPE_HISTORY, 
                getContext().getString(R.string.header_history), 
                BrowseSection.TYPE_GRID, 
                R.drawable.icon_history, 
                true
            )
        );

        mSectionsMapping.put(
            MediaGroup.TYPE_USER_PLAYLISTS, 
            new BrowseSection(
                MediaGroup.TYPE_USER_PLAYLISTS, 
                getContext().getString(R.string.header_playlists), 
                BrowseSection.TYPE_ROW, 
                R.drawable.icon_playlist, 
                false
            )
        );

        mSectionsMapping.put(
            MediaGroup.TYPE_NOTIFICATIONS, 
            new BrowseSection(
                MediaGroup.TYPE_NOTIFICATIONS, 
                getContext().getString(R.string.header_notifications), 
                BrowseSection.TYPE_GRID, 
                R.drawable.icon_notification, 
                false
            )
        );

        mSectionsMapping.put(
            MediaGroup.TYPE_PLAYBACK_QUEUE, 
            new BrowseSection(
                MediaGroup.TYPE_PLAYBACK_QUEUE, 
                "Playback queue", 
                BrowseSection.TYPE_GRID, 
                R.drawable.icon_queue, 
                false
            )
        );

        mSectionsMapping.put(
            MediaGroup.TYPE_SETTINGS, 
            new BrowseSection(
                MediaGroup.TYPE_SETTINGS, 
                getContext().getString(R.string.header_settings), 
                BrowseSection.TYPE_SETTINGS_GRID, 
                R.drawable.icon_settings
            )
        );

        //===================================================================
        // mRowMapping

        mRowMapping.put(
            MediaGroup.TYPE_HOME, 
            getContentService().getHomeObserve()
        );
        
        mRowMapping.put(
            MediaGroup.TYPE_USER_PLAYLISTS, 
            getContentService().getPlaylistRowsObserve()
        );

        mGridMapping.put(
            MediaGroup.TYPE_SUBSCRIPTIONS, 
            getContentService().getSubscriptionsObserve()
        );

        mGridMapping.put(
            MediaGroup.TYPE_HISTORY, 
            getContentService().getHistoryObserve()
        );
        
        mGridMapping.put(
            MediaGroup.TYPE_CHANNEL_UPLOADS, 
            RxHelper.fromCallable(mBrowseService::getSubscribedChannels)
        );
        
        mGridMapping.put(
            MediaGroup.TYPE_NOTIFICATIONS, 
            getNotificationsService().getNotificationItemsObserve()
        );

        //===================================================================
        // mSettingsGridMapping

        mSettingsGridMapping.put(
            MediaGroup.TYPE_SETTINGS, 
            this::getSettingItems
        );

        //===================================================================
        // mLocalGridMappings
        mLocalGridMappings.put(
            MediaGroup.TYPE_PLAYBACK_QUEUE, 
            Queue::getAll
        );

        //===================================================================
    }

    private void initPinnedSections() {
        mSections.clear();

        Collection<Video> pinnedItems = getSidebarService().getPinnedItems();

        for (Video item : pinnedItems) {
            if (item != null) {
                if (item.sectionId == -1) { // pinned channel or playlist
                    BrowseSection section = createPinnedSection(item);
                    mSections.add(section);
                } else {
                    BrowseSection section = mSectionsMapping.get(item.sectionId);

                    if (section != null) {
                        mSections.add(section);
                    }
                }
            }
        }
    }

    private void initPinnedCallbacks() {
        Collection<Video> pinnedItems = getSidebarService().getPinnedItems();

        for (Video item : pinnedItems) {
            if (item != null && item.sectionId == -1) {
                createPinnedMapping(item);
            }
        }
    }

    private List<SettingsItem> getSettingItems() {

        Context context = getContext();
        List<SettingsItem> settingItems = new ArrayList<>();

        settingItems.add(new SettingsItem(
            context.getString(R.string.settings_accounts), 
            () -> AccountSettingsPresenter.instance(context).show(), 
            R.drawable.settings_account
        ));

        settingItems.add(new SettingsItem(
            context.getString(R.string.settings_remote_control), 
            () -> RemoteControlSettingsPresenter.instance(context).show(), 
            R.drawable.settings_cast
        ));

        settingItems.add(new SettingsItem(
            context.getString(R.string.settings_general), 
            () -> GeneralSettingsPresenter.instance(context).show(), 
            R.drawable.settings_app
        ));

        settingItems.add(new SettingsItem(
            context.getString(R.string.settings_main_ui), 
            () -> MainUISettingsPresenter.instance(context).show(), 
            R.drawable.settings_main_ui
        ));

        settingItems.add(new SettingsItem(
            context.getString(R.string.settings_player), 
            () -> PlayerSettingsPresenter.instance(context).show(), 
            R.drawable.settings_player
        ));
                        
        settingItems.add(new SettingsItem(
            "SponsorBlock", 
            () -> ContentBlockSettingsPresenter.instance(context).show(), 
            R.drawable.settings_block
        ));

        settingItems.add(new SettingsItem(
            "About", 
            () -> AboutSettingsPresenter.instance(context).show(), 
            R.drawable.settings_about
        ));

        return settingItems;
    }

    public void updateSections() {
        if (getView() == null) return;

        initPinnedSections();
        initPinnedCallbacks();

        refreshSections();
    }

    private void refreshSections() {
        if (getView() == null) return;

        // clean up (profile changed etc)
        getView().removeAllSections();

        int index = 0;

        for (BrowseSection section : mErrorSections) {
            getView().addSection(index++, section);
        }

        for (BrowseSection section : mSections) { // contains sections and pinned items!
            if (section.getId() == MediaGroup.TYPE_SETTINGS) {
                section.setEnabled(true);
            }

            if (section.isEnabled()) {
                if (section.getId() == MediaGroup.TYPE_HOME) {
                    mBootSectionIndex = index;
                }
                getView().addSection(index++, section);
            } else {
                getView().removeSection(section);
            }
        }

        // Refresh and restore last focus
        int selectedSectionIndex = findSectionIndex(mCurrentSection != null ? mCurrentSection.getId() : -1);
        getView().selectSection(selectedSectionIndex != -1 ? selectedSectionIndex : mBootSectionIndex, false);
    }

    public void updatePlaylistsStyle() {

        mRowMapping.remove(MediaGroup.TYPE_USER_PLAYLISTS);
        mGridMapping.put(MediaGroup.TYPE_USER_PLAYLISTS, getContentService().getPlaylistsObserve());
        updateCategoryType(MediaGroup.TYPE_USER_PLAYLISTS, BrowseSection.TYPE_GRID);
        
    }

    private void updateCategoryType(int categoryId, int categoryType) {
        if (categoryType == -1 || categoryId == -1 || mSections == null) return;

        BrowseSection section = mSectionsMapping.get(categoryId);

        if (section != null) {
            section.setType(categoryType);
        }

        for (BrowseSection category : mSections) {
            if (category.getId() == categoryId) {
                category.setType(categoryType);
                break;
            }
        }
    }

    @Override
    public void onViewDestroyed() {
        super.onViewDestroyed();
        disposeActions();
        saveSelectedItems();
    }

    @Override
    public void onVideoItemSelected(Video item) {
        if (getView() == null) return;

        if (
            isMultiGridChannelUploadsSection() 
            && belongsToChannelUploads(item)
            && mCurrentSection != null
        ) updateVideoGrid(
            mCurrentSection, 
            mChannelUploadsPresenter.obtainUploadsObservable(item), 
            1, 
            false
        );
        
        mCurrentVideo = item;
    }

    @Override
    public void onVideoItemClicked(Video item) {
        if (getContext() == null) return;

        mVideoLoaderController.openVideo(item);
    }

    @Override
    public void onVideoItemLongClicked(Video item) {
        if (getContext() == null) return;

        if (belongsToChannelUploads(item)) { // We need to be sure we exactly on Channels section
            
            ChannelUploadsMenuPresenter.instance(getContext()).showMenu(item, (videoItem, action) -> {
                if (action == VideoMenuCallback.ACTION_UNSUBSCRIBE) { // works with any uploads section look
                    removeItem(item);
                }
            });

        } else {

            VideoMenuPresenter VMP = VideoMenuPresenter.instance(getContext());

            VMP.showMenu(item, (videoItem, action) -> {
                if (action == VideoMenuCallback.ACTION_REMOVE ||
                    action == VideoMenuCallback.ACTION_REMOVE_FROM_PLAYLIST ||
                    action == VideoMenuCallback.ACTION_REMOVE_FROM_QUEUE) {
                    removeItem(videoItem);
                } else if (action == VideoMenuCallback.ACTION_UNSUBSCRIBE && isMultiGridChannelUploadsSection()) {
                    removeItem(mCurrentVideo);
                    VMP.closeDialog();
                } else if (action == VideoMenuCallback.ACTION_UNSUBSCRIBE && isSubscriptionsSection()) {
                    removeItemAuthor(videoItem);
                    VMP.closeDialog();
                } else if (action == VideoMenuCallback.ACTION_REMOVE_AUTHOR) {
                    removeItemAuthor(videoItem);
                }
            });

        }
    }

    @Override
    public void onScrollEnd(Video item) {
        if (item == null) {
            Log.e(TAG, "Can't scroll. Video is null.");
            return;
        }

        continueGroup(item.getGroup(), true, true);
    }

    public void onSectionFocused(int sectionId) {
        
        saveSelectedItems(); // save previous state
        
        mCurrentSection = findSectionById(sectionId);
        mCurrentVideo = null; // fast scroll through the sections (fix empty selected item)
        updateCurrentSection();

        if (getView() != null 
            && isSubscriptionsSection() 
            && getGeneralData().isRememberSubscriptionsPositionEnabled()
        ) getView().selectSectionItem(
            getGeneralData().getSelectedItem(mCurrentSection.getId())
        );
        
    }

    public void onSectionLongPressed(int sectionId) {
        SectionMenuPresenter.instance(getContext()).showMenu(findSectionById(sectionId));
    }

    @Override
    public boolean hasPendingActions() {
        return RxHelper.isAnyActionRunning(mActions);
    }

    public boolean isItemPinned(Video item) {
        Collection<Video> items = getSidebarService().getPinnedItems();

        return items.contains(item);
    }

    public void moveSectionUp(BrowseSection section) {
        mCurrentSection = section; // move current focus
        getSidebarService().moveSectionUp(section.getId());
        updateSections();
    }

    public void moveSectionDown(BrowseSection section) {
        mCurrentSection = section; // move current focus
        getSidebarService().moveSectionDown(section.getId());
        updateSections();
    }

    public void renameSection(BrowseSection section) {
        mCurrentSection = section; // move current focus
        getSidebarService().renameSection(section.getId(), section.getTitle());
        updateSections();
    }

    public void renameSection(Video section) {
        getSidebarService().renameSection(section.getId(), section.getTitle());
        updateSections();
    }

    public void enableAllSections(boolean enable) {
        enableSection(MediaGroup.TYPE_HISTORY, enable);
        enableSection(MediaGroup.TYPE_USER_PLAYLISTS, enable);
        enableSection(MediaGroup.TYPE_SUBSCRIPTIONS, enable);
        enableSection(MediaGroup.TYPE_CHANNEL_UPLOADS, enable);
        

        enableSection(MediaGroup.TYPE_HOME, enable);

    }

    public void enableSection(int sectionId, boolean enable) {
        getSidebarService().enableSection(sectionId, enable);

        if (!enable && mCurrentSection != null && mCurrentSection.getId() == sectionId) {
            mCurrentSection = findNearestSection(sectionId);
        }

        updateSections();
    }

    public void pinItem(Video item) {
        if (getView() == null) return;

        getSidebarService().addPinnedItem(item);

        createPinnedMapping(item);

        BrowseSection newSection = createPinnedSection(item);
        //Helpers.removeIf(mSections, section -> section.getId() == newSection.getId());
        if (!mSections.contains(newSection)) {
            mSections.add(newSection);
        }
        getView().addSection(-1, newSection);
    }

    public void pinItem(String title, int resId, ErrorFragmentData data) {
        if (getView() == null) return;

        BrowseSection newSection = new BrowseSection(title.hashCode(), title, BrowseSection.TYPE_ERROR, resId, false, data);
        Helpers.removeIf(mErrorSections, section -> section.getId() == newSection.getId());
        mErrorSections.add(newSection);
        getView().addSection(0, newSection);
    }

    public void unpinItem(Video item) {
        getSidebarService().removePinnedItem(item);
        getGeneralData().removeSelectedItem(item.getId());

        BrowseSection section = null;

        for (BrowseSection cat : mSections) {
            if (cat.getId() == item.getId()) {
                section = cat;
                break;
            }
        }

        mGridMapping.remove(item.getId());

        if (getView() != null) {
            getView().removeSection(section);
        }
    }

    public void refresh() {
        refresh(true);
    }

    public void refresh(boolean focusOnContent) {
        updateCurrentSection();
        if (focusOnContent && getView() != null) {
            getView().focusOnContent();
        }
    }

    private void updateRefreshTime() {
        mLastUpdateTimeMs = System.currentTimeMillis();
    }

    private void updateCurrentSection() {
        disposeActions();

        if (getView() == null || mCurrentSection == null) return;

        Log.d(TAG, "Update section %s", mCurrentSection.getTitle());
        updateSection(mCurrentSection);
    }

    private void updateSection(BrowseSection section) {
        switch (section.getType()) {

            case BrowseSection.TYPE_GRID:
            case BrowseSection.TYPE_SHORTS_GRID:
                if (mGridMapping.containsKey(section.getId())) {
                    Observable<MediaGroup> group = mGridMapping.get(section.getId());
                    updateVideoGrid(section, group, -1, section.isAuthOnly());
                } else if (mLocalGridMappings.containsKey(section.getId())) {
                    
                    Callable<List<Video>> localVideos = mLocalGridMappings.get(section.getId());

                    VideoGroup videoGroup = VideoGroup.from(Helpers.get(localVideos), section);
                    videoGroup.setAction(VideoGroup.ACTION_REPLACE);
                    videoGroup.setId(videoGroup.hashCode());
                    videoGroup.setTitle(section.getTitle());
                    getView().updateSection(videoGroup);
                    getView().showProgressBar(false);

                }
                break;

            case BrowseSection.TYPE_ROW:
                Observable<List<MediaGroup>> groups = mRowMapping.get(section.getId());
                updateVideoRows(section, groups, section.isAuthOnly());
                break;

            case BrowseSection.TYPE_SETTINGS_GRID:
                Callable<List<SettingsItem>> items = mSettingsGridMapping.get(section.getId());
                getView().updateSection(SettingsGroup.from(Helpers.get(items), section));
                getView().showProgressBar(false);
                break;

            case BrowseSection.TYPE_MULTI_GRID:
                Observable<MediaGroup> group2 = mGridMapping.get(section.getId());
                updateVideoGrid(section, group2, 0, section.isAuthOnly());
                break;

            case BrowseSection.TYPE_ERROR:
                getView().showProgressBar(false);
                break;
        }

        updateRefreshTime();
    }

    private void updateVideoRows(BrowseSection section, Observable<List<MediaGroup>> groups, boolean authCheck) {
        Log.d(TAG, "loadRowsHeader: Start loading section: " + section.getTitle());

        if (authCheck && !getSignInService().isSigned()) return;

        disposeActions();

            if (getView() == null) {
                Log.e(TAG, "Browse view has been unloaded from the memory. Low RAM?");
                getViewManager().startView(BrowseView.class);
                return;
            }
            
            getView().showProgressBar(true);

        VideoGroup firstGroup = VideoGroup.from(section);
        firstGroup.setAction(VideoGroup.ACTION_REPLACE);
        getView().updateSection(firstGroup);

            if (groups == null) {
                // No group. Maybe just clear.
                getView().showProgressBar(false);
                return;
            }

        Disposable updateAction = groups.subscribe(

            mediaGroups -> {

                getView().showProgressBar(false);

                if (isHomeSection()) {
                    Helpers.removeIf(
                        mediaGroups, 
                        value -> Helpers.containsAny(
                            value.getTitle(),
                            "Primetime", // Free movies and shows row
                            "News", // Top news
                            "news", // Top news
                            "NBA TV", // Sports
                            "The Life of a Showgirl"
                        )
                    );
                }

                for (MediaGroup mediaGroup : mediaGroups) {
                    if (mediaGroup.isEmpty()) continue;

                    VideoGroup videoGroup = VideoGroup.from(mediaGroup, section);

                    getView().updateSection(videoGroup);
                    mBrowseProcessor.process(videoGroup);

                    continueGroup(videoGroup, false, false);
                }

            },

            error -> {
                Log.e(TAG, "updateRowsHeader error: %s", error.getMessage());
                handleLoadError(error);
            }, 

            () -> handleLoadError(null)

        );

        mActions.add(updateAction);

    }

    private void updateVideoGrid(BrowseSection section, Observable<MediaGroup> group, int column, boolean authCheck) {
        
        Log.d(TAG, "loadMultiGridHeader: Start loading section: " + section.getTitle());

        if (authCheck && !getSignInService().isSigned()) return;
        
        disposeActions();

        if (getView() == null) {
            Log.e(TAG, "Browse view has been unloaded from the memory. Low RAM?");
            getViewManager().startView(BrowseView.class);
            return;
        }

        Log.d(TAG, "updateGridHeader: Start loading section: " + section.getTitle());

        getView().showProgressBar(true);

        // Stay on the same group in case of multiple subscribe calls
        VideoGroup baseGroup = VideoGroup.from(section, column);
        baseGroup.setAction(VideoGroup.ACTION_REPLACE);
        getView().updateSection(baseGroup);

        if (group == null) {
            // No group. Maybe just clear.
            getView().showProgressBar(false);
            return;
        }

        Disposable updateAction = group.subscribe(
        
            mediaGroup -> {

                getView().showProgressBar(false);

                if (getView() == null)
                    getViewManager().startView(BrowseView.class);

                VideoGroup videoGroup = VideoGroup.from(baseGroup, mediaGroup);

                State lastState = mVideoStateService.getLastState();

                if (isHistorySection()
                    && !mVideoStateService.isEmpty() 
                    && !videoGroup.isEmpty()
                    && videoGroup.get(0) != lastState.video
                ) {
                    for (State state: mVideoStateService.getStates()) {
                        videoGroup.add(0, state.video);
                    }
                }

                getView().updateSection(videoGroup);
                mBrowseProcessor.process(videoGroup);

                continueGroup(videoGroup, true, false);
            },

            error -> {
                Log.e(TAG, "updateGridHeader error: %s", error.getMessage());
                handleLoadError(error);
            }, 
            
            () -> handleLoadError(null)
        
        );

        mActions.add(updateAction);
    
    }

    private void continueGroup(
        VideoGroup group, 
        boolean showLoading, 
        boolean force
    ) {
        if (
            getView() == null 
            || group == null
            || (getCurrentSection() != null && mLocalGridMappings.containsKey(getCurrentSection().getId()))
            || (!force && !BasePlayerController.shouldContinueTheGroup(getContext(), group, isGridSection()))
        ) return;

        Log.d(TAG, "continueGroup: start continue group: " + group.getTitle());

        if (showLoading) 
            getView().showProgressBar(true);

        Disposable continueAction = getContentService().continueGroupObserve(group.getMediaGroup()).subscribe(
            
            contGroup -> {
                getView().showProgressBar(false);

                VideoGroup videoGroup = VideoGroup.from(group, contGroup);
                getView().updateSection(videoGroup);
                mBrowseProcessor.process(videoGroup);

                continueGroup(videoGroup, showLoading, false);
            },
            
            error -> {
                Log.e(TAG, "continueGroup error: %s", error.getMessage());
                if (getView() != null) {
                    getView().showProgressBar(false);
                }
            }

        );

        mActions.add(continueAction);
    }

    private void disposeActions() {
        RxHelper.disposeActions(mActions);
        Utils.removeCallbacks(mRefreshSection);
        mLastUpdateTimeMs = -1;
        mBrowseProcessor.dispose();
    }

    private boolean belongsToChannelUploads(Video item) {
        return item.belongsToChannelUploads() && !item.hasVideo();
    }

    @Nullable
    public BrowseSection getCurrentSection() {
        return mCurrentSection;
    }

    private BrowseSection findSectionById(int sectionId) {
        for (BrowseSection section : mErrorSections) {
            if (section.getId() == sectionId) {
                return section;
            }
        }

        for (BrowseSection section : mSections) {
            if (section.getId() == sectionId) {
                return section;
            }
        }

        return null;
    }

    private int findSectionIndex(int sectionId) {
        if (sectionId == -1) {
            return -1;
        }

        int sectionIndex = -1;

        for (BrowseSection section : mErrorSections) {
            if (section.isEnabled()) {
                sectionIndex++;
                if (section.getId() == sectionId) {
                    return sectionIndex;
                }
            }
        }

        for (BrowseSection section : mSections) {
            if (section.isEnabled()) {
                sectionIndex++;
                if (section.getId() == sectionId) {
                    return sectionIndex;
                }
            }
        }

        return -1;
    }

    private BrowseSection findNearestSection(int sectionId) {
        BrowseSection result = findNearestSection(mErrorSections, sectionId);

        if (result == null) {
            result = findNearestSection(mSections, sectionId);
        }

        return result;
    }

    private BrowseSection findNearestSection(List<BrowseSection> sections, int sectionId) {
        BrowseSection result = null;
        BrowseSection previousSection = null;
        boolean found = false;
        for (BrowseSection section : sections) {
            if (section.getId() == sectionId) {
                found = true;
                continue;
            }
            if (section.isEnabled()) {
                if (found) {
                    result = section;
                    break;
                }
                previousSection = section;
            }
        }

        return result != null ? result : previousSection;
    }

    private Observable<MediaGroup> createPinnedGridAction(Video item) {
        if (item.channelGroupId != null) {
            return getContentService().getRssFeedObserve(ChannelGroupServiceWrapper.instance(getContext()).findChannelIdsForGroup(item.channelGroupId));
        }

        return mChannelUploadsPresenter.obtainUploadsObservable(item);
    }

    private Observable<List<MediaGroup>> createPinnedRowAction(Video item) {
        return ChannelPresenter.instance(getContext()).obtainChannelObservable(item.channelId);
    }

    /**
     * Is Channels new look enabled?
     */
    public boolean isMultiGridChannelUploadsSection() {
        return mCurrentSection != null && mCurrentSection.getType() == BrowseSection.TYPE_MULTI_GRID && mCurrentSection.getId() == MediaGroup.TYPE_CHANNEL_UPLOADS;
    }

    public boolean isSettingsSection() {
        return isSection(MediaGroup.TYPE_SETTINGS);
    }

    public boolean isPlaylistsSection() {
        return isSection(MediaGroup.TYPE_USER_PLAYLISTS);
    }

    public boolean isHomeSection() {
        return isSection(MediaGroup.TYPE_HOME);
    }

    public boolean isHistorySection() {
        return isSection(MediaGroup.TYPE_HISTORY);
    }

    public boolean isSubscriptionsSection() {
        return isSection(MediaGroup.TYPE_SUBSCRIPTIONS);
    }
    
    public boolean isPlaybackQueueSection() {
        return isSection(MediaGroup.TYPE_PLAYBACK_QUEUE);
    }

    public boolean isPinnedSection() {
        return mCurrentSection != null && isPinnedId(mCurrentSection.getId());
    }

    private boolean isPinnedId(int id) {
        return id > 100;
    }

    private boolean isSection(int sectionId) {
        return mCurrentSection != null && mCurrentSection.getId() == sectionId;
    }

    public void selectSection(int sectionId) {
        getViewManager().startView(BrowseView.class); // focus view

        if (getView() == null) {
            mBootstrapSectionId = sectionId;
            return;
        }

        int sectionIndex = findSectionIndex(sectionId);

        if (sectionIndex == -1) {
            enableSection(sectionId, true);
            sectionIndex = findSectionIndex(sectionId);
            getSidebarService().enableSection(sectionId, false); // enable temporally (till restart)
        }

        if (sectionIndex != -1) {
            getView().selectSection(sectionIndex, true);
        }
    }

    public boolean inForeground() {
        return getViewManager().getTopView() == BrowseView.class;
    }

    private boolean isGridSection() {
        return mCurrentSection != null && mCurrentSection.getType() != BrowseSection.TYPE_ROW;
    }

    @Override
    public void onAccountChanged(Account account) {
        Log.d(TAG, "On account changed");

        if (getView() == null) return;

        initSectionMappings();
        updatePlaylistsStyle();
        updateSections();
    }

    public Video getCurrentVideo() {
        return mCurrentVideo;
    }

    private void createPinnedMapping(Video item) {
        if (enableRows(item)) {
            mRowMapping.put(item.getId(), createPinnedRowAction(item));
        } else {
            mGridMapping.put(item.getId(), createPinnedGridAction(item));
        }
    }

    private BrowseSection createPinnedSection(Video item) {
        return new BrowseSection(
                item.getId(), item.getTitle(), enableRows(item) ? BrowseSection.TYPE_ROW : BrowseSection.TYPE_GRID, R.drawable.icon_pin, item.getCardImageUrl(), false, item);
    }

    private boolean enableRows(Video item) {
        return item.hasChannel() && !item.isPlaylistAsChannel();
    }

    private void handleLoadError(Throwable error) {
        if (getView() == null) return;

        getView().showProgressBar(false);

        if (getView().isEmpty() || error != null) {

            ErrorFragmentData errorFragmentData;

            if (error != null && !Helpers.containsAny(error.getMessage(), "fromNullable result is null")) {
                errorFragmentData = new CategoryEmptyError(getContext(), error);
            } else if (getSignInService().isSigned()) {
                errorFragmentData = new CategoryEmptyError(getContext(), null);
            } else {
                errorFragmentData = new SignInError(getContext());
            }

            getView().showError(errorFragmentData);
            Utils.postDelayed(mRefreshSection, 30_000);
            
        }
    }

}
