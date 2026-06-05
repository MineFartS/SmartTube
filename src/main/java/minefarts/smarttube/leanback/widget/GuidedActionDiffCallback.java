package minefarts.smarttube.leanback.widget;

import android.text.TextUtils;

import androidx.annotation.NonNull;

/**
 * DiffCallback used for GuidedActions, see {@link
 * minefarts.smarttube.leanback.app.GuidedStepSupportFragment#setActionsDiffCallback(DiffCallback)}.
 */
public class GuidedActionDiffCallback extends DiffCallback<GuidedAction> {

    static final GuidedActionDiffCallback sInstance = new GuidedActionDiffCallback();

    /**
     * Returns the singleton GuidedActionDiffCallback.
     * @return The singleton GuidedActionDiffCallback.
     */
    public static GuidedActionDiffCallback getInstance() {
        return sInstance;
    }

    @Override
    public boolean areItemsTheSame(@NonNull GuidedAction oldItem, @NonNull GuidedAction newItem) {
        if (oldItem == null) {
            return newItem == null;
        } else if (newItem == null) {
            return false;
        }
        return oldItem.getId() == newItem.getId();
    }

    @Override
    public boolean areContentsTheSame(@NonNull GuidedAction oldItem,
            @NonNull GuidedAction newItem) {
        if (oldItem == null) {
            return newItem == null;
        } else if (newItem == null) {
            return false;
        }
        return oldItem.getCheckSetId() == newItem.getCheckSetId()
                && oldItem.mActionFlags == newItem.mActionFlags
                && TextUtils.equals(oldItem.getTitle(), newItem.getTitle())
                && TextUtils.equals(oldItem.getDescription(), newItem.getDescription())
                && oldItem.getInputType() == newItem.getInputType()
                && TextUtils.equals(oldItem.getEditTitle(), newItem.getEditTitle())
                && TextUtils.equals(oldItem.getEditDescription(), newItem.getEditDescription())
                && oldItem.getEditInputType() == newItem.getEditInputType()
                && oldItem.getDescriptionEditInputType() == newItem.getDescriptionEditInputType();
    }
}
