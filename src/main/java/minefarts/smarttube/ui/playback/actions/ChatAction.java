package minefarts.smarttube.ui.playback.actions;

import android.content.Context;
import minefarts.smarttube.R;

/**
 * An action for displaying chat/comments.
 */
public class ChatAction extends TwoStateAction {

    public ChatAction(Context context) {
        super(context, R.id.action_chat, R.drawable.action_chat);

        String label = "Chat/Comments";

        String[] labels = new String[2];
        labels[INDEX_OFF] = label;
        labels[INDEX_ON] = label;
        setLabels(labels);
        
    }

}
