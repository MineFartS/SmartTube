package minefarts.smarttube.presenter;

import minefarts.smarttube.leanback.widget.ListRowPresenter;
import minefarts.smarttube.utils.ViewUtil;

public class CustomListRowPresenter extends ListRowPresenter {
    public CustomListRowPresenter() {
        super(ViewUtil.FOCUS_ZOOM_FACTOR, ViewUtil.FOCUS_DIMMER_ENABLED);
        setSelectEffectEnabled(ViewUtil.ROW_SELECT_EFFECT_ENABLED);
        enableChildRoundedCorners(ViewUtil.ROUNDED_CORNERS_ENABLED);
    }
}
