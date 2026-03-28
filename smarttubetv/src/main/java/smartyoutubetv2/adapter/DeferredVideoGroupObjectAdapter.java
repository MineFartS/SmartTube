package smartyoutubetv2.adapter;

import com.liskovsoft.smartyoutubetv2.common.app.models.data.VideoGroup;
import smartyoutubetv2.presenter.VideoCardPresenter;

public class DeferredVideoGroupObjectAdapter extends VideoGroupObjectAdapter {
    private long mPrevAppendTimeMs;

    public DeferredVideoGroupObjectAdapter(VideoGroup group, VideoCardPresenter presenter) {
        super(group, presenter);
    }

    @Override
    public void add(VideoGroup group) {
        long currentTimeMillis = System.currentTimeMillis();

        if (currentTimeMillis - mPrevAppendTimeMs < 3_000) {
            return;
        }

        mPrevAppendTimeMs = currentTimeMillis;

        super.add(group);
    }
}
