package minefarts.smarttube.leanback.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;

class ControlBar extends LinearLayout {

    public interface OnChildFocusedListener {
        public void onChildFocusedListener(View child, View focused);
    }

    private int mChildMarginFromCenter;
    private OnChildFocusedListener mOnChildFocusedListener;
    int mLastFocusIndex = -1;
    boolean mDefaultFocusToMiddle = true;

    public ControlBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ControlBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    void setDefaultFocusToMiddle(boolean defaultFocusToMiddle) {
        mDefaultFocusToMiddle = defaultFocusToMiddle;
    }

    int getDefaultFocusIndex() {
        return mDefaultFocusToMiddle ? getChildCount() / 2 : 0;
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        if (getChildCount() > 0) {
            int index = mLastFocusIndex >= 0 && mLastFocusIndex < getChildCount()
                    ? mLastFocusIndex : getDefaultFocusIndex();
            if (getChildAt(index).requestFocus(direction, previouslyFocusedRect)) {
                return true;
            }
        }
        return super.onRequestFocusInDescendants(direction, previouslyFocusedRect);
    }

    @Override
    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        if ((direction == ViewGroup.FOCUS_UP || direction == ViewGroup.FOCUS_DOWN)) {
            if (mLastFocusIndex >= 0 && mLastFocusIndex < getChildCount()) {
                views.add(getChildAt(mLastFocusIndex));
            } else if (getChildCount() > 0) {
                views.add(getChildAt(getDefaultFocusIndex()));
            }
        } else {
            super.addFocusables(views, direction, focusableMode);
        }
    }

    public void setOnChildFocusedListener(OnChildFocusedListener listener) {
        mOnChildFocusedListener = listener;
    }

    public void setChildMarginFromCenter(int marginFromCenter) {
        mChildMarginFromCenter = marginFromCenter;
    }

    @Override
    public void requestChildFocus (View child, View focused) {
        super.requestChildFocus(child, focused);
        mLastFocusIndex = indexOfChild(child);
        if (mOnChildFocusedListener != null) {
            mOnChildFocusedListener.onChildFocusedListener(child, focused);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mChildMarginFromCenter <= 0) return;

        int totalExtraMargin = 0;
        for (int i = 0; i < getChildCount() - 1; i++) {
            View first = getChildAt(i);
            View second = getChildAt(i+1);
            int measuredWidth = first.getMeasuredWidth() + second.getMeasuredWidth();
            int marginStart = mChildMarginFromCenter - measuredWidth / 2;
            LayoutParams lp = (LayoutParams) second.getLayoutParams();
            int extraMargin = marginStart - lp.getMarginStart();
            lp.setMarginStart(marginStart);
            second.setLayoutParams(lp);
            totalExtraMargin += extraMargin;
        }
        setMeasuredDimension(getMeasuredWidth() + totalExtraMargin, getMeasuredHeight());
    }
}
