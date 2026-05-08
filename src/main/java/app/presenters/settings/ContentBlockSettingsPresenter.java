package SmartTubeApp.app.presenters.settings;

import android.content.Context;
import android.text.TextUtils;
import androidx.core.content.ContextCompat;
import SmartTubeApp.R;
import SmartTubeApp.app.models.data.Video;
import SmartTubeApp.app.models.playback.controllers.ContentBlockController.SegmentAction;
import SmartTubeApp.app.models.playback.ui.OptionItem;
import SmartTubeApp.app.models.playback.ui.UiOptionItem;
import SmartTubeApp.app.presenters.AppDialogPresenter;
import SmartTubeApp.app.presenters.PlaybackPresenter;
import SmartTubeApp.app.presenters.base.BasePresenter;
import SmartTubeApp.app.models.playback.PlayerEngine;
import SmartTubeApp.misc.ServiceManager;
import SmartTubeApp.prefs.ContentBlockData;
import SmartTubeApp.utils.AppDialogUtil;
import SmartTubeApp.utils.Utils;

import com.liskovsoft.sharedutils.data.SponsorSegment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ContentBlockSettingsPresenter extends BasePresenter<Void> {
    
    private final ContentBlockData mContentBlockData;

    public ContentBlockSettingsPresenter(Context context) {
        super(context);
        mContentBlockData = ContentBlockData.instance(context);
    }

    public static ContentBlockSettingsPresenter instance(Context context) {
        return new ContentBlockSettingsPresenter(context);
    }

    public void show(Runnable onFinish) {
        
        AppDialogPresenter settingsPresenter = AppDialogPresenter.instance(getContext());

        appendSponsorBlockSwitch(settingsPresenter);
        appendExcludeChannelButton(settingsPresenter);
        appendActionsSection(settingsPresenter);
        appendColorMarkersSection(settingsPresenter);

        appendLinks(settingsPresenter);

        settingsPresenter.showDialog(
            getContext().getString(R.string.content_block_provider), 
            onFinish
        );

    }

    public void show() {
        show(null);
    }

    private void appendSponsorBlockSwitch(AppDialogPresenter settingsPresenter) {
        
        Video video = null;

        if (getViewManager().getTopView() == PlayerEngine.class) {
            video = PlaybackPresenter.instance(getContext()).getVideo();
        }

        final String channelId = video != null ? video.channelId : null;

        boolean isChannelExcluded = ContentBlockData.instance(getContext()).isChannelExcluded(channelId);

        OptionItem sponsorBlockOption = UiOptionItem.from(getContext().getString(R.string.enable),
            option -> {
                mContentBlockData.enableSponsorBlock(option.isSelected());
                ContentBlockData.instance(getContext()).stopExcludingChannel(channelId);
            },
            !isChannelExcluded && mContentBlockData.isSponsorBlockEnabled()
        );

        settingsPresenter.appendSingleSwitch(sponsorBlockOption);

    }

    private void appendActionsSection(AppDialogPresenter settingsPresenter) {

        List<OptionItem> options = new ArrayList<>();

        for (SegmentAction action : mContentBlockData.getActions()) {
            options.add(
                UiOptionItem.from(

                    getColoredString(
                        mContentBlockData.getLocalizedRes(action.segmentCategory), 
                        mContentBlockData.getColorRes(action.segmentCategory)
                    ),
                    
                    optionItem -> {
                    
                        AppDialogPresenter dialogPresenter = AppDialogPresenter.instance(getContext());

                        List<OptionItem> nestedOptions = new ArrayList<>();
                        
                        nestedOptions.add(
                            UiOptionItem.from(
                                getContext().getString(R.string.content_block_action_none),
                                optionItem1 -> action.actionType = ContentBlockData.ACTION_DO_NOTHING,
                                action.actionType == ContentBlockData.ACTION_DO_NOTHING
                            )
                        );

                        nestedOptions.add(
                            UiOptionItem.from(
                                getContext().getString(R.string.content_block_action_toast),
                                optionItem1 -> action.actionType = ContentBlockData.ACTION_SKIP_WITH_TOAST,
                                action.actionType == ContentBlockData.ACTION_SKIP_WITH_TOAST
                            )
                        );

                        String title = getContext().getString(mContentBlockData.getLocalizedRes(action.segmentCategory));

                        dialogPresenter.appendRadioCategory(title, nestedOptions);
                        
                        dialogPresenter.showDialog(
                            title, 
                            mContentBlockData::persistActions
                        );

                    }
                )
            );
        }

        settingsPresenter.appendStringsCategory(
            getContext().getString(R.string.content_block_action_type), 
            options
        );

    }

    private void appendColorMarkersSection(AppDialogPresenter settingsPresenter) {

        List<OptionItem> options = new ArrayList<>();

        for (String segmentCategory : mContentBlockData.getAllCategories()) {
            options.add(
                UiOptionItem.from(
                    getColoredString(
                        mContentBlockData.getLocalizedRes(segmentCategory),
                        mContentBlockData.getColorRes(segmentCategory)
                    ),
                    optionItem -> {
                        if (optionItem.isSelected()) {
                            mContentBlockData.enableColorMarker(segmentCategory);
                        } else {
                            mContentBlockData.disableColorMarker(segmentCategory);
                        }
                    },
                    mContentBlockData.isColorMarkerEnabled(segmentCategory)
                )
            );
        }

        settingsPresenter.appendCheckedCategory(
            getContext().getString(R.string.sponsor_color_markers), 
            options
        );

    }

    private void appendLinks(AppDialogPresenter settingsPresenter) {
        OptionItem statsCheckOption = UiOptionItem.from(getContext().getString(R.string.content_block_status),
                option -> Utils.openLink(getContext(), getContext().getString(R.string.content_block_status_url)));

        OptionItem webSiteOption = UiOptionItem.from(getContext().getString(R.string.about_sponsorblock),
                option -> Utils.openLink(getContext(), getContext().getString(R.string.content_block_provider_url)));

        settingsPresenter.appendSingleButton(statsCheckOption);
        settingsPresenter.appendSingleButton(webSiteOption);
    }

    private void appendExcludeChannelButton(AppDialogPresenter settingsPresenter) {
        Video video = PlaybackPresenter.instance(getContext()).getVideo();

        if (video == null || getViewManager().getTopView() != PlayerEngine.class) {
            return;
        }

        settingsPresenter.appendSingleButton(AppDialogUtil.createExcludeFromContentBlockButton(
            getContext(), 
            video, 
            settingsPresenter::closeDialog
        ));
        
    }

    private CharSequence getColoredString(int strResId, int colorResId) {
        String origin = getContext().getString(strResId);
        CharSequence colorMark = Utils.color("●", ContextCompat.getColor(getContext(), colorResId));
        return TextUtils.concat( colorMark, " ", origin);
    }
}
