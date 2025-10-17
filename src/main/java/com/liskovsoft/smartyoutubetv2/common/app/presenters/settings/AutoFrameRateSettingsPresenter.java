package com.liskovsoft.smartyoutubetv2.common.app.presenters.settings;

import android.content.Context;
import com.liskovsoft.smartyoutubetv2.common.R;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.controllers.AutoFrameRateController;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.OptionCategory;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.AppDialogPresenter;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.base.BasePresenter;
import com.liskovsoft.smartyoutubetv2.common.prefs.PlayerData;

/**
 * Presenter that builds and shows Auto Frame Rate settings dialog.
 *
 * Responsibilities:
 * - Read current auto-frame-rate preferences from PlayerData.
 * - Compose dialog categories (auto-FR enable, pause behavior, available modes)
 *   by delegating to AutoFrameRateController helper methods.
 * - Show the assembled dialog via AppDialogPresenter.
 *
 * This presenter is lightweight and created per-call via instance(context).
 */
public class AutoFrameRateSettingsPresenter extends BasePresenter<Void> {
    // Preferences helper storing player-related settings (e.g. auto-FR selection)
    private final PlayerData mPlayerData;

    public AutoFrameRateSettingsPresenter(Context context) {
        super(context);
        mPlayerData = PlayerData.instance(context);
    }

    /**
     * Factory accessor. Returns a presenter instance bound to provided context.
     * A new presenter is created each time; it does not keep a global singleton.
     */
    public static AutoFrameRateSettingsPresenter instance(Context context) {
        return new AutoFrameRateSettingsPresenter(context);
    }

    /**
     * Show the Auto Frame Rate dialog without a finish callback.
     * Delegates to show(Runnable) with an empty runnable.
     */
    public void show() {
        show(() -> {});
    }

    /**
     * Build and display the Auto Frame Rate settings dialog.
     *
     * The dialog is composed of three categories:
     * - General auto-frame-rate toggle and basic options
     * - Pause-related behavior when switching frame rates
     * - Available auto-FR modes
     *
     * @param onFinish optional callback executed when dialog is dismissed
     */
    public void show(Runnable onFinish) {
        AppDialogPresenter settingsPresenter = AppDialogPresenter.instance(getContext());

        // Populate dialog categories by using controller helpers that build OptionCategory objects.
        appendAutoFrameRateCategory(settingsPresenter);
        appendAutoFrameRatePauseCategory(settingsPresenter);
        appendAutoFrameRateModesCategory(settingsPresenter);

        // Display the dialog with localized title and optional finish callback.
        settingsPresenter.showDialog(getContext().getString(R.string.auto_frame_rate), onFinish);
    }

    /**
     * Append the main auto-frame-rate category built by AutoFrameRateController.
     * The returned OptionCategory contains switches / radio options bound to PlayerData.
     */
    private void appendAutoFrameRateCategory(AppDialogPresenter settingsPresenter) {
        OptionCategory category = AutoFrameRateController.createAutoFrameRateCategory(getContext(), mPlayerData);
        settingsPresenter.appendCategory(category);
    }

    /**
     * Append category controlling behavior when playback is paused during FR switches.
     * Uses controller helper to create the OptionCategory wired to PlayerData.
     */
    private void appendAutoFrameRatePauseCategory(AppDialogPresenter settingsPresenter) {
        OptionCategory category = AutoFrameRateController.createAutoFrameRatePauseCategory(getContext(), mPlayerData);
        settingsPresenter.appendCategory(category);
    }

    /**
     * Append category listing available Auto-FR modes (e.g. mode presets).
     * The controller builds the category; presenter only appends it to the dialog.
     */
    private void appendAutoFrameRateModesCategory(AppDialogPresenter settingsPresenter) {
        OptionCategory category = AutoFrameRateController.createAutoFrameRateModesCategory(getContext());
        settingsPresenter.appendCategory(category);
    }
}
