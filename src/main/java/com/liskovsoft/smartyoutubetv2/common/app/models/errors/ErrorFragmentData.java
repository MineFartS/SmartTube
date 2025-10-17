package com.liskovsoft.smartyoutubetv2.common.app.models.errors;

/**
 * Contract for providing data to an error fragment/screen.
 * Implementations supply a user-visible message, optional action text,
 * and a callback to execute when the action is triggered.
 */
public interface ErrorFragmentData {
    /**
     * Invoked when the user selects the action associated with the error (e.g. "Sign in").
     * Implementations should perform the appropriate follow-up behavior.
     */
    void onAction();

    /**
     * Returns the message to display to the user describing the error.
     *
     * @return localized message string (should never be null)
     */
    String getMessage();

    /**
     * Returns the text for an optional action button shown alongside the message.
     *
     * @return action text or null if no action should be shown
     */
    String getActionText();
}
