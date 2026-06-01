package minefarts.smarttube;

import minefarts.smarttube.source.TrackGroupArray;
import minefarts.smarttube.trackselection.TrackSelectionArray;
import minefarts.smarttube.upstream.Allocator;
import minefarts.smarttube.utils.Assertions;

public final class RampingLoadControl implements LoadControl {

    private final long initialMinBufferMs = 1500;
    private final long initialMaxBufferMs = 3000;
    private final long initialBufferForPlaybackMs = 500;
    private final long initialBufferForPlaybackAfterRebufferMs = 1000;

    private final long rampDurationMs = 60_000;

    private volatile long startupWallClockMs = 0;
    private volatile long lastElapsed = -1;
    private volatile DefaultLoadControl delegate;

    public RampingLoadControl() {
        updateDelegate(0);
    }

    private long lerp(long from, long to, double t) {
        return from + (long) ((to - from) * t);
    }

    private void updateDelegate(long elapsedMs) {

        double t = Math.min(
            1d, 
            (double) elapsedMs / (double) rampDurationMs
        );

        int minBufferMs = (int) lerp(
            initialMinBufferMs, 
            DefaultLoadControl.DEFAULT_MIN_BUFFER_MS, 
            t
        );
        
        int maxBufferMs = (int) lerp(
            initialMaxBufferMs, 
            DefaultLoadControl.DEFAULT_MAX_BUFFER_MS, 
            t
        );
        
        int bufferForPlaybackMs = (int) lerp(
            initialBufferForPlaybackMs, 
            DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS, 
            t
        );
        
        int bufferForPlaybackAfterRebufferMs = (int) lerp(
            initialBufferForPlaybackAfterRebufferMs,
            DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS,
            t
        );

        minBufferMs = Math.max(
            minBufferMs, 
            Math.max(bufferForPlaybackMs, bufferForPlaybackAfterRebufferMs)
        );

        maxBufferMs = Math.max(maxBufferMs, minBufferMs);

        this.delegate = new DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                minBufferMs,
                maxBufferMs,
                bufferForPlaybackMs,
                bufferForPlaybackAfterRebufferMs
            )
            .setPrioritizeTimeOverSizeThresholds(
                DefaultLoadControl.DEFAULT_PRIORITIZE_TIME_OVER_SIZE_THRESHOLDS
            )
            .setBackBuffer(
                DefaultLoadControl.DEFAULT_BACK_BUFFER_DURATION_MS,
                DefaultLoadControl.DEFAULT_RETAIN_BACK_BUFFER_FROM_KEYFRAME
            )
            .createDefaultLoadControl();
        
    }

    private void updateDelegate() {
        updateDelegate(System.currentTimeMillis() - startupWallClockMs);
    }

    @Override
    public void onPrepared() {
        startupWallClockMs = 0;
        updateDelegate(0);
        delegate.onPrepared();
    }

    @Override
    public void onTracksSelected(
        Renderer[] renderers, 
        TrackGroupArray trackGroups, 
        TrackSelectionArray trackSelections
    ) {
        updateDelegate();
        delegate.onTracksSelected(renderers, trackGroups, trackSelections);
    }

    @Override
    public void onStopped() {
        startupWallClockMs = 0;
        delegate.onStopped();
    }

    @Override
    public void onReleased() {
        startupWallClockMs = 0;
        delegate.onReleased();
    }

    @Override
    public Allocator getAllocator() {
        updateDelegate();
        return delegate.getAllocator();
    }

    @Override
    public long getBackBufferDurationUs() {
        updateDelegate();
        return delegate.getBackBufferDurationUs();
    }

    @Override
    public boolean retainBackBufferFromKeyframe() {
        updateDelegate();
        return delegate.retainBackBufferFromKeyframe();
    }

    @Override
    public boolean shouldContinueLoading(long bufferedDurationUs, float playbackSpeed) {
        updateDelegate();
        return delegate.shouldContinueLoading(bufferedDurationUs, playbackSpeed);
    }

    @Override
    public boolean shouldStartPlayback(
        long bufferedDurationUs, 
        float playbackSpeed, 
        boolean rebuffering
    ) {
        updateDelegate();
        return delegate.shouldStartPlayback(bufferedDurationUs, playbackSpeed, rebuffering);
    }

}

