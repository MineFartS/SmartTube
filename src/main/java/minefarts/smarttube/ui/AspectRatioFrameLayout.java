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

        float width = getMeasuredWidth();
        float height = getMeasuredHeight();
        
        final float viewAspectRatio = (float) width / height;

        if (videoAspectRatio / viewAspectRatio > 1) {
            height = (width / videoAspectRatio);
        } else {
            width  = (height * videoAspectRatio);
        }

        if (scale > 0 && scale != 1) {
            width  *= scale;
            height *= scale;
        }

        aspectRatioUpdateDispatcher.scheduleUpdate(videoAspectRatio, viewAspectRatio);

        super.onMeasure(
            MeasureSpec.makeMeasureSpec((int) width,  MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec((int) height, MeasureSpec.EXACTLY)
        );
    
    }

}

