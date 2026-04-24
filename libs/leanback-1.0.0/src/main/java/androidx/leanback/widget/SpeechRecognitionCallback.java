
package androidx.leanback.widget;

/**
 * Interface for receiving notification that speech recognition should be initiated.
 *
 * @deprecated Launching voice recognition activity is no longer supported. App should declare
 *             android.permission.RECORD_AUDIO in AndroidManifest file. See details in
 *             {@link androidx.leanback.app.SearchSupportFragment}.
 */
@Deprecated
public interface SpeechRecognitionCallback {
    /**
     * Method invoked when speech recognition should be initiated.
     */
    void recognizeSpeech();
}
