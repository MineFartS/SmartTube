package androidx.leanback.widget;

import static androidx.leanback.widget.ItemAlignmentFacet.ITEM_ALIGN_OFFSET_PERCENT_DISABLED;
import static androidx.recyclerview.widget.RecyclerView.HORIZONTAL;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;

import androidx.leanback.widget.GridLayoutManager.LayoutParams;

/**
 * Helper class to handle ItemAlignmentFacet in a grid view.
 */
class ItemAlignmentFacetHelper {

    private static Rect sRect = new Rect();

    /**
     * get alignment position relative to optical left/top of itemView.
     */
    static int getAlignmentPosition(View itemView, ItemAlignmentFacet.ItemAlignmentDef facet,
            int orientation) {
        LayoutParams p = (LayoutParams) itemView.getLayoutParams();
        View view = itemView;
        if (facet.mViewId != 0) {
            view = itemView.findViewById(facet.mViewId);
            if (view == null) {
                view = itemView;
            }
        }
        int alignPos = facet.mOffset;
        if (orientation == HORIZONTAL) {
            if (itemView.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                alignPos = (view == itemView ? p.getOpticalWidth(view)
                        : view.getWidth()) - alignPos;
                if (facet.mOffsetWithPadding) {
                    if (facet.mOffsetPercent == 0f) {
                        alignPos -= view.getPaddingRight();
                    } else if (facet.mOffsetPercent == 100f) {
                        alignPos += view.getPaddingLeft();
                    }
                }
                if (facet.mOffsetPercent != ITEM_ALIGN_OFFSET_PERCENT_DISABLED) {
                    alignPos -= (int) (((view == itemView ? p.getOpticalWidth(view)
                            : view.getWidth()) * facet.mOffsetPercent) / 100f);
                }
                if (itemView != view) {
                    sRect.right = alignPos;
                    ((ViewGroup) itemView).offsetDescendantRectToMyCoords(view, sRect);
                    alignPos = sRect.right + p.getOpticalRightInset();
                }
            } else  {
                if (facet.mOffsetWithPadding) {
                    if (facet.mOffsetPercent == 0f) {
                        alignPos += view.getPaddingLeft();
                    } else if (facet.mOffsetPercent == 100f) {
                        alignPos -= view.getPaddingRight();
                    }
                }
                if (facet.mOffsetPercent != ITEM_ALIGN_OFFSET_PERCENT_DISABLED) {
                    alignPos += (int) (((view == itemView ? p.getOpticalWidth(view)
                            : view.getWidth()) * facet.mOffsetPercent) / 100f);
                }
                if (itemView != view) {
                    sRect.left = alignPos;
                    ((ViewGroup) itemView).offsetDescendantRectToMyCoords(view, sRect);
                    alignPos = sRect.left - p.getOpticalLeftInset();
                }
            }
        } else {
            if (facet.mOffsetWithPadding) {
                if (facet.mOffsetPercent == 0f) {
                    alignPos += view.getPaddingTop();
                } else if (facet.mOffsetPercent == 100f) {
                    alignPos -= view.getPaddingBottom();
                }
            }
            if (facet.mOffsetPercent != ITEM_ALIGN_OFFSET_PERCENT_DISABLED) {
                alignPos += (int) (((view == itemView ? p.getOpticalHeight(view) : view.getHeight())
                        * facet.mOffsetPercent) / 100f);
            }
            if (itemView != view) {
                sRect.top = alignPos;
                ((ViewGroup) itemView).offsetDescendantRectToMyCoords(view, sRect);
                alignPos = sRect.top - p.getOpticalTopInset();
            }
            if (facet.isAlignedToTextViewBaseLine()) {
                alignPos += view.getBaseline();
            }
        }
        return alignPos;
    }

    private ItemAlignmentFacetHelper() {
    }
}
