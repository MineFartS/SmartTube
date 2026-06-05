package minefarts.smarttube.ui.browse.video;

import android.os.Bundle;

import androidx.annotation.Nullable;

import minefarts.smarttube.app.presenters.BrowsePresenter;
import minefarts.smarttube.app.presenters.base.BasePresenter;

public class VideoRowsFragment extends MultipleRowsFragment {
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getMainFragmentAdapter().getFragmentHost() != null) {
            getMainFragmentAdapter().getFragmentHost().notifyDataReady(getMainFragmentAdapter());
        }
    }

    @Override
    protected BasePresenter getMainPresenter() {
        return BrowsePresenter.instance(getContext());
    }

    @Override
    public void setExpand(boolean expand) {
        // force expand by default
        super.setExpand(true);
    }

}
