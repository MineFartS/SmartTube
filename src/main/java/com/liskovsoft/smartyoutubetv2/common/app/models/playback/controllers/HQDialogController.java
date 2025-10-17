package com.liskovsoft.smartyoutubetv2.common.app.models.playback.controllers;

import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import com.liskovsoft.smartyoutubetv2.common.R;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.BasePlayerController;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.OptionCategory;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.OptionItem;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.UiOptionItem;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.AppDialogPresenter;
import com.liskovsoft.smartyoutubetv2.common.exoplayer.selector.FormatItem;
import com.liskovsoft.smartyoutubetv2.common.utils.AppDialogUtil;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Controller responsible for presenting and handling High Quality / Playback settings dialog.
 *
 * Responsibilities:
 * - Build dialog categories for video/audio formats, presets, zoom, buffering, and other playback tweaks.
 * - Persist format selection and apply changes to the player.
 * - Notify registered listeners when the dialog is hidden so other controllers can react.
 *
 * Note: This class orchestrates AppDialogPresenter usage and keeps dialog-related categories in maps
 * so categories can be added/removed dynamically.
 */
public class HQDialogController extends BasePlayerController {
    private static final String TAG = HQDialogController.class.getSimpleName();
    private static final int VIDEO_FORMATS_ID = 132;
    private static final int AUDIO_FORMATS_ID = 133;

    // Categories stored in insertion order; separate maps allow temporary internal categories vs public ones.
    private final Map<Integer, OptionCategory> mCategories = new LinkedHashMap<>();
    private final Map<Integer, OptionCategory> mCategoriesInt = new LinkedHashMap<>();

    // Set of listeners invoked when HQ dialog is hidden (used by other controllers to react to changes).
    private final Set<Runnable> mHideListeners = new HashSet<>();

    // Reference to the shared AppDialogPresenter used to show/append categories.
    private AppDialogPresenter mAppDialogPresenter;

    @Override
    public void onInit() {
        // Obtain AppDialogPresenter for the current context
        mAppDialogPresenter = AppDialogPresenter.instance(getContext());
    }

    @Override
    public void onViewResumed() {
        // reserved for potential future updates related to background playback
        //updateBackgroundPlayback();
    }

    @Override
    public void onButtonClicked(int buttonId, int buttonState) {
        if (buttonId == R.id.lb_control_high_quality) {
            onHighQualityClicked();
        }
    }

    /**
     * Entry point when user requests high-quality playback settings.
     * Prepares dialog categories and shows the dialog.
     */
    private void onHighQualityClicked() {
        // Ensure video fits properly into dialog before showing options
        fitVideoIntoDialog();

        // Build categories
        addQualityCategories();
        addAudioLanguage();
        addPresetsCategory();
        addVideoZoomCategory();
        addNetworkEngine();
        addVideoBufferCategory();
        addAudioDelayCategory();
        addPitchEffectCategory();
        //addBackgroundPlaybackCategory();

        // Append internal and external categories to dialog presenter
        appendOptions(mCategoriesInt);
        appendOptions(mCategories);

        // Show dialog and register hide callback
        mAppDialogPresenter.showDialog(getContext().getString(R.string.playback_settings), this::onDialogHide);
    }

    private void addQualityCategories() {
        List<FormatItem> videoFormats = getPlayer().getVideoFormats();
        String videoFormatsTitle = getContext().getString(R.string.title_video_formats);

        List<FormatItem> audioFormats = getPlayer().getAudioFormats();
        String audioFormatsTitle = getContext().getString(R.string.title_audio_formats);

        addCategoryInt(OptionCategory.from(
                VIDEO_FORMATS_ID,
                OptionCategory.TYPE_RADIO_LIST,
                videoFormatsTitle,
                UiOptionItem.from(videoFormats, this::selectFormatOption, getContext().getString(R.string.option_disabled))));
        addCategoryInt(OptionCategory.from(
                AUDIO_FORMATS_ID,
                OptionCategory.TYPE_RADIO_LIST,
                audioFormatsTitle,
                UiOptionItem.from(audioFormats, this::selectFormatOption, getContext().getString(R.string.option_disabled))));
    }

    /**
     * Called when user selects a format option (video/audio).
     * Applies format to player and persists selection according to preset rules.
     */
    private void selectFormatOption(OptionItem option) {
        FormatItem formatItem = UiOptionItem.toFormat(option);
        getPlayer().setFormat(formatItem);
        persistFormat(formatItem);

        if (getPlayerData().getFormat(formatItem.getType()).isPreset()) {
            // Preset currently active. Show warning about format reset.
            MessageHelpers.showMessage(getContext(), R.string.video_preset_enabled);
        }

        if (!getPlayer().containsMedia()) {
            getPlayer().reloadPlayback();
        }

        // Make result easily be spotted by the user for video format changes
        if (formatItem.getType() == FormatItem.TYPE_VIDEO) {
            getPlayer().showOverlay(false);
        }
    }

    private void persistFormat(FormatItem formatItem) {
        if (formatItem.getType() == FormatItem.TYPE_VIDEO) {
            if (!getPlayerData().getFormat(FormatItem.TYPE_VIDEO).isPreset()) {
                // Persist permanent selection
                getPlayerData().setFormat(formatItem);
            } else {
                // Use temporary format for preset mode
                getPlayerData().setTempVideoFormat(formatItem);
            }
        } else {
            getPlayerData().setFormat(formatItem);
        }
    }

    private void addVideoBufferCategory() {
        addCategoryInt(AppDialogUtil.createVideoBufferCategory(getContext(),
                () -> getPlayer().restartEngine()));
    }

    private void addAudioDelayCategory() {
        addCategoryInt(AppDialogUtil.createAudioShiftCategory(getContext(),
                () -> getPlayer().restartEngine()));
    }

    private void addPitchEffectCategory() {
        addCategoryInt(AppDialogUtil.createPitchEffectCategory(getContext()));
    }

    private void addAudioLanguage() {
        addCategoryInt(AppDialogUtil.createAudioLanguageCategory(getContext(),
                () -> getPlayer().restartEngine()));
    }

    private void addNetworkEngine() {
        addCategoryInt(AppDialogUtil.createNetworkEngineCategory(getContext(),
                () -> getPlayer().restartEngine()));
    }

    /**
     * Called when the dialog is hidden. Notifies registered listeners so they can apply changes.
     */
    private void onDialogHide() {
        //updateBackgroundPlayback();

        for (Runnable listener : mHideListeners) {
            listener.run();
        }
    }

    //private void updateBackgroundPlayback() {
    //    ViewManager.instance(getContext()).blockTop(null);
    //
    //    if (getPlayer() != null) {
    //        getPlayer().setBackgroundMode(getPlayerData().getBackgroundMode());
    //    }
    //}

    //private void addBackgroundPlaybackCategory() {
    //    OptionCategory category =
    //            AppDialogUtil.createBackgroundPlaybackCategory(getContext(), getPlayerData(), GeneralData.instance(getContext()), this::updateBackgroundPlayback);
    //
    //    addCategoryInt(category);
    //}

    private void addPresetsCategory() {
        addCategoryInt(AppDialogUtil.createVideoPresetsCategory(
                getContext(), () -> {
                    if (getPlayer() == null) {
                        return;
                    }

                    FormatItem format = getPlayerData().getFormat(FormatItem.TYPE_VIDEO);
                    getPlayer().setFormat(format);

                    if (!getPlayer().containsMedia()) {
                        getPlayer().reloadPlayback();
                    }

                    // Make result easily be spotted by the user
                    getPlayer().showOverlay(false);
                }
        ));
    }

    private void addVideoZoomCategory() {
        addCategoryInt(AppDialogUtil.createVideoZoomCategory(
                getContext(), () -> {
                    getPlayer().setResizeMode(getPlayerData().getResizeMode());
                    getPlayer().setZoomPercents(getPlayerData().getZoomPercents());

                    // Make result easily be spotted by the user
                    getPlayer().showOverlay(false);
                }));
    }

    private void removeCategoryInt(int id) {
        mCategoriesInt.remove(id);
    }

    private void addCategoryInt(OptionCategory category) {
        mCategoriesInt.put(category.id, category);
    }

    public void removeCategory(int id) {
        mCategories.remove(id);
    }

    public void addCategory(OptionCategory category) {
        mCategories.put(category.id, category);
    }

    /**
     * Register a listener to be called when the HQ dialog is hidden.
     *
     * @param listener Runnable to invoke on dialog hide
     */
    public void addOnDialogHide(Runnable listener) {
        mHideListeners.add(listener);
    }

    public void removeOnDialogHide(Runnable listener) {
        mHideListeners.remove(listener);
    }

    private void appendOptions(Map<Integer, OptionCategory> categories) {
        for (OptionCategory category : categories.values()) {
            mAppDialogPresenter.appendCategory(category);
        }
    }
}
