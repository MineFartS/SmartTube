package com.liskovsoft.smartyoutubetv2.common.exoplayer.selector.track;

/**
 * Audio-specific MediaTrack with bitrate/language/DRC-aware selection heuristics.
 *
 * Behavior:
 * - Compares tracks primarily by codec/bitrate and channel properties.
 * - Provides helpers to detect DRC (dynamic range compression) marks and prefer tracks
 *   that preserve DRC consistency with the origin track.
 *
 * Notes:
 * - Some devices have issues with very high bitrate mp4a audio; TrackSelectorManager may
 *   filter or prefer alternate codecs on problematic devices.
 *
 * Audio track details:
 * - channels (mono/stereo/5.1+), sampleRate, language, codec, isDefault
 *
 * Notes:
 * - When multiple audio tracks with same language exist prefer one with higher channel count
 *   unless user preference forces stereo.
 * - Expose a friendly title (e.g., "English • Stereo • AAC").
 */
public class AudioTrack extends MediaTrack {
    public AudioTrack(int rendererIndex) {
        super(rendererIndex);
    }

    //@Override
    //public int inBounds(MediaTrack track2) {
    //    int result = compare(track2);
    //
    //    // Select at least something.
    //    if (result == -1 && track2 != null && track2.format != null) {
    //        result = 1;
    //    }
    //
    //    return result;
    //}

    @Override
    public int inBounds(MediaTrack track2) {
        if (format == null) {
            return -1;
        }

        if (track2 == null || track2.format == null) {
            return 1;
        }

        int result = -1;

        String id1 = format.id;
        String id2 = track2.format.id;
        int bitrate1 = format.bitrate;
        int bitrate2 = track2.format.bitrate;

        // Compare by language isn't robust since language set may not contain target language
        String language1 = format.language;
        String language2 = track2.format.language;
        boolean sameLanguage = sameLanguage(language1, language2);

        if (Helpers.equals(id1, id2)) {
            result = 0;
        } else if (bitrate1 != -1 && bitrateLessOrEquals(bitrate2, bitrate1)) {
            result = 1;
        } else if (bitrate1 == -1 && (TrackSelectorUtil.is51Audio(format) || !TrackSelectorUtil.is51Audio(track2.format))) {
            result = 1;
        }

        return result;
    }

    @Override
    public int compare(MediaTrack track2) {
        if (format == null) {
            return -1;
        }

        if (track2 == null || track2.format == null) {
            return 1;
        }

        int result = -1;

        if (format.id == null && format.language == null && format.bitrate == -1 && codecEquals(this, track2)) {
            result = 0;
        } else if (Helpers.equals(format.id, track2.format.id)) {
            result = 1;
        } else if (!codecEquals(this, track2) || !drcEquals(format, track2.format) || bitrateLessOrEquals(track2.format.bitrate, format.bitrate)) {
            result = 0;
        }

        return result;
    }

    private boolean sameLanguage(String language1, String language2) {
        return Helpers.equals(language1, language2) || (language1 == null || language2 == null);
    }

    private static boolean drcEquals(Format format1, Format format2) {
        if (format1 == null || format2 == null) {
            return false;
        }

        return TrackSelectorUtil.isDrc(format1) == TrackSelectorUtil.isDrc(format2);
    }
}
