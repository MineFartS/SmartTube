package SmartTubeApp.app.models.playback.controllers;

import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import SmartTubeApp.R;
import SmartTubeApp.app.models.playback.BasePlayerController;
import SmartTubeApp.app.models.playback.ui.OptionCategory;
import SmartTubeApp.app.models.playback.ui.OptionItem;
import SmartTubeApp.app.models.playback.ui.UiOptionItem;
import SmartTubeApp.app.presenters.AppDialogPresenter;
import SmartTubeApp.exoplayer.selector.FormatItem;
import SmartTubeApp.utils.AppDialogUtil;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HQDialogController extends BasePlayerController {

    private static final int VIDEO_FORMATS_ID = 132;
    private static final int AUDIO_FORMATS_ID = 133;
    // NOTE: using map, because same item could be changed time to time
    private final Map<Integer, OptionCategory> mCategories = new LinkedHashMap<>();
    private final Map<Integer, OptionCategory> mCategoriesInt = new LinkedHashMap<>();
    private final Set<Runnable> mHideListeners = new HashSet<>();
    private AppDialogPresenter mAppDialogPresenter;

    @Override
    public void onInit() {
        mAppDialogPresenter = AppDialogPresenter.instance(getContext());
    }

    @Override
    public void onViewResumed() {
        //updateBackgroundPlayback();
    }

    @Override
    public void onButtonClicked(int buttonId, int buttonState) {
        if (buttonId == R.id.lb_control_high_quality) {
            onHighQualityClicked();
        }
    }

    private void onHighQualityClicked() {

        fitVideoIntoDialog();

        addCategoryInt(OptionCategory.from(
            VIDEO_FORMATS_ID,
            OptionCategory.TYPE_RADIO_LIST,
            "Video formats",
            UiOptionItem.from(
                getPlayer().getVideoFormats(), 
                this::selectFormatOption, 
                getContext().getString(R.string.option_disabled)
            )
        ));
        
        addCategoryInt(OptionCategory.from(
            AUDIO_FORMATS_ID,
            OptionCategory.TYPE_RADIO_LIST,
            "Audio formats",
            UiOptionItem.from(
                getPlayer().getAudioFormats(), 
                this::selectFormatOption, 
                getContext().getString(R.string.option_disabled)
            )
        ));

        addAudioLanguage();
        addPresetsCategory();

        appendOptions(mCategoriesInt);
        appendOptions(mCategories);

        mAppDialogPresenter.showDialog(
            getContext().getString(R.string.playback_settings), 
            this::onDialogHide
        );
        
    }

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

        // Make result easily be spotted by the user
        if (formatItem.getType() == FormatItem.TYPE_VIDEO) {
            getPlayer().showOverlay(false);
        }
    }

    private void persistFormat(FormatItem formatItem) {
        if (formatItem.getType() == FormatItem.TYPE_VIDEO) {
            if (!getPlayerData().getFormat(FormatItem.TYPE_VIDEO).isPreset()) {
                getPlayerData().setFormat(formatItem);
            } else {
                getPlayerData().setTempVideoFormat(formatItem);
            }
        } else {
            getPlayerData().setFormat(formatItem);
        }
    }

    private void addAudioLanguage() {
        addCategoryInt(AppDialogUtil.createAudioLanguageCategory(getContext(),
                () -> getPlayer().restartEngine()));
    }

    private void onDialogHide() {
        for (Runnable listener : mHideListeners) {
            listener.run();
        }
    }

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

    private void addCategoryInt(OptionCategory category) {
        mCategoriesInt.put(category.id, category);
    }

    public void removeCategory(int id) {
        mCategories.remove(id);
    }

    public void addCategory(OptionCategory category) {
        mCategories.put(category.id, category);
    }

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
