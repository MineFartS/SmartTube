package minefarts.smarttube.leanback.widget;

import minefarts.smarttube.R;
import minefarts.smarttube.leanback.app.DetailsFragment;
import minefarts.smarttube.leanback.app.DetailsSupportFragment;

/**
 * Subclass of Parallax object that tracks overview row's top and bottom edge in DetailsFragment
 * or DetailsSupportFragment.
 * <p>
 * It can be used for both creating cover image parallax effect and controlling video playing
 * when transitioning to/from half/full screen.  A direct use case is
 * {@link minefarts.smarttube.leanback.app.DetailsFragmentBackgroundController}.
 * </p>
 * @see DetailsFragment#getParallax()
 * @see minefarts.smarttube.leanback.app.DetailsFragmentBackgroundController
 * @see DetailsSupportFragment#getParallax()
 * @see minefarts.smarttube.leanback.app.DetailsSupportFragmentBackgroundController
 */
public class DetailsParallax extends RecyclerViewParallax {
    final IntProperty mFrameTop;
    final IntProperty mFrameBottom;

    public DetailsParallax() {
        // track the top edge of details_frame of first item of adapter
        mFrameTop = addProperty("overviewRowTop")
                .adapterPosition(0)
                .viewId(R.id.details_frame);

        // track the bottom edge of details_frame of first item of adapter
        mFrameBottom = addProperty("overviewRowBottom")
                .adapterPosition(0)
                .viewId(R.id.details_frame)
                .fraction(1.0f);

    }

    /**
     * Returns the top of the details overview row. This is tracked for implementing the
     * parallax effect.
     */
    public Parallax.IntProperty getOverviewRowTop() {
        return mFrameTop;
    }

    /**
     * Returns the bottom of the details overview row. This is tracked for implementing the
     * parallax effect.
     */
    public Parallax.IntProperty getOverviewRowBottom() {
        return mFrameBottom;
    }
}
