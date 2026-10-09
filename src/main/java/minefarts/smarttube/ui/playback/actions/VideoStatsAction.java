package minefarts.smarttube.ui.playback.actions;

import android.content.Context;
import minefarts.smarttube.R;

public class VideoStatsAction extends TwoStateAction {
    public VideoStatsAction(Context context) {
        super(context, R.id.action_video_stats, R.drawable.action_video_stats, false);

        String[] labels = new String[2];
        // Note, labels denote the action taken when clicked
        labels[INDEX_OFF] = context.getString(R.string.player_tweaks);
        labels[INDEX_ON] = context.getString(R.string.player_tweaks);
        setLabels(labels);
    }
}
