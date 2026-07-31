package minefarts.smarttube.commons;

import android.view.View;

/**
 * Stub for chatkit DebouncedOnClickListener.
 */
public abstract class DebouncedOnClickListener implements View.OnClickListener {
    private final long mMinInterval;
    private long mLastClickTime;

    public DebouncedOnClickListener(long minIntervalMs) {
        this.mMinInterval = minIntervalMs;
    }

    public abstract void onDebouncedClick(View view);

    @Override
    public void onClick(View view) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - mLastClickTime >= mMinInterval) {
            mLastClickTime = currentTime;
            onDebouncedClick(view);
        }
    }
}
