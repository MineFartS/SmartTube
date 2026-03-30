package smartyoutubetv1.app.presenters.dialogs.menu.providers;

import android.content.Context;

import smartyoutubetv1.app.presenters.dialogs.menu.providers.channelgroup.RemoveGroupMenuProvider;
import smartyoutubetv1.app.presenters.dialogs.menu.providers.channelgroup.RenameGroupMenuProvider;
import smartyoutubetv1.app.presenters.dialogs.menu.providers.channelgroup.ChannelGroupMenuProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContextMenuManager {

    private final Context mContext;
    
    private final List<ContextMenuProvider> mProviders;

    public ContextMenuManager(Context context) {
        mContext = context;
        mProviders = new ArrayList<>();
        // NOTE: don't change idx after release
        mProviders.add(new ChannelGroupMenuProvider(context, 0));
        mProviders.add(new RemoveGroupMenuProvider(context, 1));
        mProviders.add(new RenameGroupMenuProvider(context, 2));
    }

    public List<ContextMenuProvider> getProviders() {
        return Collections.unmodifiableList(mProviders);
    }
}
