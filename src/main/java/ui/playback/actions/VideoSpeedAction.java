package minefarts.smarttube.ui.playback.actions;

import android.content.Context;
import minefarts.smarttube.R;

public class VideoSpeedAction extends TwoStateAction {

    public VideoSpeedAction(Context context) {
        super(context, R.id.action_video_speed, R.drawable.action_video_speed);

        String label = "Video speed";

        String[] labels = new String[2];
        labels[INDEX_OFF] = label;
        labels[INDEX_ON] = label;
        setLabels(labels);
        
    }
}
