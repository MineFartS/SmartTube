package com.liskovsoft.smartyoutubetv2.common.exoplayer.errors;

/**
 * Customized LoadErrorHandlingPolicy for DASH/HLS loads.
 *
 * Purpose:
 * - Tune retry/backoff behavior for manifest/segment requests to avoid long stalls for live streams.
 * - Map HTTP error codes to retry/no-retry decisions and provide sensible retry delays.
 *
 * Notes:
 * - Keep conservative retry policy for live streams to avoid reloading outdated manifests too often.
 * - Ensure that backoff respects ExoPlayer threading model and uses provided clocks.
 */
public class DashDefaultLoadErrorHandlingPolicy extends DefaultLoadErrorHandlingPolicy {
    /**
     * Copied from the parent class!
     */
    @Override
    public long getBlacklistDurationMsFor(int dataType, long loadDurationMs, IOException exception, int errorCount) {
        if (exception instanceof InvalidResponseCodeException) {
            int responseCode = ((InvalidResponseCodeException) exception).responseCode;
            return responseCode == 404 // HTTP 404 Not Found.
                    || responseCode == 410 // HTTP 410 Gone.
                    ? DEFAULT_TRACK_BLACKLIST_MS
                    : C.TIME_UNSET;
        }
        return C.TIME_UNSET;
    }

    /**
     * Copied from the parent class!
     */
    @Override
    public long getRetryDelayMsFor(int dataType, long loadDurationMs, IOException exception, int errorCount) {
        return exception instanceof ParserException
                || exception instanceof FileNotFoundException
                || exception instanceof UnexpectedLoaderException
                ? C.TIME_UNSET
                : Math.min((errorCount - 1) * 1000, 5000);
    }
}
