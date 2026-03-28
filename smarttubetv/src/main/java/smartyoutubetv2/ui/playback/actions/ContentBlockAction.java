package smartyoutubetv2.ui.playback.actions;

import android.content.Context;
import smartyoutubetv2.R;

/**
 * An action for enable/disable sponsored content.
 */
public class ContentBlockAction extends TwoStateAction {
    public ContentBlockAction(Context context) {
        super(context, R.id.action_content_block, R.drawable.action_content_block);

        String label = context.getString(R.string.content_block_provider);
        String[] labels = new String[2];
        // Note, labels denote the action taken when clicked
        labels[INDEX_OFF] = label;
        labels[INDEX_ON] = label;
        setLabels(labels);
    }
}
