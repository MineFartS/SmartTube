package com.liskovsoft.smartyoutubetv2.common.app.presenters.settings;

import android.content.Context;
import android.text.TextUtils;
import androidx.core.content.ContextCompat;
import com.liskovsoft.smartyoutubetv2.common.R;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.Video;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.controllers.ContentBlockController.SegmentAction;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.OptionItem;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.UiOptionItem;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.AppDialogPresenter;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.PlaybackPresenter;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.base.BasePresenter;
import com.liskovsoft.smartyoutubetv2.common.app.views.PlaybackView;
import com.liskovsoft.smartyoutubetv2.common.misc.MediaServiceManager;
import com.liskovsoft.smartyoutubetv2.common.prefs.ContentBlockData;
import com.liskovsoft.smartyoutubetv2.common.utils.AppDialogUtil;
import com.liskovsoft.smartyoutubetv2.common.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Presenter responsible for building and showing the ContentBlock (SponsorBlock) settings dialog.
 *
 * Responsibilities:
 * - Read and persist ContentBlockData preferences.
 * - Compose dialog UI sections (enable switch, actions, color markers, misc options, links).
 * - Expose a small API to show the dialog and optionally run a callback when it finishes.
 *
 * This presenter is lightweight and created per-call via instance(context).
 */
public class ContentBlockSettingsPresenter extends BasePresenter<Void> {
    // Preferences holder for content-block related settings.
    private final ContentBlockData mContentBlockData;

    public ContentBlockSettingsPresenter(Context context) {
        super(context);
        mContentBlockData = ContentBlockData.instance(context);
    }

    /**
     * Factory accessor. Returns a new presenter using the provided context.
     */
    public static ContentBlockSettingsPresenter instance(Context context) {
        return new ContentBlockSettingsPresenter(context);
    }

    /**
     * Build and show the settings dialog.
     *
     * Sections included:
     * - Global enable switch (and exclude current channel button when applicable)
     * - Action mapping per segment category (nested dialog)
     * - Color markers toggles
     * - Misc toggles (paid content notification, skip-once, alternate server)
     * - Links to provider / status pages
     *
     * @param onFinish optional callback executed when dialog is dismissed
     */
    public void show(Runnable onFinish) {
        AppDialogPresenter settingsPresenter = AppDialogPresenter.instance(getContext());

        // Compose dialog sections
        appendSponsorBlockSwitch(settingsPresenter);
        appendExcludeChannelButton(settingsPresenter);
        appendActionsSection(settingsPresenter);
        appendColorMarkersSection(settingsPresenter);
        appendMiscSection(settingsPresenter);
        appendLinks(settingsPresenter);

        // Show dialog with provider title and optional finish callback
        settingsPresenter.showDialog(getContext().getString(R.string.content_block_provider), onFinish);
    }

    /**
     * Convenience overload without finish callback.
     */
    public void show() {
        show(null);
    }

    /**
     * Append the main enable switch for the content-block feature.
     *
     * If playback is active, attempts to detect current video to offer per-channel exclusion.
     * Toggling the switch updates ContentBlockData immediately.
     */
    private void appendSponsorBlockSwitch(AppDialogPresenter settingsPresenter) {
        Video video = null;

        // Try to obtain current playback video when playback view is on top.
        if (getViewManager().getTopView() == PlaybackView.class) {
            video = PlaybackPresenter.instance(getContext()).getVideo();
        }

        final String channelId = video != null ? video.channelId : null;
        boolean isChannelExcluded = ContentBlockData.instance(getContext()).isChannelExcluded(channelId);

        OptionItem sponsorBlockOption = UiOptionItem.from(getContext().getString(R.string.enable),
                option -> {
                    // Persist enable/disable state and ensure channel exclusion is reset.
                    mContentBlockData.enableSponsorBlock(option.isSelected());
                    ContentBlockData.instance(getContext()).stopExcludingChannel(channelId);
                },
                // Default switch state: enabled only if sponsor block is enabled and channel is not excluded.
                !isChannelExcluded && mContentBlockData.isSponsorBlockEnabled()
        );

        settingsPresenter.appendSingleSwitch(sponsorBlockOption);
    }

    /**
     * Build the actions section: one entry per known segment category.
     *
     * Each category opens a nested dialog allowing user to pick what happens when such segments are found:
     * - Do nothing
     * - Skip only
     * - Skip with toast
     * - Show dialog
     *
     * Selections are persisted via ContentBlockData.persistActions callback.
     */
    private void appendActionsSection(AppDialogPresenter settingsPresenter) {
        List<OptionItem> options = new ArrayList<>();

        Set<SegmentAction> actions = mContentBlockData.getActions();

        for (SegmentAction action : actions) {
            options.add(UiOptionItem.from(
                    // Use localized category name with color marker in the label
                    getColoredString(mContentBlockData.getLocalizedRes(action.segmentCategory), mContentBlockData.getColorRes(action.segmentCategory)),
                    optionItem -> {
                        AppDialogPresenter dialogPresenter = AppDialogPresenter.instance(getContext());

                        // Build nested radio options to choose action type for this category
                        List<OptionItem> nestedOptions = new ArrayList<>();
                        nestedOptions.add(UiOptionItem.from(getContext().getString(R.string.content_block_action_none),
                                optionItem1 -> action.actionType = ContentBlockData.ACTION_DO_NOTHING,
                                action.actionType == ContentBlockData.ACTION_DO_NOTHING));
                        nestedOptions.add(UiOptionItem.from(getContext().getString(R.string.content_block_action_only_skip),
                                optionItem1 -> action.actionType = ContentBlockData.ACTION_SKIP_ONLY,
                                action.actionType == ContentBlockData.ACTION_SKIP_ONLY));
                        nestedOptions.add(UiOptionItem.from(getContext().getString(R.string.content_block_action_toast),
                                optionItem1 -> action.actionType = ContentBlockData.ACTION_SKIP_WITH_TOAST,
                                action.actionType == ContentBlockData.ACTION_SKIP_WITH_TOAST));
                        nestedOptions.add(UiOptionItem.from(getContext().getString(R.string.content_block_action_dialog),
                                optionItem1 -> action.actionType = ContentBlockData.ACTION_SHOW_DIALOG,
                                action.actionType == ContentBlockData.ACTION_SHOW_DIALOG));

                        String title = getContext().getString(mContentBlockData.getLocalizedRes(action.segmentCategory));

                        // Show nested dialog and persist changes on close
                        dialogPresenter.appendRadioCategory(title, nestedOptions);
                        dialogPresenter.showDialog(title, mContentBlockData::persistActions);
                    }));
        }

        settingsPresenter.appendStringsCategory(getContext().getString(R.string.content_block_action_type), options);
    }

    /**
     * Append toggles to enable color markers for each known segment category.
     */
    private void appendColorMarkersSection(AppDialogPresenter settingsPresenter) {
        List<OptionItem> options = new ArrayList<>();

        for (String segmentCategory : mContentBlockData.getAllCategories()) {
            options.add(UiOptionItem.from(getColoredString(mContentBlockData.getLocalizedRes(segmentCategory), mContentBlockData.getColorRes(segmentCategory)),
                    optionItem -> {
                        if (optionItem.isSelected()) {
                            mContentBlockData.enableColorMarker(segmentCategory);
                        } else {
                            mContentBlockData.disableColorMarker(segmentCategory);
                        }
                    },
                    mContentBlockData.isColorMarkerEnabled(segmentCategory)));
        }

        settingsPresenter.appendCheckedCategory(getContext().getString(R.string.sponsor_color_markers), options);
    }

    /**
     * Add provider links: status and about pages.
     */
    private void appendLinks(AppDialogPresenter settingsPresenter) {
        OptionItem statsCheckOption = UiOptionItem.from(getContext().getString(R.string.content_block_status),
                option -> Utils.openLink(getContext(), getContext().getString(R.string.content_block_status_url)));

        OptionItem webSiteOption = UiOptionItem.from(getContext().getString(R.string.about_sponsorblock),
                option -> Utils.openLink(getContext(), getContext().getString(R.string.content_block_provider_url)));

        settingsPresenter.appendSingleButton(statsCheckOption);
        settingsPresenter.appendSingleButton(webSiteOption);
    }

    /**
     * Miscellaneous toggles for content block behavior.
     *
     * Options include:
     * - Paid content notification
     * - Skip each segment only once
     * - Use alternate server for segments
     */
    private void appendMiscSection(AppDialogPresenter settingsPresenter) {
        List<OptionItem> options = new ArrayList<>();

        options.add(UiOptionItem.from(getContext().getString(R.string.paid_content_notification),
                optionItem -> mContentBlockData.enablePaidContentNotification(optionItem.isSelected()),
                mContentBlockData.isPaidContentNotificationEnabled()));

        options.add(UiOptionItem.from(getContext().getString(R.string.skip_each_segment_once),
                optionItem -> mContentBlockData.enableDontSkipSegmentAgain(optionItem.isSelected()),
                mContentBlockData.isDontSkipSegmentAgainEnabled()));

        options.add(UiOptionItem.from(getContext().getString(R.string.content_block_alt_server),
                getContext().getString(R.string.content_block_alt_server_desc),
                optionItem -> mContentBlockData.enableAltServer(optionItem.isSelected()),
                mContentBlockData.isAltServerEnabled()));

        settingsPresenter.appendCheckedCategory(getContext().getString(R.string.player_other), options);
    }

    /**
     * Append a button that allows excluding the currently playing video's channel from content blocking.
     * This button is only shown when the playback view is active and a video is available.
     */
    private void appendExcludeChannelButton(AppDialogPresenter settingsPresenter) {
        Video video = PlaybackPresenter.instance(getContext()).getVideo();

        if (video == null || getViewManager().getTopView() != PlaybackView.class) {
            return;
        }

        settingsPresenter.appendSingleButton(AppDialogUtil.createExcludeFromContentBlockButton(getContext(), video, MediaServiceManager.instance(), settingsPresenter::closeDialog));
    }

    /**
     * Helper to build a colored label for UI using a small colored dot followed by localized text.
     */
    private CharSequence getColoredString(int strResId, int colorResId) {
        String origin = getContext().getString(strResId);
        CharSequence colorMark = Utils.color("●", ContextCompat.getColor(getContext(), colorResId));
        return TextUtils.concat( colorMark, " ", origin);
    }
}
