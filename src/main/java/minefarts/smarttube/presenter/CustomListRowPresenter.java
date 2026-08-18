package minefarts.smarttube.presenter;

import androidx.leanback.widget.ListRowPresenter;
import minefarts.smarttube.util.ViewUtil;

public class CustomListRowPresenter extends ListRowPresenter {
    public CustomListRowPresenter() {
        super(ViewUtil.FOCUS_ZOOM_FACTOR, ViewUtil.FOCUS_DIMMER_ENABLED);
        setSelectEffectEnabled(ViewUtil.ROW_SELECT_EFFECT_ENABLED);
        enableChildRoundedCorners(ViewUtil.ROUNDED_CORNERS_ENABLED);
    }
}
