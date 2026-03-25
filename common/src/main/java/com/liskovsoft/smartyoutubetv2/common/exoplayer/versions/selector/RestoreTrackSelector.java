package com.liskovsoft.smartyoutubetv2.common.exoplayer.versions.selector;

import android.util.Pair;
import androidx.annotation.Nullable;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.RendererCapabilities;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection.Definition;
import com.google.android.exoplayer2.trackselection.TrackSelection.Factory;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.smartyoutubetv2.common.exoplayer.selector.TrackSelectorManager;
import com.liskovsoft.smartyoutubetv2.common.exoplayer.selector.track.MediaTrack;

public class RestoreTrackSelector extends DefaultTrackSelector {
    private static final String TAG = RestoreTrackSelector.class.getSimpleName();

    private TrackSelectorCallback mCallback;

    public interface TrackSelectorCallback {
        Pair<Definition, MediaTrack> onSelectVideoTrack(TrackGroupArray groups, Parameters params);
        Pair<Definition, MediaTrack> onSelectAudioTrack(TrackGroupArray groups, Parameters params);
        Pair<Definition, MediaTrack> onSelectSubtitleTrack(TrackGroupArray groups, Parameters params);
        void updateVideoTrackSelection(TrackGroupArray groups, Parameters params, Definition definition);
        void updateAudioTrackSelection(TrackGroupArray groups, Parameters params, Definition definition);
        void updateSubtitleTrackSelection(TrackGroupArray groups, Parameters params, Definition definition);
    }

    public RestoreTrackSelector(Factory trackSelectionFactory) {
        super(trackSelectionFactory);
    }

    public void setOnTrackSelectCallback(TrackSelectorCallback callback) {
        mCallback = callback;
    }

    // Exo 2.10 and up
    @Nullable
    @Override
    protected Definition selectVideoTrack(TrackGroupArray groups, int[][] formatSupports, int mixedMimeTypeAdaptationSupports,
                                              Parameters params, boolean enableAdaptiveTrackSelection) throws ExoPlaybackException {
        if (mCallback != null) {
            Pair<Definition, MediaTrack> resultPair = mCallback.onSelectVideoTrack(groups, params);

            if (resultPair != null) {
                Log.d(TAG, "selectVideoTrack: choose custom video processing");
                return resultPair.first;
            } else {
                return null; // video disabled
            }
        }

        Log.d(TAG, "selectVideoTrack: choose default video processing");

        Definition definition = super.selectVideoTrack(groups, formatSupports, mixedMimeTypeAdaptationSupports, params, false);

        // Don't invoke if track already has been selected by the app
        if (mCallback != null && definition != null) {
            mCallback.updateVideoTrackSelection(groups, params, definition);
        }

        return definition;
    }

    // Exo 2.10 and up
    @Nullable
    @Override
    protected Pair<Definition, AudioTrackScore> selectAudioTrack(TrackGroupArray groups, int[][] formatSupports,
                                                                 int mixedMimeTypeAdaptationSupports, Parameters params, boolean enableAdaptiveTrackSelection) throws ExoPlaybackException {
        if (mCallback != null) {
            Pair<Definition, MediaTrack> resultPair = mCallback.onSelectAudioTrack(groups, params);
            if (resultPair != null) {
                Log.d(TAG, "selectVideoTrack: choose custom audio processing");
                return new Pair<>(resultPair.first, new AudioTrackScore(resultPair.second.format, params, RendererCapabilities.FORMAT_HANDLED));
            } else {
                return null; // audio disabled
            }
        }

        Log.d(TAG, "selectAudioTrack: choose default audio processing");

        Pair<Definition, AudioTrackScore> definitionPair = super.selectAudioTrack(groups, formatSupports,
                mixedMimeTypeAdaptationSupports, params, false);

        // Don't invoke if track already has been selected by the app
        if (mCallback != null && definitionPair != null) {
            mCallback.updateAudioTrackSelection(groups, params, definitionPair.first);
        }

        return definitionPair;
    }

    // Exo 2.10 and up
    @Nullable
    @Override
    protected Pair<Definition, TextTrackScore> selectTextTrack(TrackGroupArray groups, int[][] formatSupport, Parameters params,
                                                               @Nullable String selectedAudioLanguage) throws ExoPlaybackException {
        if (mCallback != null) {
            Pair<Definition, MediaTrack> resultPair = mCallback.onSelectSubtitleTrack(groups, params);
            if (resultPair != null) {
                Log.d(TAG, "selectTextTrack: choose custom text processing");
                return new Pair<>(resultPair.first, new TextTrackScore(resultPair.second.format, params, RendererCapabilities.FORMAT_HANDLED, ""));
            }
        }

        Log.d(TAG, "selectTextTrack: choose default text processing");

        Pair<Definition, TextTrackScore> definitionPair = super.selectTextTrack(groups, formatSupport, params, selectedAudioLanguage);

        // Don't invoke if track already has been selected by the app
        if (mCallback != null && definitionPair != null) {
            mCallback.updateSubtitleTrackSelection(groups, params, definitionPair.first);
        }

        return definitionPair;
    }

}
