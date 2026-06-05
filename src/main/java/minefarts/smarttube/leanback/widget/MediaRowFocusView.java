package minefarts.smarttube.leanback.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.RestrictTo;
import minefarts.smarttube.R;

/**
 * Creates a view for a media item row in a playlist
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
class MediaRowFocusView extends View {

    private final Paint mPaint;
    private final RectF mRoundRectF = new RectF();
    private int mRoundRectRadius;

    public MediaRowFocusView(Context context) {
        super(context);
        mPaint = createPaint(context);
    }

    public MediaRowFocusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = createPaint(context);
    }

    public MediaRowFocusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = createPaint(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mRoundRectRadius = getHeight() / 2;
        int drawHeight = 2 * mRoundRectRadius;
        int drawOffset = (drawHeight - getHeight()) / 2;
        mRoundRectF.set(0, -drawOffset, getWidth(), getHeight() + drawOffset);
        canvas.drawRoundRect(mRoundRectF, mRoundRectRadius, mRoundRectRadius, mPaint);
    }

    private Paint createPaint(Context context) {
        Paint paint = new Paint();
        paint.setColor(context.getResources().getColor(
                R.color.lb_playback_media_row_highlight_color));
        return paint;
    }

    public int getRoundRectRadius() {
        return mRoundRectRadius;
    }
}