package minefarts.smarttube.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// A {@link FrameLayout} that resizes itself to match a specified aspect ratio.
public final class AspectRatioFrameLayout extends FrameLayout {
  
    private final AspectRatioUpdateDispatcher aspectRatioUpdateDispatcher;
    @Nullable public AspectRatioListener aspectRatioListener;

    private float videoAspectRatio;
    private float scale;

    public AspectRatioFrameLayout(Context context) {
        this(context, null);
    }

    public AspectRatioFrameLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AspectRatioFrameLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        scale = -1;
        aspectRatioUpdateDispatcher = new AspectRatioUpdateDispatcher(this);
    }

    public void setAspectRatio(float widthHeightRatio) {
        if (this.videoAspectRatio != widthHeightRatio) {
            this.videoAspectRatio = widthHeightRatio;
            requestLayout();
        }
    }

    public void setZoomPercents(int percents) {
        scale = (percents / 100);
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec); // Zoom 100%
        if (videoAspectRatio <= 0) return;

        final int parentWidth = getMeasuredWidth();
        final int parentHeight = getMeasuredHeight();
        if (parentWidth <= 0 || parentHeight <= 0) return;

        // Keep the content's aspect ratio by letterboxing (never stretching).
        // We compute the maximum area that preserves videoAspectRatio inside parent bounds.
        float width = parentWidth;
        float height = parentHeight;

        float parentAspectRatio = (float) parentWidth / (float) parentHeight;

        if (videoAspectRatio > parentAspectRatio) {
            // Video is wider than the available space => limit by height.
            height = parentHeight;
            width = height * videoAspectRatio;
        } else {
            // Video is taller than the available space => limit by width.
            width = parentWidth;
            height = width / videoAspectRatio;
        }

        if (scale > 0 && scale != 1) {
            width  *= scale;
            height *= scale;
        }

        // Clamp to the parent bounds to avoid any overflow that could lead to stretch artifacts.
        width = Math.min(width, parentWidth);
        height = Math.min(height, parentHeight);

        // Use the real "natural" view aspect ratio before we change the size.
        aspectRatioUpdateDispatcher.scheduleUpdate(videoAspectRatio, parentAspectRatio);

        super.onMeasure(
            MeasureSpec.makeMeasureSpec(Math.round(width), MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(Math.round(height), MeasureSpec.EXACTLY)
        );
    }

    }


