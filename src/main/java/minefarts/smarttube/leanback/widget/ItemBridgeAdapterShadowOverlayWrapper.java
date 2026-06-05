package minefarts.smarttube.leanback.widget;

import android.content.Context;
import android.view.View;

/**
 * A wrapper class working with {@link ItemBridgeAdapter} to wrap item view in a
 * {@link ShadowOverlayContainer}.  The ShadowOverlayContainer is created from conditions
 * of {@link ShadowOverlayHelper}.
 */
public class ItemBridgeAdapterShadowOverlayWrapper extends ItemBridgeAdapter.Wrapper {

    private final ShadowOverlayHelper mHelper;

    public ItemBridgeAdapterShadowOverlayWrapper(ShadowOverlayHelper helper) {
        mHelper = helper;
    }

    @Override
    public View createWrapper(View root) {
        Context context = root.getContext();
        ShadowOverlayContainer wrapper = mHelper.createShadowOverlayContainer(context);
        return wrapper;
    }
    @Override
    public void wrap(View wrapper, View wrapped) {
        ((ShadowOverlayContainer) wrapper).wrap(wrapped);
    }

}
