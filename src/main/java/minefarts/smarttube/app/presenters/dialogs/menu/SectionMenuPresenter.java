package minefarts.smarttube.app.presenters.dialogs.menu;

import android.content.Context;

import androidx.annotation.Nullable;

import minefarts.smarttube.utils.service.data.MediaGroup;
import minefarts.smarttube.utils.data.MediaItem;
import minefarts.smarttube.utils.helpers.MessageHelpers;
import minefarts.smarttube.R;
import minefarts.smarttube.app.models.data.BrowseSection;
import minefarts.smarttube.app.models.data.Video;
import minefarts.smarttube.app.models.playback.ui.UiOptionItem;
import minefarts.smarttube.app.presenters.AppDialogPresenter;
import minefarts.smarttube.app.presenters.BrowsePresenter;
import minefarts.smarttube.app.presenters.PlaybackPresenter;
import minefarts.smarttube.app.presenters.dialogs.menu.VideoMenuPresenter.MenuAction;
import minefarts.smarttube.app.presenters.dialogs.menu.VideoMenuPresenter.VideoMenuCallback;
import minefarts.smarttube.app.presenters.dialogs.menu.providers.ContextMenuManager;
import minefarts.smarttube.app.presenters.dialogs.menu.providers.ContextMenuProvider;
import minefarts.smarttube.app.views.SplashView;
import minefarts.smarttube.utils.ServiceManager;
import minefarts.smarttube.prefs.MainUIData;
import minefarts.smarttube.utils.SimpleEditDialog;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SectionMenuPresenter extends BaseMenuPresenter {

    private final AppDialogPresenter mDialogPresenter;
    private Video mVideo;
    private BrowseSection mSection;
    private boolean mIsMarkAllChannelsWatchedEnabled;
    private boolean mIsRefreshEnabled;
    private boolean mIsMoveSectionEnabled;
    private boolean mIsRenameSectionEnabled;
    private final Map<Long, MenuAction> mMenuMapping = new HashMap<>();

    private SectionMenuPresenter(Context context) {
        super(context);
        mDialogPresenter = AppDialogPresenter.instance(context);

        initMenuMapping();
    }

    public static SectionMenuPresenter instance(Context context) {
        return new SectionMenuPresenter(context);
    }

    @Override
    protected @Nullable Video getVideo() {
        return mVideo;
    }

    @Override
    protected BrowseSection getSection() {
        return mSection;
    }

    @Override
    protected AppDialogPresenter getDialogPresenter() {
        return mDialogPresenter;
    }

    @Override
    protected VideoMenuCallback getCallback() {
        return null;
    }

    public void showMenu(BrowseSection section) {
        if (section == null) return;

        disposeActions();

        mSection = section;
        mVideo = section.getData() instanceof Video ? (Video) section.getData() : null;

        ServiceManager.authCheck(
            this::obtainPlaylistsAndShowDialogSigned, 
            this::prepareAndShowDialogUnsigned
        );
        
    }

    private void obtainPlaylistsAndShowDialogSigned() {
        prepareAndShowDialogSigned();
    }

    private void prepareAndShowDialogSigned() {

        if (getContext() == null) return;

        appendRefreshButton();
        appendUnpinVideoFromSidebarButton();
        appendUnpinSectionFromSidebarButton();
        appendAccountSelectionButton();
        appendMoveSectionButton();
        appendRenameSectionButton();
        appendCreatePlaylistButton();
        appendUpdateCheckButton();

        for (Long menuItem : MainUIData.instance(getContext()).getMenuItemsOrdered()) {
            MenuAction menuAction = mMenuMapping.get(menuItem);
            if (menuAction != null) {
                menuAction.run();
            }
        }

        if (!mDialogPresenter.isEmpty()) {
            String title = mSection != null ? mSection.getTitle() : null;
            mDialogPresenter.showDialog(title, this::disposeActions);
        }

    }

    private void prepareAndShowDialogUnsigned() {

        if (getContext() == null) return;

        appendRefreshButton();
        appendUnpinVideoFromSidebarButton();
        appendUnpinSectionFromSidebarButton();
        appendAccountSelectionButton();
        appendMoveSectionButton();
        appendRenameSectionButton();

        for (Long menuItem : MainUIData.instance(getContext()).getMenuItemsOrdered()) {
            MenuAction menuAction = mMenuMapping.get(menuItem);
            if (menuAction != null && !menuAction.isAuth()) {
                menuAction.run();
            }
        }

        if (!mDialogPresenter.isEmpty()) {
            String title = mSection != null ? mSection.getTitle() : null;
            mDialogPresenter.showDialog(title, this::disposeActions);
        }

    }

    private void appendRefreshButton() {
        if (!mIsRefreshEnabled) return;

        if (mSection == null || mSection.getId() == MediaGroup.TYPE_SETTINGS) return;

        mDialogPresenter.appendSingleButton(
                UiOptionItem.from(getContext().getString(R.string.refresh_section), optionItem -> {
                    if (BrowsePresenter.instance(getContext()).getView() != null) {
                        BrowsePresenter.instance(getContext()).getView().focusOnContent();
                        BrowsePresenter.instance(getContext()).refresh();
                    }
                    mDialogPresenter.closeDialog();
                }));
    }

    private void appendMoveSectionButton() {
        if (!mIsMoveSectionEnabled) return;

        if (mSection == null) return;

        mDialogPresenter.appendSingleButton(
                UiOptionItem.from(getContext().getString(R.string.move_section_up), optionItem -> {
                    //mDialogPresenter.closeDialog();
                    BrowsePresenter.instance(getContext()).moveSectionUp(mSection);
                }));

        mDialogPresenter.appendSingleButton(
                UiOptionItem.from(getContext().getString(R.string.move_section_down), optionItem -> {
                    //mDialogPresenter.closeDialog();
                    BrowsePresenter.instance(getContext()).moveSectionDown(mSection);
                }));
    }

    private void appendRenameSectionButton() {
        if (!mIsRenameSectionEnabled) return;

        if (mSection == null || mSection.isDefault() || getVideo() == null ||
                (!getVideo().hasPlaylist() && !getVideo().hasReloadPageKey() && !getVideo().hasChannel())) return;

        mDialogPresenter.appendSingleButton(
                UiOptionItem.from(getContext().getString(R.string.rename_section), optionItem -> {
                    mDialogPresenter.closeDialog();
                    SimpleEditDialog.show(
                            getContext(),
                            getContext().getString(R.string.rename_section),
                            mSection.getTitle(),
                            newValue -> {
                                mSection.setTitle(newValue);
                                BrowsePresenter.instance(getContext()).renameSection(mSection);
                                return true;
                            });
                }));
    }

    private void processNextChannel(Iterator<MediaItem> iterator) {
        if (iterator.hasNext()) {
            MediaItem next = iterator.next();

            if (!next.hasNewContent()) {
                processNextChannel(iterator);
                return;
            }

            MessageHelpers.showMessage(getContext(), next.getTitle());
            ServiceManager.loadChannelUploads(next, (groupTmp) -> processNextChannel(iterator));
        } else {
            MessageHelpers.showMessage(getContext(), R.string.msg_done);
        }
    }

    private void disposeActions() {
        //RxUtils.disposeActions(mPlaylistAction);
    }

    @Override
    protected void updateEnabledMenuItems() {

        super.updateEnabledMenuItems();

        mIsRefreshEnabled = true;
        mIsMarkAllChannelsWatchedEnabled = true;
        mIsMoveSectionEnabled = true;
        mIsRenameSectionEnabled = true;

        MainUIData mainUIData = MainUIData.instance(getContext());

        mIsMoveSectionEnabled = mainUIData.isMenuItemEnabled(MainUIData.MENU_ITEM_MOVE_SECTION_UP);
        mIsMoveSectionEnabled = mainUIData.isMenuItemEnabled(MainUIData.MENU_ITEM_MOVE_SECTION_DOWN);
        mIsRenameSectionEnabled = mainUIData.isMenuItemEnabled(MainUIData.MENU_ITEM_RENAME_SECTION);
    
    }

    private void initMenuMapping() {
        mMenuMapping.clear();

        for (ContextMenuProvider provider : new ContextMenuManager(getContext()).getProviders()) {
            if (provider.getMenuType() != ContextMenuProvider.MENU_TYPE_SECTION) {
                continue;
            }
            mMenuMapping.put(provider.getId(), new MenuAction(() -> appendContextMenuItem(provider), false));
        }
    }

    private void appendContextMenuItem(ContextMenuProvider provider) {
        MainUIData mainUIData = MainUIData.instance(getContext());
        if (mainUIData.isMenuItemEnabled(provider.getId()) && provider.isEnabled(getVideo())) {
            mDialogPresenter.appendSingleButton(
                    UiOptionItem.from(getContext().getString(provider.getTitleResId()), optionItem -> provider.onClicked(getVideo(), getCallback()))
            );
        }
    }
}
