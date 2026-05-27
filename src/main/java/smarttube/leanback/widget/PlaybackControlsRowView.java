package minefarts.smarttube.leanback.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;

/**
 * A LinearLayout that preserves the focused child view.
 */
class PlaybackControlsRowView extends LinearLayout {
    public interface OnUnhandledKeyListener {
        /**
         * Returns true if the key event should be consumed.
         */
        public boolean onUnhandledKey(KeyEvent event);
    }

    private OnUnhandledKeyListener mOnUnhandledKeyListener;

    public PlaybackControlsRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlaybackControlsRowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOnUnhandledKeyListener(OnUnhandledKeyListener listener) {
         mOnUnhandledKeyListener = listener;
    }

    public OnUnhandledKeyListener getOnUnhandledKeyListener() {
        return mOnUnhandledKeyListener;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (super.dispatchKeyEvent(event)) {
            return true;
        }
        return mOnUnhandledKeyListener != null && mOnUnhandledKeyListener.onUnhandledKey(event);
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        final View focused = findFocus();
        if (focused != null && focused.requestFocus(direction, previouslyFocusedRect)) {
            return true;
        }
        return super.onRequestFocusInDescendants(direction, previouslyFocusedRect);
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }
}
