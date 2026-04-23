package SmartTubeApp.app.presenters.dialogs.menu.providers.channelgroup;

import android.content.Context;

import androidx.annotation.NonNull;

import com.liskovsoft.youtubeapi.data.ItemGroup;
import SmartTubeApp.R;
import SmartTubeApp.app.models.data.Video;
import SmartTubeApp.app.presenters.AppDialogPresenter;
import SmartTubeApp.app.presenters.BrowsePresenter;
import SmartTubeApp.app.presenters.dialogs.menu.VideoMenuPresenter.VideoMenuCallback;
import SmartTubeApp.app.presenters.dialogs.menu.providers.ContextMenuProvider;
import SmartTubeApp.utils.SimpleEditDialog;

public class RenameGroupMenuProvider extends ContextMenuProvider {
    private final Context mContext;
    private final ChannelGroupServiceWrapper mService;

    public RenameGroupMenuProvider(@NonNull Context context, int idx) {
        super(idx);
        mContext = context;
        mService = ChannelGroupServiceWrapper.instance(context);
    }

    @Override
    public int getTitleResId() {
        return R.string.rename_group;
    }

    @Override
    public void onClicked(Video item, VideoMenuCallback callback) {
        AppDialogPresenter.instance(mContext).closeDialog();
        SimpleEditDialog.show(
                mContext,
                mContext.getString(R.string.rename_group),
                item.title,
                newValue -> {
                    item.title = newValue;
                    BrowsePresenter.instance(mContext).renameSection(item);

                    ItemGroup channelGroup = mService.findChannelGroupById(item.channelGroupId);

                    if (channelGroup != null) {
                        //channelGroup.title = newValue;
                        //mService.addChannelGroup(channelGroup);
                        mService.renameChannelGroup(channelGroup, newValue);
                    }

                    return true;
                });
    }

    @Override
    public boolean isEnabled(Video item) {
        return item != null && item.channelGroupId != null;
    }

    @Override
    public int getMenuType() {
        return MENU_TYPE_SECTION;
    }
}
