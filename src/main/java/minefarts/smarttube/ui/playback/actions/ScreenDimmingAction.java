package minefarts.smarttube.ui.playback.actions;

import android.content.Context;
import minefarts.smarttube.R;

public class ScreenDimmingAction extends TwoStateAction {
    public ScreenDimmingAction(Context context) {
        super(context, R.id.action_screen_dimming, R.drawable.action_screen_timeout_on);

        String label = context.getString(R.string.screen_dimming);
        String[] labels = new String[2];
        // Note, labels denote the action taken when clicked
        labels[INDEX_OFF] = label;
        labels[INDEX_ON] = label;
        setLabels(labels);
    }
}
