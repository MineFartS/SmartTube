

package androidx.leanback.widget;

/**
 * Interface to be implemented by UI component to support seeking. PlaybackGlueHost may implement
 * the interface to support seeking UI for the PlaybackGlue. There is only one single method
 * {@link #setPlaybackSeekUiClient(Client)} in the interface. Client (PlaybackGlue) registers
 * itself as a Client to receive events emitted by PlaybackSeekUi and provide data to the
 * PlaybackSeekUi.
 */
public interface PlaybackSeekUi {

    /**
     * Client (e.g. PlaybackGlue) to register on PlaybackSeekUi so that it can interact
     * with Seeking UI. For example client(PlaybackGlue) will pause media when PlaybackSeekUi emits
     * {@link #onSeekStarted()} event.
     */
    class Client {

        /**
         * Called by PlaybackSeekUi to query client if seek is allowed.
         * @return True if allow PlaybackSeekUi to start seek, false otherwise.
         */
        public boolean isSeekEnabled() {
            return false;
        }

        /**
         * Event for start seeking. Client will typically pause media and save the current position
         * in the callback.
         */
        public void onSeekStarted() {
        }

        /**
         * Called by PlaybackSeekUi asking for PlaybackSeekDataProvider. This method will be called
         * after {@link #isSeekEnabled()} returns true. If client does not provide a
         * {@link PlaybackSeekDataProvider}, client may directly seek media in
         * {@link #onSeekPositionChanged(long)}.
         * @return PlaybackSeekDataProvider or null if no PlaybackSeekDataProvider is available.
         */
        public PlaybackSeekDataProvider getPlaybackSeekDataProvider() {
            return null;
        }

        /**
         * Called when user seeks to a different location. This callback is called multiple times
         * between {@link #onSeekStarted()} and {@link #onSeekFinished(boolean)}.
         * @param pos Position that user seeks to.
         */
        public void onSeekPositionChanged(long pos) {
        }

        /**
         * Called when cancelled or confirmed. When cancelled, client should restore playing from
         * the position before {@link #onSeekStarted()}. When confirmed, client should seek to
         * last updated {@link #onSeekPositionChanged(long)}.
         * @param cancelled True if cancelled false if confirmed.
         */
        public void onSeekFinished(boolean cancelled) {
        }
    }

    /**
     * Interface to be implemented by UI widget to support PlaybackSeekUi.
     */
    void setPlaybackSeekUiClient(Client client);

}
