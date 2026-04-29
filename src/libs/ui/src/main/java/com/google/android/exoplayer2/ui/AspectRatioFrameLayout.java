
package com.google.android.exoplayer2.ui;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A {@link FrameLayout} that resizes itself to match a specified aspect ratio.
 */
public final class AspectRatioFrameLayout extends FrameLayout {

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
        float targetAspectRatio, float naturalAspectRatio, boolean aspectRatioMismatch);
  }

  private static final float MAX_ASPECT_RATIO_DEFORMATION_FRACTION = 0.01f;

  private final AspectRatioUpdateDispatcher aspectRatioUpdateDispatcher;

  @Nullable private AspectRatioListener aspectRatioListener;

  private float videoAspectRatio;

  private int zoomPercents;

  public AspectRatioFrameLayout(Context context) {
    this(context, /* attrs= */ null);
  }

  public AspectRatioFrameLayout(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    zoomPercents = -1;
    aspectRatioUpdateDispatcher = new AspectRatioUpdateDispatcher();
  }

  /**
   * Sets the aspect ratio that this view should satisfy.
   *
   * @param widthHeightRatio The width to height ratio.
   */
  public void setAspectRatio(float widthHeightRatio) {
    if (this.videoAspectRatio != widthHeightRatio) {
      this.videoAspectRatio = widthHeightRatio;
      requestLayout();
    }
  }

  /**
   * Sets the {@link AspectRatioListener}.
   *
   * @param listener The listener to be notified about aspect ratios changes, or null to clear a
   *     listener that was previously set.
   */
  public void setAspectRatioListener(@Nullable AspectRatioListener listener) {
    this.aspectRatioListener = listener;
  }

  /**
   * MODIFIED: Set video zoom in percents
   */
  public void setZoom(int percents) {
    if (zoomPercents != percents) {
      zoomPercents = percents;
      requestLayout();
    }
  }

  @Override
  protected void onMeasure(
    int widthMeasureSpec,
    int heightMeasureSpec
) {

    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    
    // Aspect ratio not set.
    if (videoAspectRatio <= 0) return;

    int width = getMeasuredWidth();
    int height = getMeasuredHeight();

    float viewAspectRatio = (float) width / height;

    if (videoAspectRatio / viewAspectRatio > 1) {
        height = (int) (width / videoAspectRatio);
    } else {
        width  = (int) (height * videoAspectRatio);
    }

    aspectRatioUpdateDispatcher.scheduleUpdate(
        videoAspectRatio, 
        viewAspectRatio, 
        true
    );
    
    super.onMeasure(
        MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
        MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
    );

  }

  /** Dispatches updates to {@link AspectRatioListener}. */
  private final class AspectRatioUpdateDispatcher implements Runnable {

    private float targetAspectRatio;
    private float naturalAspectRatio;
    private boolean aspectRatioMismatch;
    private boolean isScheduled;

    public void scheduleUpdate(
        float targetAspectRatio, float naturalAspectRatio, boolean aspectRatioMismatch) {
      this.targetAspectRatio = targetAspectRatio;
      this.naturalAspectRatio = naturalAspectRatio;
      this.aspectRatioMismatch = aspectRatioMismatch;

      if (!isScheduled) {
        isScheduled = true;
        post(this);
      }
    }

    @Override
    public void run() {
      isScheduled = false;
      if (aspectRatioListener == null) {
        return;
      }
      aspectRatioListener.onAspectRatioUpdated(
          targetAspectRatio, naturalAspectRatio, aspectRatioMismatch);
    }
  }
}
