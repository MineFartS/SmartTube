package minefarts.smarttube.app.presenters.base;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import minefarts.smarttube.fragment.app.Fragment;

import minefarts.smarttube.app.models.playback.service.VideoStateService;
import minefarts.smarttube.utils.CommentsService;
import minefarts.smarttube.utils.service.ContentService;
import minefarts.smarttube.utils.MediaItemService;
import minefarts.smarttube.utils.NotificationsService;
import minefarts.smarttube.utils.SignInService;
import minefarts.smarttube.app.models.data.Queue;
import minefarts.smarttube.app.models.data.Video;
import minefarts.smarttube.app.models.data.VideoGroup;
import minefarts.smarttube.app.presenters.PlaybackPresenter;
import minefarts.smarttube.app.presenters.service.SidebarService;
import minefarts.smarttube.app.views.BrowseView;
import minefarts.smarttube.app.views.ChannelUploadsView;
import minefarts.smarttube.app.views.ChannelView;
import minefarts.smarttube.ui.playback.PlaybackFragment;
import minefarts.smarttube.app.views.SearchView;
import minefarts.smarttube.app.views.ViewManager;
import minefarts.smarttube.utils.ServiceManager;
import minefarts.smarttube.utils.TickleManager;
import minefarts.smarttube.prefs.GeneralData;
import minefarts.smarttube.prefs.MainUIData;
import minefarts.smarttube.prefs.SearchData;
import minefarts.smarttube.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

public abstract class BasePresenter<T> {

    private WeakReference<T> mView = new WeakReference<>(null);
    private WeakReference<Activity> mActivity = new WeakReference<>(null);
    private WeakReference<Context> mApplicationContext = new WeakReference<>(null);
    
    public void onFinish() {
        // NOP
    }

    public BasePresenter(Context context) {
        setContext(context);
    }

    public void setView(T view) {
        if (checkView(view)) {
            mView = new WeakReference<>(view);
        }
    }

    public T getView() {
        T view = mView.get();

        return checkView(view) ? view : null;
    }

    public void setContext(Context context) {
        
        if (context == null) return;

        // Update the VideoStateService context
        VideoStateService.instance(context);

        // Localization fix: prefer Activity context
        if (context instanceof Activity && Utils.checkActivity((Activity) context)) {
            mActivity = new WeakReference<>((Activity) context);
        }

        // In case view was disposed like SplashView does
        mApplicationContext = new WeakReference<>(context.getApplicationContext());
    }

    public Context getContext() {
        Activity activity = null;

        Activity viewActivity = getViewActivity(mView.get());

        // Trying to find localized context.
        // First, try the view that belongs to this presenter.
        // Second, try the activity that presenter called (could be destroyed).
        if (viewActivity != null) {
            activity = viewActivity;
        } else if (mActivity.get() != null) {
            activity = mActivity.get();
        }

        // In case view was disposed like SplashView does
        // Fallback to non-localized ApplicationContext if others fail
        return Utils.checkActivity(activity) ? activity : mApplicationContext.get();
    }

    public void onViewInitialized() {
        enableSync();
    }

    public void onViewDestroyed() {
        // Multiple views with same presenter fix?
        // View stays in RAM after has been destroyed. Is it a bug?
        //mView = new WeakReference<>(null);
        //mActivity = new WeakReference<>(null);
    }

    public void onViewPaused() {
        // NOP
    }

    public void onViewResumed() {
        if (canViewBeSynced()) {
            // NOTE: don't place cleanup in the onViewResumed!!! This could cause errors when view is resumed.
            if (syncItem(Queue.getChangedItems())) {
                Queue.onNewSession();
            }
        }

        //showBootDialogs();
    }

    protected void removeItem(Video item) {
        removeItem(Collections.singletonList(item), VideoGroup.ACTION_REMOVE);
    }

    protected void removeItemAuthor(Video item) {
        removeItem(Collections.singletonList(item), VideoGroup.ACTION_REMOVE_AUTHOR);
    }

    private void removeItem(List<Video> items, int action) {
        if (items.isEmpty()) {
            return;
        }

        VideoGroup removedGroup = VideoGroup.from(items);
        removedGroup.setAction(action);
        T view = getView();

        updateView(removedGroup, view);
    }

    public boolean syncItem(Video item) {
        return syncItem(Collections.singletonList(item));
    }

    public boolean syncItem(List<Video> items) {
        if (items.isEmpty()) {
            return false;
        }

        VideoGroup syncGroup = VideoGroup.from(items);
        syncGroup.setAction(VideoGroup.ACTION_SYNC);
        T view = getView();

        return updateView(syncGroup, view);
    }

    private boolean canViewBeSynced() {
        T view = getView();
        return view instanceof BrowseView ||
               view instanceof ChannelView ||
               view instanceof ChannelUploadsView ||
               view instanceof SearchView;
    }

    private boolean updateView(VideoGroup group, T view) {
        if (view instanceof BrowseView) {
            ((BrowseView) view).updateSection(group);
        } else if (view instanceof ChannelView) {
            ((ChannelView) view).update(group);
        } else if (view instanceof ChannelUploadsView) {
            ((ChannelUploadsView) view).update(group);
        } else if (view instanceof SearchView) {
            ((SearchView) view).updateSearch(group);
        } else if (view instanceof PlaybackFragment) {
            ((PlaybackFragment) view).updateSuggestions(group);
        } else {
            return false;
        }

        return true;
    }

    private void enableSync() {
        if (this instanceof PlaybackPresenter) {
            Queue.onNewSession();
        }
    }

    /**
     * Check that view's activity is alive
     */
    private static <T> boolean checkView(T view) {
        Activity activity = getViewActivity(view);

        return Utils.checkActivity(activity);
    }

    @SuppressWarnings("deprecation")
    private static <T> Activity getViewActivity(T view) {
        Activity activity = null;

        if (view instanceof Fragment) { // regular fragment
            activity = ((Fragment) view).getActivity();
        } else if (view instanceof android.app.Fragment) { // dialog fragment
            activity = ((android.app.Fragment) view).getActivity();
        } else if (view instanceof Activity) { // splash view
            activity = (Activity) view;
        } else if (view instanceof View) {
            Context context = ((View) view).getContext();
            if (context instanceof Activity) {
                activity = (Activity) context;
            }
        }
        return activity;
    }

    protected MainUIData getMainUIData() {
        return MainUIData.instance(getContext());
    }

    protected GeneralData getGeneralData() {
        return GeneralData.instance(getContext());
    }

    protected SearchData getSearchData() {
        return SearchData.instance(getContext());
    }

    protected SidebarService getSidebarService() {
        return SidebarService.instance(getContext());
    }

    protected CommentsService getCommentsService() {
        return ServiceManager.getCommentsService();
    }

    protected ContentService getContentService() {
        return ServiceManager.getContentService();
    }

    protected SignInService getSignInService() {
        return ServiceManager.getSignInService();
    }

    protected NotificationsService getNotificationsService() {
        return ServiceManager.getNotificationsService();
    }

    protected MediaItemService getMediaItemService() {
        return ServiceManager.getMediaItemService();
    }

    protected ViewManager getViewManager() {
        return ViewManager.instance(getContext());
    }

    protected TickleManager getTickleManager() {
        return TickleManager.instance();
    }

    protected PlaybackPresenter getPlaybackPresenter() {
        return PlaybackPresenter.instance(getContext());
    }
}
