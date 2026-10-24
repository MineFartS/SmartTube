package minefarts.smarttube.ui.playback.actions;

import android.content.Context;

import minefarts.smarttube.R;

public class SoundOffAction extends TwoStateAction {
    public SoundOffAction(Context context) {
        super(context, R.id.action_sound_off, R.drawable.action_sound_off);

        String label = context.getString(R.string.action_sound_off);
        String[] labels = new String[2];
        // Note, labels denote the action taken when clicked
        labels[INDEX_OFF] = label;
        labels[INDEX_ON] = label;
        setLabels(labels);
    }
}
