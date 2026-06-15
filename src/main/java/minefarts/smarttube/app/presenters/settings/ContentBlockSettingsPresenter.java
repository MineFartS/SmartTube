package minefarts.smarttube.app.presenters.settings;

import android.content.Context;
import android.text.TextUtils;
import androidx.core.content.ContextCompat;

import minefarts.smarttube.R;
import minefarts.smarttube.app.models.data.Video;
import minefarts.smarttube.app.models.playback.controllers.ContentBlockController.SegmentAction;
import minefarts.smarttube.app.models.playback.ui.UiOptionItem;
import minefarts.smarttube.app.presenters.AppDialogPresenter;
import minefarts.smarttube.app.presenters.PlaybackPresenter;
import minefarts.smarttube.app.presenters.base.BasePresenter;
import minefarts.smarttube.ui.playback.PlaybackFragment2;
import minefarts.smarttube.utils.ServiceManager;
import minefarts.smarttube.prefs.ContentBlockData;
import minefarts.smarttube.utils.AppDialogUtil;
import minefarts.smarttube.utils.Utils;

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

        settingsPresenter.showDialog("SponsorBlock", onFinish);

    }

    public void show() {
        show(null);
    }

    private void appendSponsorBlockSwitch(AppDialogPresenter settingsPresenter) {
        
        Video video = null;

        if (getViewManager().getTopView() == PlaybackFragment2.class) {
            video = PlaybackPresenter.instance(getContext()).getVideo();
        }

        final String channelId = video != null ? video.channelId : null;

        boolean isChannelExcluded = ContentBlockData.instance(getContext()).isChannelExcluded(channelId);

        UiOptionItem sponsorBlockOption = UiOptionItem.from(getContext().getString(R.string.enable),
            option -> {
                mContentBlockData.enableSponsorBlock(option.isSelected());
                ContentBlockData.instance(getContext()).stopExcludingChannel(channelId);
            },
            !isChannelExcluded && mContentBlockData.isSponsorBlockEnabled()
        );

        settingsPresenter.appendSingleSwitch(sponsorBlockOption);

    }

    private void appendActionsSection(AppDialogPresenter settingsPresenter) {

        List<UiOptionItem> options = new ArrayList<>();

        for (SegmentAction action : mContentBlockData.getActions()) {
            options.add(
                UiOptionItem.from(

                    getColoredString(
                        action.segmentCategory,
                        mContentBlockData.getColorRes(action.segmentCategory)
                    ),
                    
                    optionItem -> {
                    
                        AppDialogPresenter dialogPresenter = AppDialogPresenter.instance(getContext());

                        List<UiOptionItem> nestedOptions = new ArrayList<>();
                        
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

                        dialogPresenter.appendRadioCategory(action.segmentCategory, nestedOptions);
                        
                        dialogPresenter.showDialog(
                            action.segmentCategory, 
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

        List<UiOptionItem> options = new ArrayList<>();

        for (String segmentCategory : mContentBlockData.getAllCategories()) {
            options.add(
                UiOptionItem.from(
                    getColoredString(
                        segmentCategory,
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
        
        UiOptionItem statsCheckOption = UiOptionItem.from(
            getContext().getString(R.string.content_block_status),
            option -> Utils.openLink(getContext(), "https://status.sponsor.ajay.app")
        );

        UiOptionItem webSiteOption = UiOptionItem.from(
            getContext().getString(R.string.about_sponsorblock),
            option -> Utils.openLink(getContext(), "https://sponsor.ajay.app")
        );

        settingsPresenter.appendSingleButton(statsCheckOption);
        settingsPresenter.appendSingleButton(webSiteOption);
    }

    private void appendExcludeChannelButton(AppDialogPresenter settingsPresenter) {
        Video video = PlaybackPresenter.instance(getContext()).getVideo();

        if (video == null || getViewManager().getTopView() != PlaybackFragment2.class) return;

        settingsPresenter.appendSingleButton(AppDialogUtil.createExcludeFromContentBlockButton(
            getContext(), 
            video, 
            settingsPresenter::closeDialog
        ));
        
    }

    private CharSequence getColoredString(int strResId, int colorResId) {
        return getColoredString(
            getContext().getString(strResId),
            colorResId
        );
    }

    private CharSequence getColoredString(String origin, int colorResId) {
        CharSequence colorMark = Utils.color("●", ContextCompat.getColor(getContext(), colorResId));
        return TextUtils.concat( colorMark, " ", origin);
    }

}
