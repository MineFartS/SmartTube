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
  
    @Nullable public AspectRatioListener aspectRatioListener;

    private float videoAspectRatio;

    public AspectRatioFrameLayout(Context context) {
        this(context, null);
    }

    public AspectRatioFrameLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AspectRatioFrameLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setAspectRatio(float widthHeightRatio) {
        if (this.videoAspectRatio != widthHeightRatio) {
            this.videoAspectRatio = widthHeightRatio;
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Aspect ratio not set.
        if (videoAspectRatio <= 0) return;

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        if (width == 0 || height == 0) return;

        if (videoAspectRatio > (width / height)) {
            height = (int) (width  / videoAspectRatio);
        } else {
            width  = (int) (height * videoAspectRatio);
        }

        // Use computed dimensions to letterbox/pillarbox the video within the parent.
        setMeasuredDimension(width, height);
    
    }

}

