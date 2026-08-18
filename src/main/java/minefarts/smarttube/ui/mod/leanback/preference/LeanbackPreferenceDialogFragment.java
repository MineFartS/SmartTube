

package minefarts.smarttube.ui.mod.leanback.preference;

import android.app.Fragment;
import android.os.Build;
import androidx.preference.DialogPreference;
import androidx.preference.Preference;

public class LeanbackPreferenceDialogFragment extends Fragment {
    private Preference mPref;

    public static final String ARG_KEY = "key";

    public LeanbackPreferenceDialogFragment() {
        if (Build.VERSION.SDK_INT >= 21) {
            LeanbackPreferenceFragmentTransitionHelperApi21.addTransitions(this);
        }
    }

    // MODIFIED: Fix Android 9 error by allowing null fragments
    // Target fragment doesn't belongs to this fragment manager
    @Override
    public void setTargetFragment(Fragment fragment, int requestCode) {
        // NOP
    }

    public void setPreference(Preference pref) {
        mPref = pref;
    }

    // MODIFIED: allow use this fragment without target fragment
    public DialogPreference getPreference() {
        return mPref != null ? (DialogPreference) mPref : null;
    }
}
