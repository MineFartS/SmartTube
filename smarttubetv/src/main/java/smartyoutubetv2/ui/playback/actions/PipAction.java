package smartyoutubetv2.ui.playback.actions;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import androidx.leanback.widget.Action;
import smartyoutubetv2.R;

/**
 * An action for displaying a PIP icon.
 */
public class PipAction extends Action {
    public PipAction(Context context) {
        super(R.id.action_pip);
        Drawable uncoloredDrawable = ContextCompat.getDrawable(context, R.drawable.action_pip);

        setIcon(uncoloredDrawable);
        setLabel1(context.getString(
                R.string.run_in_background));
    }
}
