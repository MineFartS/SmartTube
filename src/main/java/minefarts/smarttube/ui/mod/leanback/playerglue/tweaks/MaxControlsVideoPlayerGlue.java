package minefarts.smarttube.ui.mod.leanback.playerglue.tweaks;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import minefarts.smarttube.leanback.media.PlayerAdapter;
import minefarts.smarttube.leanback.widget.AbstractDetailsDescriptionPresenter;
import minefarts.smarttube.leanback.widget.PlaybackControlsRow;
import minefarts.smarttube.leanback.widget.PlaybackRowPresenter;
import minefarts.smarttube.leanback.media.PlaybackBaseControlGlue;
import minefarts.smarttube.leanback.widget.RowPresenter;
import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.app.models.playback.ui.SeekBarSegment;
import minefarts.smarttube.leanback.media.PlaybackTransportControlGlue;
import minefarts.smarttube.ui.mod.leanback.playerglue.tweaks.PlaybackTransportRowPresenter.TopEdgeFocusListener;
import minefarts.smarttube.ui.mod.leanback.playerglue.tweaks.PlaybackTransportRowPresenter.ViewHolder;

import java.lang.ref.WeakReference;
import java.util.List;

public abstract class MaxControlsVideoPlayerGlue<T extends PlayerAdapter>
        extends PlaybackTransportControlGlue<T> implements TopEdgeFocusListener {

    private WeakReference<PlaybackTransportRowPresenter.ViewHolder> mTransportViewHolder;
    private WeakReference<AbstractDetailsDescriptionPresenter.ViewHolder> mDescriptionViewHolder;

    /**
     * Constructor for the glue.
     *
     * @param context context
     * @param impl    Implementation to underlying media player.
     */
    public MaxControlsVideoPlayerGlue(Context context, T impl) {
        super(context, impl);
    }

    @Override
    protected PlaybackRowPresenter onCreateRowPresenter() {
        final AbstractDetailsDescriptionPresenter detailsPresenter =
                new AbstractDetailsDescriptionPresenter() {
                    @Override
                    protected void onBindDescription(ViewHolder viewHolder, Object obj) {
                        mDescriptionViewHolder = new WeakReference<>(viewHolder);

                        fixClippedTitle(viewHolder);

                        fixThumbOverlapping(viewHolder);

                        PlaybackBaseControlGlue<?> glue = (PlaybackBaseControlGlue<?>) obj;
                        viewHolder.getTitle().setText(glue.getTitle());
                        viewHolder.getSubtitle().setText(glue.getSubtitle());

                    }

                    private void fixClippedTitle(ViewHolder viewHolder) {
                        // Fix clipped title on videos with embedded icons
                        Helpers.setField(viewHolder, "mTitleMargin", 0);
                    }

                    /**
                     * MOD: Also fixes cropped title, subtitle, body
                     */
                    private void fixThumbOverlapping(ViewHolder viewHolder) {
                        LinearLayout.LayoutParams textParam = new LinearLayout.LayoutParams
                                (LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);

                        viewHolder.getTitle().setLayoutParams(textParam);
                        viewHolder.getSubtitle().setLayoutParams(textParam);
                        viewHolder.getBody().setLayoutParams(textParam);
                    }
                };

        PlaybackTransportRowPresenter rowPresenter = new PlaybackTransportRowPresenter() {

            @Override
            protected void onBindRowViewHolder(RowPresenter.ViewHolder vh, Object item) {

                super.onBindRowViewHolder(vh, item);

                vh.setOnKeyListener(MaxControlsVideoPlayerGlue.this);

                ViewHolder viewHolder = (ViewHolder) vh;

                mTransportViewHolder = new WeakReference<>(viewHolder);

                viewHolder.setTopEdgeFocusListener(MaxControlsVideoPlayerGlue.this);

                viewHolder.setDateVisibility(isControlsVisible());

            }

            @Override
            protected void onUnbindRowViewHolder(RowPresenter.ViewHolder vh) {
                super.onUnbindRowViewHolder(vh);
                vh.setOnKeyListener(null);
            }

        };

        rowPresenter.setDescriptionPresenter(detailsPresenter);

        return rowPresenter;
    }

    @Override
    public void setControlsVisibility(boolean show) {
        super.setControlsVisibility(show);

        if (getTransportViewHolder() != null) {
            getTransportViewHolder().setDateVisibility(show);
        }
    }

    @Override
    public void play() {
        super.play();

        if (getTransportViewHolder() != null) {
            getTransportViewHolder().setPlay(true);
        }
    }

    @Override
    public void pause() {
        super.pause();

        if (getTransportViewHolder() != null) {
            getTransportViewHolder().setPlay(false);
        }
    }

    @Override
    protected void onUpdateControlsVisibility() {
        super.onUpdateControlsVisibility();
    }

    @Override
    protected void onUpdateProgress() {
        super.onUpdateProgress();
    }

    public void setSeekPreviewTitle(String title) {
        if (getTransportViewHolder() != null) { // the chapter title when show seeking ui
            getTransportViewHolder().setSeekPreviewTitle(title);
        }
        if (getDescriptionViewHolder() != null) { // the chapter title when show full ui
            getDescriptionViewHolder().getBody().setText(title);
            getDescriptionViewHolder().getBody().setVisibility(title != null ? View.VISIBLE: View.GONE);
        }
    }

    public void setSeekBarSegments(List<SeekBarSegment> segments) {
        if (getTransportViewHolder() != null) {
            getTransportViewHolder().setSeekBarSegments(segments);
        }
    }

    private ViewHolder getTransportViewHolder() {
        return mTransportViewHolder != null ? mTransportViewHolder.get() : null;
    }

    private AbstractDetailsDescriptionPresenter.ViewHolder getDescriptionViewHolder() {
        return mDescriptionViewHolder != null ? mDescriptionViewHolder.get() : null;
    }

    public abstract void onTopEdgeFocused();
}
