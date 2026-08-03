package minefarts.smarttube.ui;

/** Listener to be notified about changes of the aspect ratios of this view. */
public interface AspectRatioListener {

    /**
     * Called when either the target aspect ratio or the view aspect ratio is updated.
     *
     * @param targetAspectRatio The aspect ratio that has been set in {@link #setAspectRatio(float)}
     * @param naturalAspectRatio The natural aspect ratio of this view (before its width and height
     *     are modified to satisfy the target aspect ratio).
     * @param aspectRatioMismatch Whether the target and natural aspect ratios differ enough for
     *     changing the resize mode to have an effect.
     */
    void onAspectRatioUpdated(
        float targetAspectRatio, 
        float naturalAspectRatio, 
        boolean aspectRatioMismatch
    );

}