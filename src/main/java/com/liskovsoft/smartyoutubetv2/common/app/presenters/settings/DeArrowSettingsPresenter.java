package com.liskovsoft.smartyoutubetv2.common.app.presenters.settings;

import android.content.Context;

import com.liskovsoft.smartyoutubetv2.common.R;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.OptionItem;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.UiOptionItem;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.AppDialogPresenter;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.base.BasePresenter;
import com.liskovsoft.smartyoutubetv2.common.prefs.DeArrowData;
import com.liskovsoft.smartyoutubetv2.common.prefs.MainUIData;
import com.liskovsoft.smartyoutubetv2.common.utils.ClickbaitRemover;
import com.liskovsoft.smartyoutubetv2.common.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Presenter that builds and shows the DeArrow settings dialog.
 *
 * Responsibilities:
 * - Read and persist DeArrow-related preferences (title/thumbnail replacement, thumb quality).
 * - Compose dialog sections (switches, thumbnail quality radio group, informational links).
 * - Invoke the AppDialogPresenter to display the assembled settings UI.
 *
 * Notes:
 * - This presenter does not keep a long-lived singleton; a new instance is created via instance().
 * - Changes are persisted immediately via MainUIData / DeArrowData setters.
 */
public class DeArrowSettingsPresenter extends BasePresenter<Void> {
    private final MainUIData mMainUIData;
    private final DeArrowData mDeArrowData;

    private DeArrowSettingsPresenter(Context context) {
        super(context);
        mMainUIData = MainUIData.instance(context);
        mDeArrowData = DeArrowData.instance(context);
    }

    /**
     * Factory-style accessor. Returns a presenter tied to provided context.
     */
    public static DeArrowSettingsPresenter instance(Context context) {
        return new DeArrowSettingsPresenter(context);
    }

    /**
     * Build and show the DeArrow settings dialog.
     *
     * @param onFinish optional callback executed when dialog is dismissed
     */
    public void show(Runnable onFinish) {
        AppDialogPresenter settingsPresenter = AppDialogPresenter.instance(getContext());

        // Build UI sections
        appendSwitches(settingsPresenter);
        appendThumbQuality(settingsPresenter);
        appendLinks(settingsPresenter);

        settingsPresenter.showDialog(getContext().getString(R.string.dearrow_provider), onFinish);
    }

    /**
     * Convenience overload without finish callback.
     */
    public void show() {
        show(null);
    }

    /**
     * Append thumbnail quality radio options.
     * Persists selection to MainUIData.setThumbQuality().
     */
    private void appendThumbQuality(AppDialogPresenter settingsPresenter) {
        List<OptionItem> options = new ArrayList<>();

        for (int[] pair : new int[][] {
                {R.string.thumb_quality_default, ClickbaitRemover.THUMB_QUALITY_DEFAULT},
                {R.string.thumb_quality_start, ClickbaitRemover.THUMB_QUALITY_START},
                {R.string.thumb_quality_middle, ClickbaitRemover.THUMB_QUALITY_MIDDLE},
                {R.string.thumb_quality_end, ClickbaitRemover.THUMB_QUALITY_END}}) {
            options.add(UiOptionItem.from(getContext().getString(pair[0]),
                    optionItem -> mMainUIData.setThumbQuality(pair[1]),
                    mMainUIData.getThumbQuality() == pair[1]
            ));
        }

        settingsPresenter.appendRadioCategory(getContext().getString(R.string.dearrow_not_submitted_thumbs), options);
    }

    /**
     * Append primary toggle switches for title/thumbnail replacement options.
     *
     * Behavior:
     * - Ensures mutually exclusive title sources: enabling crowdsourced titles disables unlocalized titles and vice versa.
     * - Persisted immediately when a switch is toggled.
     */
    private void appendSwitches(AppDialogPresenter settingsPresenter) {
        List<OptionItem> options = new ArrayList<>();

        options.add(UiOptionItem.from(getContext().getString(R.string.card_unlocalized_titles),
                option -> {
                    mMainUIData.setUnlocalizedTitlesEnabled(option.isSelected());
                    // When enabling unlocalized titles, disable crowdsourced replacement
                    mDeArrowData.setReplaceTitlesEnabled(false);
                },
                mMainUIData.isUnlocalizedTitlesEnabled()));

        options.add(UiOptionItem.from(getContext().getString(R.string.crowdsoursed_titles),
                optionItem -> {
                    mDeArrowData.setReplaceTitlesEnabled(optionItem.isSelected());
                    // When enabling crowdsourced replacements, disable unlocalized titles
                    mMainUIData.setUnlocalizedTitlesEnabled(false);
                },
                mDeArrowData.isReplaceTitlesEnabled()));

        options.add(UiOptionItem.from(getContext().getString(R.string.crowdsourced_thumbnails),
                optionItem -> mDeArrowData.setReplaceThumbnailsEnabled(optionItem.isSelected()),
                mDeArrowData.isReplaceThumbnailsEnabled()));

        for (OptionItem item : options) {
            settingsPresenter.appendSingleSwitch(item);
        }
    }

    /**
     * Append informational links/buttons to the dialog (status page and provider site).
     * Opens external links using Utils.openLink().
     */
    private void appendLinks(AppDialogPresenter settingsPresenter) {
        OptionItem statsCheckOption = UiOptionItem.from(getContext().getString(R.string.dearrow_status),
                option -> Utils.openLink(getContext(), getContext().getString(R.string.dearrow_status_url)));

        OptionItem webSiteOption = UiOptionItem.from(getContext().getString(R.string.about_dearrow),
                option -> Utils.openLink(getContext(), getContext().getString(R.string.dearrow_provider_url)));

        settingsPresenter.appendSingleButton(statsCheckOption);
        settingsPresenter.appendSingleButton(webSiteOption);
    }

    /**
     * Helper that appends a switch controlling the global DeArrow provider enable flag.
     * Not used by default UI but kept for completeness.
     */
    private void appendDeArrowSwitch(AppDialogPresenter settingsPresenter) {
        String title = String.format(
                "%s (%s)",
                getContext().getString(R.string.dearrow_provider),
                getContext().getString(R.string.dearrow_provider_url)
        );
        OptionItem sponsorBlockOption = UiOptionItem.from(title,
                option -> mDeArrowData.setDeArrowEnabled(option.isSelected()),
                mDeArrowData.isDeArrowEnabled()
        );

        settingsPresenter.appendSingleSwitch(sponsorBlockOption);
    }
}
