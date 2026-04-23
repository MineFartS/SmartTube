

package SmartTubeApp.messages;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

class RecyclerScrollMoreListener
        extends RecyclerView.OnScrollListener {

    private OnLoadMoreListener loadMoreListener;
    private int currentPage = 0;
    private int previousTotalItemCount = 0;
    private int currentScrollPos = 0;
    private int maxScrollPos = 0;
    private boolean loading = true;

    private RecyclerView.LayoutManager mLayoutManager;

    RecyclerScrollMoreListener(LinearLayoutManager layoutManager, OnLoadMoreListener loadMoreListener) {
        this.mLayoutManager = layoutManager;
        this.loadMoreListener = loadMoreListener;
    }

    // MODIFIED: fully custom
    // Fix: lastVisibleItemPositions is wrong. Solution: remove it altogether.
    @Override
    public void onScrolled(RecyclerView view, int dx, int dy) {
        if (loadMoreListener != null) {
            // MODIFIED: throttle calls
            // Swallow scrolling up. Continue on scroll down.
            currentScrollPos += dy;
            if (currentScrollPos <= maxScrollPos) {
                return;
            }
            maxScrollPos += dy;

            int totalItemCount = mLayoutManager.getItemCount();

            if (totalItemCount < previousTotalItemCount) {
                this.currentPage = 0;
                this.previousTotalItemCount = totalItemCount;
                if (totalItemCount == 0) {
                    this.loading = true;
                }
            }

            if (loading && (totalItemCount > previousTotalItemCount)) {
                loading = false;
                previousTotalItemCount = totalItemCount;
            }

            if (!loading) {
                currentPage++;
                loadMoreListener.onLoadMore(loadMoreListener.getMessagesCount(), totalItemCount);
                loading = true;
            }
        }
    }

    interface OnLoadMoreListener {
        void onLoadMore(int page, int total);

        int getMessagesCount();
    }
}
