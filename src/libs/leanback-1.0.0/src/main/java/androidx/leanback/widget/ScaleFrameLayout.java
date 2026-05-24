package androidx.leanback.widget;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.RestrictTo;

/**
 * Subclass of FrameLayout that support scale layout area size for children.
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class ScaleFrameLayout extends FrameLayout {

    private static final int DEFAULT_CHILD_GRAVITY = Gravity.TOP | Gravity.START;

    private float mLayoutScaleX = 1f;
    private float mLayoutScaleY = 1f;

    private float mChildScale = 1f;
    private float mAspectRatio = 0f;

    public ScaleFrameLayout(Context context) {
        this(context ,null);
    }

    public ScaleFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScaleFrameLayout(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setLayoutScaleX(float scaleX) {
        if (scaleX != mLayoutScaleX) {
            mLayoutScaleX = scaleX;
            requestLayout();
        }
    }

    public void setLayoutScaleY(float scaleY) {
        if (scaleY != mLayoutScaleY) {
            mLayoutScaleY = scaleY;
            requestLayout();
        }
    }

    public void setChildScale(float scale) {
        if (mChildScale != scale) {
            mChildScale = scale;
            for (int i = 0; i < getChildCount(); i++) {
                getChildAt(i).setScaleX(scale);
                getChildAt(i).setScaleY(scale);
            }
        }
    }

    public void setAspectRatio(float aspectRatio) {
        if (mAspectRatio != aspectRatio) {
            mAspectRatio = aspectRatio;
            requestLayout();
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        child.setScaleX(mChildScale);
        child.setScaleY(mChildScale);
    }

    @Override
    protected boolean addViewInLayout (View child, int index, ViewGroup.LayoutParams params,
            boolean preventRequestLayout) {
        boolean ret = super.addViewInLayout(child, index, params, preventRequestLayout);
        if (ret) {
            child.setScaleX(mChildScale);
            child.setScaleY(mChildScale);
        }
        return ret;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int count = getChildCount();

        final int parentLeft, parentRight;
        final int layoutDirection = getLayoutDirection();
        final float pivotX = (layoutDirection == View.LAYOUT_DIRECTION_RTL)
                ? getWidth() - getPivotX()
                : getPivotX();
        if (mLayoutScaleX != 1f) {
            parentLeft = getPaddingLeft() + (int)(pivotX - pivotX / mLayoutScaleX + 0.5f);
            parentRight = (int)(pivotX + (right - left - pivotX) / mLayoutScaleX + 0.5f)
                    - getPaddingRight();
        } else {
            parentLeft = getPaddingLeft();
            parentRight = right - left - getPaddingRight();
        }

        final int parentTop, parentBottom;
        final float pivotY = getPivotY();
        if (mLayoutScaleY != 1f) {
            parentTop = getPaddingTop() + (int)(pivotY - pivotY / mLayoutScaleY + 0.5f);
            parentBottom = (int)(pivotY + (bottom - top - pivotY) / mLayoutScaleY + 0.5f)
                    - getPaddingBottom();
        } else {
            parentTop = getPaddingTop();
            parentBottom = bottom - top - getPaddingBottom();
        }

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();

                final int width = child.getMeasuredWidth();
                final int height = child.getMeasuredHeight();

                int childLeft;
                int childTop;

                int gravity = lp.gravity;
                if (gravity == -1) {
                    gravity = DEFAULT_CHILD_GRAVITY;
                }

                final int absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection);
                final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

                switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                    case Gravity.CENTER_HORIZONTAL:
                        childLeft = parentLeft + (parentRight - parentLeft - width) / 2
                                + lp.leftMargin - lp.rightMargin;
                        break;
                    case Gravity.RIGHT:
                        childLeft = parentRight - width - lp.rightMargin;
                        break;
                    case Gravity.LEFT:
                    default:
                        childLeft = parentLeft + lp.leftMargin;
                }

                switch (verticalGravity) {
                    case Gravity.TOP:
                        childTop = parentTop + lp.topMargin;
                        break;
                    case Gravity.CENTER_VERTICAL:
                        childTop = parentTop + (parentBottom - parentTop - height) / 2
                                + lp.topMargin - lp.bottomMargin;
                        break;
                    case Gravity.BOTTOM:
                        childTop = parentBottom - height - lp.bottomMargin;
                        break;
                    default:
                        childTop = parentTop + lp.topMargin;
                }

                child.layout(childLeft, childTop, childLeft + width, childTop + height);
                // synchronize child pivot to be same as ScaleFrameLayout's pivot
                child.setPivotX(pivotX - childLeft);
                child.setPivotY(pivotY - childTop);
            }
        }
    }

    private static int getScaledMeasureSpec(int measureSpec, float scale) {
        return scale == 1f ? measureSpec : MeasureSpec.makeMeasureSpec(
                (int) (MeasureSpec.getSize(measureSpec) / scale + 0.5f),
                MeasureSpec.getMode(measureSpec));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mAspectRatio > 0f) {
            final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

            if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
                int height = (int) (widthSize / mAspectRatio + 0.5f);
                if (heightMode == MeasureSpec.AT_MOST && height > heightSize) {
                    height = heightSize;
                }
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
            } else if (heightMode == MeasureSpec.EXACTLY && widthMode != MeasureSpec.EXACTLY) {
                int width = (int) (heightSize * mAspectRatio + 0.5f);
                if (widthMode == MeasureSpec.AT_MOST && width > widthSize) {
                    width = widthSize;
                }
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
            } else if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
                int width = widthSize;
                int height = (int) (width / mAspectRatio + 0.5f);
                if (height > heightSize) {
                    height = heightSize;
                    width = (int) (height * mAspectRatio + 0.5f);
                }
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
            }
        }

        if (mLayoutScaleX != 1f || mLayoutScaleY != 1f) {
            final int scaledWidthMeasureSpec =
                    getScaledMeasureSpec(widthMeasureSpec, mLayoutScaleX);
            final int scaledHeightMeasureSpec =
                    getScaledMeasureSpec(heightMeasureSpec, mLayoutScaleY);
            super.onMeasure(scaledWidthMeasureSpec, scaledHeightMeasureSpec);
            setMeasuredDimension((int)(getMeasuredWidth() * mLayoutScaleX + 0.5f),
                    (int)(getMeasuredHeight() * mLayoutScaleY + 0.5f));
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    /**
     * setForeground() is not supported,  throws UnsupportedOperationException() when called.
     */
    @Override
    public void setForeground(Drawable d) {
        throw new UnsupportedOperationException();
    }

}
