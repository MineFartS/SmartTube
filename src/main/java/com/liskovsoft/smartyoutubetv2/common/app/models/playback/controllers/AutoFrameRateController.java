package com.liskovsoft.smartyoutubetv2.common.app.models.playback.controllers;

import android.annotation.SuppressLint;
import android.content.Context;
import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.smartyoutubetv2.common.R;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.Video;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.BasePlayerController;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.OptionCategory;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.OptionItem;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.UiOptionItem;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.AppDialogPresenter;
import com.liskovsoft.smartyoutubetv2.common.autoframerate.AutoFrameRateHelper;
import com.liskovsoft.smartyoutubetv2.common.exoplayer.selector.FormatItem;
import com.liskovsoft.smartyoutubetv2.common.autoframerate.ModeSyncManager;
import com.liskovsoft.smartyoutubetv2.common.autoframerate.internal.DisplayHolder.Mode;
import com.liskovsoft.smartyoutubetv2.common.autoframerate.internal.DisplaySyncHelper.AutoFrameRateListener;
import com.liskovsoft.smartyoutubetv2.common.autoframerate.internal.UhdHelper;
import com.liskovsoft.smartyoutubetv2.common.prefs.PlayerData;
import com.liskovsoft.smartyoutubetv2.common.utils.TvQuickActions;
import com.liskovsoft.smartyoutubetv2.common.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Controller responsible for Auto Frame Rate (AFR) behavior.
 *
 * Responsibilities:
 * - Coordinate AFR helper and mode sync manager
 * - Provide UI options for AFR configuration
 * - Apply or restore AFR depending on video playback and user preferences
 * - Handle mode switch lifecycle events and playback pauses during mode switch
 *
 * Note: This class intentionally uses delayed apply/restore to accommodate device timing issues.
 */
public class AutoFrameRateController extends BasePlayerController implements AutoFrameRateListener {
    private static final String TAG = AutoFrameRateController.class.getSimpleName();
    private static final long AUTO_FRAME_RATE_DELAY_MS = 500;
    private static final int AUTO_FRAME_RATE_ID = 21;
    private static final int AUTO_FRAME_RATE_VIDEO_PAUSE_ID = 22;
    private static final int AUTO_FRAME_RATE_MODES_ID = 23;
    private static final long SHORTS_DURATION_MIN_MS = 30 * 1_000;
    private static final long SHORTS_DURATION_MAX_MS = 61 * 1_000;

    // Helper that communicates with system/service to apply AFR settings
    private final AutoFrameRateHelper mAutoFrameRateHelper;
    // Manager that synchronizes current display mode selection with AFR state
    private final ModeSyncManager mModeSyncManager;
    // Runnables used for delayed application/stop calls
    private final Runnable mApplyAfr = this::applyAfr;
    private final Runnable mApplyAfrStop = this::applyAfrStop;

    // Remember whether playback was playing before AFR operations
    private boolean mIsPlay;

    // Controllers used to manage video state & HQ dialog UI categories
    private VideoStateController mStateController;
    private HQDialogController mHQDialogController;

    // Handler to restore playback after a delay (e.g., after mode switch)
    private final Runnable mPlaybackResumeHandler = () -> {
        if (getPlayer() != null) {
            restorePlayback();
        }
    };

    public AutoFrameRateController() {
        // Obtain singleton instances and register as listener
        mAutoFrameRateHelper = AutoFrameRateHelper.instance(null);
        mAutoFrameRateHelper.setListener(this);
        mModeSyncManager = ModeSyncManager.instance();
        mModeSyncManager.setAfrHelper(mAutoFrameRateHelper);
    }

    @Override
    public void onInit() {
        // Save current system/display state so it can be restored later
        mAutoFrameRateHelper.saveOriginalState(getActivity());
        mStateController = getController(VideoStateController.class);
        mHQDialogController = getController(HQDialogController.class);
    }

    @Override
    public void onViewResumed() {
        // Configure helper according to persisted user preferences
        mAutoFrameRateHelper.setFpsCorrectionEnabled(getPlayerData().isAfrFpsCorrectionEnabled());
        mAutoFrameRateHelper.setResolutionSwitchEnabled(getPlayerData().isAfrResSwitchEnabled(), false);
        mAutoFrameRateHelper.setDoubleRefreshRateEnabled(getPlayerData().isDoubleRefreshRateEnabled());
        mAutoFrameRateHelper.setSkip24RateEnabled(getPlayerData().isSkip24RateEnabled());

        // Update UI options in HQ dialog controller
        addUiOptions();
    }

    @Override
    public void onVideoLoaded(Video item) {
        // Save playback state and attempt to apply AFR after a short delay
        savePlayback();

        // Sometimes AFR is not working on activity startup. Trying to fix with delay.
        applyAfrDelayed();
        //applyAfr();
    }

    @Override
    public void onModeStart(Mode newMode) {
        if (getContext() == null || getPlayerData() == null) {
            return;
        }

        // Build a user-readable message for logging (Ugoos device already shows mode change)
        @SuppressLint("StringFormatMatches")
        String message = getContext().getString(
                R.string.auto_frame_rate_applying,
                newMode.getPhysicalWidth(),
                newMode.getPhysicalHeight(),
                newMode.getRefreshRate());
        Log.d(TAG, message);
        // Pause playback if configured — switching modes may cause visible glitches
        maybePausePlayback();
        getPlayerData().setAfrSwitchTimeMs(System.currentTimeMillis());
    }

    @Override
    public void onModeError(Mode newMode) {
        if (getContext() == null) {
            return;
        }

        // Log user-visible message and fall back to alternate approach (TvQuickActions)
        String msg = getContext().getString(R.string.msg_mode_switch_error, newMode != null ? UhdHelper.toResolution(newMode) : null);
        Log.e(TAG, msg);

        // Seems that the device doesn't support direct mode switching.
        // Use tvQuickActions instead.
        maybePausePlayback();
    }

    @Override
    public void onModeCancel() {
        // Restore playback state when mode change was cancelled
        restorePlayback();
    }

    @Override
    public void onEngineReleased() {
        // If player engine is released while AFR enabled — stop AFR after a small delay
        if (getPlayerData().isAfrEnabled()) {
            applyAfrStopDelayed();
        }
    }

    private void applyAfrStopDelayed() {
        Utils.postDelayed(mApplyAfrStop, 200);
    }

    private void applyAfrStop() {
        // Send data to AFR daemon via tvQuickActions app
        TvQuickActions.sendStopAFR(getContext());
    }

    private void onFpsCorrectionClick() {
        mAutoFrameRateHelper.setFpsCorrectionEnabled(getPlayerData().isAfrFpsCorrectionEnabled());
    }

    private void onResolutionSwitchClick() {
        mAutoFrameRateHelper.setResolutionSwitchEnabled(getPlayerData().isAfrResSwitchEnabled(), getPlayerData().isAfrEnabled());
    }

    private void onDoubleRefreshRateClick() {
        mAutoFrameRateHelper.setDoubleRefreshRateEnabled(getPlayerData().isDoubleRefreshRateEnabled());
    }

    public void onSkip24RateClick() {
        mAutoFrameRateHelper.setSkip24RateEnabled(getPlayerData().isSkip24RateEnabled());
    }

    private void applyAfrWrapper() {
        if (getPlayerData().isAfrEnabled()) {
            // Use dialog to show feedback while AFR is applied
            AppDialogPresenter.instance(getContext()).showDialogMessage("Applying AFR...", this::applyAfr, 1_000);
        }
    }

    /**
     * Sometimes AFR is not working on activity startup. Trying to fix with delay.
     */
    private void applyAfrDelayed() {
        Utils.postDelayed(mApplyAfr, AUTO_FRAME_RATE_DELAY_MS);
    }

    public void applyAfr() {
        if (!skipAfr() && getPlayerData().isAfrEnabled()) {
            FormatItem videoFormat = getPlayer().getVideoFormat();
            applyAfr(videoFormat, false);
            // Send data to AFR daemon via tvQuickActions app
            TvQuickActions.sendStartAFR(getContext(), videoFormat);
        } else {
            restoreAfr();
        }
    }

    private void restoreAfr() {
        String msg = "Restoring original frame rate...";
        Log.d(TAG, msg);
        mAutoFrameRateHelper.restoreOriginalState(getActivity());
        mModeSyncManager.save(null);
    }

    private void applyAfr(FormatItem videoFormat, boolean force) {
        if (videoFormat != null) {
            String msg = String.format("Applying afr... fps: %s, resolution: %sx%s, activity: %s",
                    videoFormat.getFrameRate(),
                    videoFormat.getWidth(),
                    videoFormat.getHeight(),
                    getContext().getClass().getSimpleName()
            );
            Log.d(TAG, msg);

            mAutoFrameRateHelper.apply(getActivity(), videoFormat, force);
        }
    }

    private void maybePausePlayback() {
        if (getPlayer() == null) {
            return;
        }

        int delayMs = 5_000;

        if (getPlayerData().getAfrPauseMs() > 0) {
            // Stop playback temporarily while switching display mode to avoid artifacts
            getPlayer().setPlayWhenReady(false);
            delayMs = getPlayerData().getAfrPauseMs();
        }

        Utils.postDelayed(mPlaybackResumeHandler, delayMs);
    }

    private void savePlayback() {
        if (!skipAfr() && mAutoFrameRateHelper.isSupported() && getPlayerData().isAfrEnabled() && getPlayerData().getAfrPauseMs() > 0) {
            // Block play control while AFR is about to be applied
            mStateController.blockPlay(true);
        }

        // Preserve current play state so it can be restored later
        mIsPlay = mStateController.getPlayEnabled();
    }

    private void restorePlayback() {
        // Fix restore after disable afr: don't do afr enabled check
        if (!skipAfr() && mAutoFrameRateHelper.isSupported() && getPlayerData().getAfrPauseMs() > 0) {
            mStateController.blockPlay(false);
            getPlayer().setPlayWhenReady(mIsPlay);
        }
    }

    // Avoid nested dialogs. They have problems with timings. So player controls may hide without user interaction.
    private void addUiOptions() {
        if (mAutoFrameRateHelper.isSupported() && getContext() != null) {
            OptionCategory afrCategory = createAutoFrameRateCategory(
                    getContext(), PlayerData.instance(getContext()),
                    () -> {}, this::onResolutionSwitchClick, this::onFpsCorrectionClick, this::onDoubleRefreshRateClick, this::onSkip24RateClick);

            OptionCategory afrPauseCategory = createAutoFrameRatePauseCategory(
                    getContext(), PlayerData.instance(getContext()));

            OptionCategory modesCategory = createAutoFrameRateModesCategory(getContext());

            // Create nested dialogs

            List<OptionItem> options = new ArrayList<>();
            options.add(UiOptionItem.from(afrCategory.title, optionItem -> {
                AppDialogPresenter dialogPresenter = AppDialogPresenter.instance(getContext());
                dialogPresenter.appendCategory(afrCategory);
                dialogPresenter.showDialog(afrCategory.title);
            }));
            options.add(UiOptionItem.from(afrPauseCategory.title, optionItem -> {
                AppDialogPresenter dialogPresenter = AppDialogPresenter.instance(getContext());
                dialogPresenter.appendCategory(afrPauseCategory);
                dialogPresenter.showDialog(afrPauseCategory.title);
            }));
            options.add(UiOptionItem.from(modesCategory.title, optionItem -> {
                AppDialogPresenter dialogPresenter = AppDialogPresenter.instance(getContext());
                dialogPresenter.appendCategory(modesCategory);
                dialogPresenter.showDialog(modesCategory.title);
            }));

            mHQDialogController.addCategory(OptionCategory.from(AUTO_FRAME_RATE_ID, OptionCategory.TYPE_STRING_LIST, getContext().getString(R.string.auto_frame_rate), options));
            mHQDialogController.addOnDialogHide(mApplyAfr); // Apply NEW Settings on dialog close
        } else {
            mHQDialogController.removeCategory(AUTO_FRAME_RATE_ID);
            mHQDialogController.removeOnDialogHide(mApplyAfr);
        }
    }

    public static OptionCategory createAutoFrameRateCategory(Context context, PlayerData playerData) {
        return createAutoFrameRateCategory(context, playerData, () -> {}, () -> {}, () -> {}, () -> {}, () -> {});
    }

    private static OptionCategory createAutoFrameRateCategory(Context context, PlayerData playerData,
            Runnable onAfrCallback, Runnable onResolutionCallback, Runnable onFpsCorrectionCallback,
            Runnable onDoubleRefreshRateCallback, Runnable onSkip24RateCallback) {
        String afrEnable = context.getString(R.string.auto_frame_rate);
        String afrEnableDesc = context.getString(R.string.auto_frame_rate_desc);
        String fpsCorrection = context.getString(R.string.frame_rate_correction, "24->23.97, 30->29.97, 60->59.94");
        String resolutionSwitch = context.getString(R.string.resolution_switch);
        String doubleRefreshRate = context.getString(R.string.double_refresh_rate);
        String skip24Rate = context.getString(R.string.skip_24_rate);
        String skipShorts = context.getString(R.string.skip_shorts);
        List<OptionItem> options = new ArrayList<>();

        OptionItem afrEnableOption = UiOptionItem.from(afrEnable, afrEnableDesc, optionItem -> {
            playerData.setAfrEnabled(optionItem.isSelected());
            onAfrCallback.run();
        }, playerData.isAfrEnabled());
        OptionItem afrResSwitchOption = UiOptionItem.from(resolutionSwitch, optionItem -> {
            playerData.setAfrResSwitchEnabled(optionItem.isSelected());
            onResolutionCallback.run();
        }, playerData.isAfrResSwitchEnabled());
        OptionItem afrFpsCorrectionOption = UiOptionItem.from(fpsCorrection, optionItem -> {
            playerData.setAfrFpsCorrectionEnabled(optionItem.isSelected());
            onFpsCorrectionCallback.run();
        }, playerData.isAfrFpsCorrectionEnabled());
        OptionItem doubleRefreshRateOption = UiOptionItem.from(doubleRefreshRate, optionItem -> {
            playerData.setDoubleRefreshRateEnabled(optionItem.isSelected());
            onDoubleRefreshRateCallback.run();
        }, playerData.isDoubleRefreshRateEnabled());
        OptionItem skip24RateOption = UiOptionItem.from(skip24Rate, optionItem -> {
            playerData.setSkip24RateEnabled(optionItem.isSelected());
            onSkip24RateCallback.run();
        }, playerData.isSkip24RateEnabled());
        OptionItem skipShortsOption = UiOptionItem.from(skipShorts, optionItem -> {
            playerData.setSkipShortsEnabled(optionItem.isSelected());
        }, playerData.isSkipShortsEnabled());

        afrResSwitchOption.setRequired(afrEnableOption);
        afrFpsCorrectionOption.setRequired(afrEnableOption);
        doubleRefreshRateOption.setRequired(afrEnableOption);
        skip24RateOption.setRequired(afrEnableOption);
        skipShortsOption.setRequired(afrEnableOption);

        options.add(afrEnableOption);
        options.add(afrResSwitchOption);
        options.add(afrFpsCorrectionOption);
        options.add(doubleRefreshRateOption);
        options.add(skip24RateOption);
        options.add(skipShortsOption);

        return OptionCategory.from(AUTO_FRAME_RATE_ID, OptionCategory.TYPE_CHECKBOX_LIST, afrEnable, options);
    }

    public static OptionCategory createAutoFrameRatePauseCategory(Context context, PlayerData playerData) {
        String title = context.getString(R.string.auto_frame_rate_pause);

        List<OptionItem> options = new ArrayList<>();

        for (int pauseMs : Helpers.range(0, 15_000, 250)) {
            @SuppressLint("StringFormatMatches")
            String optionTitle = pauseMs == 0 ? context.getString(R.string.option_never) : context.getString(R.string.auto_frame_rate_sec, pauseMs / 1_000f);
            options.add(UiOptionItem.from(optionTitle,
                    optionItem -> {
                        playerData.setAfrPauseMs(pauseMs);
                        playerData.setAfrEnabled(true);
                    },
                    pauseMs == playerData.getAfrPauseMs()));
        }

        return OptionCategory.from(AUTO_FRAME_RATE_VIDEO_PAUSE_ID, OptionCategory.TYPE_RADIO_LIST, title, options);
    }

    public static OptionCategory createAutoFrameRateModesCategory(Context context) {
        String title = context.getString(R.string.auto_frame_rate_modes);

        UhdHelper uhdHelper = new UhdHelper(context);

        Mode[] supportedModes = uhdHelper.getSupportedModes();
        Arrays.sort(supportedModes);

        StringBuilder result = new StringBuilder();

        for (Mode mode : supportedModes) {
            result.append(String.format("%sx%s@%s\n", mode.getPhysicalWidth(), mode.getPhysicalHeight(), mode.getRefreshRate()));
        }

        return OptionCategory.from(AUTO_FRAME_RATE_MODES_ID, OptionCategory.TYPE_LONG_TEXT, title, UiOptionItem.from(result.toString()));
    }

    private boolean skipAfr() {
        if (getPlayerData() == null || getPlayer() == null || getPlayer().getVideo() == null) {
            return true;
        }

        // NOTE: Avoid detecting shorts by Video.isShorts. Because this is working only in certain places (e.g. Shorts section).
        return isEmbedPlayer() || getPlayer().getDurationMs() <= SHORTS_DURATION_MIN_MS || isSkipShortsPrefs();
    }

    private boolean isSkipShortsPrefs() {
        // Skip AFR for short videos when user enabled skip-shorts preference
        return getPlayerData().isSkipShortsEnabled() && (getPlayer().getVideo().isShorts || getPlayer().getDurationMs() <= SHORTS_DURATION_MAX_MS);
    }
}
