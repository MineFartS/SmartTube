package androidx.leanback.widget;

import android.view.View;

/**
 * Interface for a custom EditText subclass to support autofill in
 * {@link androidx.leanback.app.GuidedStepSupportFragment}.
 * <p>
 *
 * Apps who needs to supply custom layouts for {@link GuidedActionsStylist} with their own EditText
 * classes should implement this interface in order to support autofill in
 * {@link androidx.leanback.app.GuidedStepSupportFragment}. This ensures autofill event happened
 * within custom EditText is propagated to GuidedStepSupportFragment.
 * e.g.
 * <pre><code>
 * public class MyEditText extends EditText implements GuidedActionAutofillSupport {
 *     OnAutofillListener mAutofillViewListener;
 *     &#064;Override
 *     public void setOnAutofillListener(OnAutofillListener autofillViewListener) {
 *         mAutofillViewListener = autofillViewListener;
 *     }
 *
 *     &#064;Override
 *     public void autofill(AutofillValue values) {
 *         super.autofill(values);
 *         if (mAutofillViewListener != null) {
 *             mAutofillViewListener.onAutofill(this);
 *         }
 *     }
 *     // ...
 * }
 * </code></pre>
 *
 */
public interface GuidedActionAutofillSupport {

    /**
     * Listener for autofill event. Leanback will set the Listener on the custom view.
     */
    interface OnAutofillListener {

        /**
         * Custom view should call this method when autofill event happened.
         *
         * @param view The view where autofill happened.
         */
        void onAutofill(View view);

    }

    /**
     * Sets AutofillListener on the custom view.
     */
    void setOnAutofillListener(OnAutofillListener listener);
}
