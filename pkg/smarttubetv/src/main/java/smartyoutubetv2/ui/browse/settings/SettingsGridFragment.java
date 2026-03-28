package smartyoutubetv2.ui.browse.settings;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.leanback.widget.VerticalGridPresenter;
import smartyoutubetv1.app.models.data.SettingsGroup;
import smartyoutubetv1.app.models.data.SettingsItem;
import smartyoutubetv1.app.presenters.BrowsePresenter;
import smartyoutubetv1.app.presenters.PlaybackPresenter;
import smartyoutubetv1.prefs.GeneralData;
import smartyoutubetv1.utils.SimpleEditDialog;
import smartyoutubetv1.utils.Utils;
import smartyoutubetv2.R;
import smartyoutubetv2.presenter.SettingsCardPresenter;
import smartyoutubetv2.ui.browse.interfaces.SettingsSection;
import smartyoutubetv2.ui.browse.video.GridFragmentHelper;
import smartyoutubetv2.ui.common.LeanbackActivity;
import smartyoutubetv2.ui.common.UriBackgroundManager;
import smartyoutubetv2.ui.mod.fragments.GridFragment;
import smartyoutubetv2.util.ViewUtil;

import java.util.ArrayList;
import java.util.List;

public class SettingsGridFragment extends GridFragment implements SettingsSection {

    private ArrayObjectAdapter mSettingsAdapter;
    private BrowsePresenter mMainPresenter;
    private UriBackgroundManager mBackgroundManager;
    private final List<SettingsGroup> mPendingUpdates = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMainPresenter = BrowsePresenter.instance(getContext());
        mBackgroundManager = ((LeanbackActivity) getActivity()).getBackgroundManager();

        setupAdapter();
        setupEventListeners();
        applyPendingUpdates();

        if (getMainFragmentAdapter().getFragmentHost() != null) {
            getMainFragmentAdapter().getFragmentHost().notifyDataReady(getMainFragmentAdapter());
        }
    }

    @Override
    protected void showOrHideTitle() {
        // NOP. Always show Browse fragment title
    }

    private void applyPendingUpdates() {
        for (SettingsGroup group : mPendingUpdates) {
            update(group);
        }

        mPendingUpdates.clear();
    }

    private void setupEventListeners() {
        setOnItemViewClickedListener(new ItemViewClickedListener());
    }

    private void setupAdapter() {
        VerticalGridPresenter presenter = new VerticalGridPresenter(ViewUtil.FOCUS_ZOOM_FACTOR, ViewUtil.FOCUS_DIMMER_ENABLED);
        presenter.enableChildRoundedCorners(ViewUtil.ROUNDED_CORNERS_ENABLED);
        presenter.setNumberOfColumns(GridFragmentHelper.getMaxColsNum(getContext(), R.dimen.settings_card_width));
        setGridPresenter(presenter);

        if (mSettingsAdapter == null) {
            SettingsCardPresenter gridPresenter = new SettingsCardPresenter();
            mSettingsAdapter = new ArrayObjectAdapter(gridPresenter);
            setAdapter(mSettingsAdapter);
        }
    }

    @Override
    public void clear() {
        if (mSettingsAdapter != null) {
            mSettingsAdapter.clear();
        }
    }

    @Override
    public boolean isEmpty() {
        if (mSettingsAdapter == null) {
            return mPendingUpdates.isEmpty();
        }

        return mSettingsAdapter.size() == 0;
    }

    @Override
    public void update(SettingsGroup group) {
        if (mSettingsAdapter == null) {
            mPendingUpdates.add(group);
            return;
        }

        // Always clear (continuation not supported)
        clear();

        if (group != null) {
            for (SettingsItem item : group.getItems()) {
                mSettingsAdapter.add(item);
            }
        }
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
    
        @Override
        public void onItemClicked(
            Presenter.ViewHolder itemViewHolder, 
            Object item,
            RowPresenter.ViewHolder rowViewHolder, 
            Row row
        ) {

            if (item instanceof SettingsItem) {
                ((SettingsItem) item).onClick.run();
            }

        }
    
    }

}
