package minefarts.smarttube.sabr.parser;

import androidx.annotation.NonNull;

import minefarts.smarttube.extractor.ExtractorInput;
import minefarts.smarttube.sabr.parser.exceptions.MediaSegmentMismatchError;
import minefarts.smarttube.sabr.parser.exceptions.SabrStreamError;
import minefarts.smarttube.sabr.parser.models.AudioSelector;
import minefarts.smarttube.sabr.parser.models.CaptionSelector;
import minefarts.smarttube.sabr.parser.models.VideoSelector;
import minefarts.smarttube.sabr.parser.parts.FormatInitializedSabrPart;
import minefarts.smarttube.sabr.parser.parts.MediaSeekSabrPart;
import minefarts.smarttube.sabr.parser.parts.MediaSegmentDataSabrPart;
import minefarts.smarttube.sabr.parser.parts.MediaSegmentEndSabrPart;
import minefarts.smarttube.sabr.parser.parts.MediaSegmentInitSabrPart;
import minefarts.smarttube.sabr.parser.parts.PoTokenStatusSabrPart;
import minefarts.smarttube.sabr.parser.parts.RefreshPlayerResponseSabrPart;
import minefarts.smarttube.sabr.parser.parts.SabrPart;
import minefarts.smarttube.sabr.parser.processor.ProcessFormatInitializationMetadataResult;
import minefarts.smarttube.sabr.parser.processor.ProcessMediaEndResult;
import minefarts.smarttube.sabr.parser.processor.ProcessMediaHeaderResult;
import minefarts.smarttube.sabr.parser.processor.ProcessMediaResult;
import minefarts.smarttube.sabr.parser.processor.ProcessStreamProtectionStatusResult;
import minefarts.smarttube.sabr.parser.processor.SabrProcessor;
import minefarts.smarttube.sabr.parser.ump.UMPDecoder;
import minefarts.smarttube.sabr.parser.ump.UMPPart;
import minefarts.smarttube.sabr.parser.ump.UMPPartId;
import minefarts.smarttube.sabr.protos.videostreaming.ClientAbrState;
import minefarts.smarttube.sabr.protos.videostreaming.ClientInfo;
import minefarts.smarttube.sabr.protos.videostreaming.FormatInitializationMetadata;
import minefarts.smarttube.sabr.protos.videostreaming.LiveMetadata;
import minefarts.smarttube.sabr.protos.videostreaming.NextRequestPolicy;
import minefarts.smarttube.sabr.protos.videostreaming.MediaHeader;
import minefarts.smarttube.sabr.protos.videostreaming.SabrRedirect;
import minefarts.smarttube.sabr.protos.videostreaming.StreamProtectionStatus;
import minefarts.smarttube.sabr.protos.videostreaming.SabrSeek;
import minefarts.smarttube.sabr.protos.videostreaming.SabrError;
import minefarts.smarttube.sabr.protos.videostreaming.SabrContextUpdate;
import minefarts.smarttube.sabr.protos.videostreaming.SabrContextSendingPolicy;
import minefarts.smarttube.sabr.protos.videostreaming.ReloadPlayerResponse;
import minefarts.smarttube.sabr.protos.videostreaming.VideoPlaybackAbrRequest;
import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.utils.mylogger.Log;
import minefarts.smarttube.utils.querystringparser.UrlQueryString;
import minefarts.smarttube.utils.querystringparser.UrlQueryStringFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SabrStream {
    private static final String TAG = SabrStream.class.getSimpleName();
    private final int[] KNOWN_PARTS = {
            UMPPartId.MEDIA_HEADER,
            UMPPartId.MEDIA,
            UMPPartId.MEDIA_END,
            UMPPartId.STREAM_PROTECTION_STATUS,
            UMPPartId.SABR_REDIRECT,
            UMPPartId.FORMAT_INITIALIZATION_METADATA,
            UMPPartId.NEXT_REQUEST_POLICY,
            UMPPartId.LIVE_METADATA,
            UMPPartId.SABR_SEEK,
            UMPPartId.SABR_ERROR,
            UMPPartId.SABR_CONTEXT_UPDATE,
            UMPPartId.SABR_CONTEXT_SENDING_POLICY,
            UMPPartId.RELOAD_PLAYER_RESPONSE
    };
    private final int[] IGNORED_PARTS = {
            UMPPartId.REQUEST_IDENTIFIER,
            UMPPartId.REQUEST_CANCELLATION_POLICY,
            UMPPartId.PLAYBACK_START_POLICY,
            UMPPartId.ALLOWED_CACHED_FORMATS,
            UMPPartId.PAUSE_BW_SAMPLING_HINT,
            UMPPartId.START_BW_SAMPLING_HINT,
            UMPPartId.REQUEST_PIPELINING,
            UMPPartId.SELECTABLE_FORMATS,
            UMPPartId.PREWARM_CONNECTION,
    };
    private final UMPDecoder decoder;
    private final SabrProcessor processor;
    private final NoSegmentsTracker noNewSegmentsTracker;
    private final Set<Integer> unknownPartTypes;
    private int sqMismatchForwardCount;
    private int sqMismatchBacktrackCount;
    private boolean receivedNewSegments;
    private String url;
    private List<? extends  SabrPart> multiResult = null;

    private static class NoSegmentsTracker {
        public int consecutiveRequests = 0;
        public float timestampStarted = -1;
        public int liveHeadSegmentStarted = -1;

        public void reset() {
             consecutiveRequests = 0;
             timestampStarted = -1;
             liveHeadSegmentStarted = -1;
        }

        public void increment(int liveHeadSegment) {
            if (consecutiveRequests == 0) {
                timestampStarted = System.currentTimeMillis() * 1_000;
                liveHeadSegmentStarted = liveHeadSegment;
            }
            consecutiveRequests += 1;
        }
    }

    public SabrStream(
            @NonNull String serverAbrStreamingUrl,
            @NonNull String videoPlaybackUstreamerConfig,
            @NonNull ClientInfo clientInfo,
            AudioSelector audioSelection,
            VideoSelector videoSelection,
            CaptionSelector captionSelection,
            int liveSegmentTargetDurationSec,
            int liveSegmentTargetDurationToleranceMs,
            long startTimeMs,
            String poToken,
            boolean postLive,
            String videoId
    ) {
        decoder = new UMPDecoder();
        processor = new SabrProcessor(
                videoPlaybackUstreamerConfig,
                clientInfo,
                audioSelection,
                videoSelection,
                captionSelection,
                liveSegmentTargetDurationSec,
                liveSegmentTargetDurationToleranceMs,
                startTimeMs,
                poToken,
                postLive,
                videoId
        );
        url = serverAbrStreamingUrl;

        // Whether we got any new (not consumed) segments in the request
        noNewSegmentsTracker = new NoSegmentsTracker();
        unknownPartTypes = new HashSet<>();

        sqMismatchBacktrackCount = 0;
        sqMismatchForwardCount = 0;
    }

    public SabrPart parse(@NonNull ExtractorInput extractorInput) {
        SabrPart result = null;

        while (result == null && (multiResult == null || multiResult.isEmpty())) {
            UMPPart part = nextKnownUMPPart(extractorInput);

            if (part == null) {
                break;
            }

            result = parsePart(part);

            if (result == null) {
                multiResult = parseMultiPart(part);
            }
        }

        return result != null ? result : multiResult != null && !multiResult.isEmpty() ? multiResult.remove(0) : null;
    }

    public VideoPlaybackAbrRequest buildVideoPlaybackAbrRequest() {
        return processor.buildVideoPlaybackAbrRequest();
    }

    public void reset() {
        noNewSegmentsTracker.reset();
    }

    private SabrPart parsePart(UMPPart part) {
        switch (part.partId) {
            case UMPPartId.MEDIA_HEADER:
                return processMediaHeader(part);
            case UMPPartId.MEDIA:
                return processMedia(part);
            case UMPPartId.MEDIA_END:
                return processMediaEnd(part);
            case UMPPartId.STREAM_PROTECTION_STATUS:
                return processStreamProtectionStatus(part);
            case UMPPartId.SABR_REDIRECT:
                processSabrRedirect(part);
                return null;
            case UMPPartId.FORMAT_INITIALIZATION_METADATA:
                return processFormatInitializationMetadata(part);
            case UMPPartId.NEXT_REQUEST_POLICY:
                processNextRequestPolicy(part);
                return null;
            case UMPPartId.SABR_ERROR:
                processSabrError(part);
                return null;
            case UMPPartId.SABR_CONTEXT_UPDATE:
                processSabrContextUpdate(part);
                return null;
            case UMPPartId.SABR_CONTEXT_SENDING_POLICY:
                processSabrContextSendingPolicy(part);
                return null;
            case UMPPartId.RELOAD_PLAYER_RESPONSE:
                return processReloadPlayerResponse(part);
        }

        if (!contains(IGNORED_PARTS, part.partId)) {
            unknownPartTypes.add(part.partId);
        }

        Log.d(TAG, "Unhandled part type %s", part.partId);

        return null;
    }

    private List<? extends SabrPart> parseMultiPart(UMPPart part) {
        switch (part.partId) {
            case UMPPartId.LIVE_METADATA:
                return processLiveMetadata(part);
            case UMPPartId.SABR_SEEK:
                return processSabrSeek(part);
        }

        return null;
    }

    private MediaSegmentInitSabrPart processMediaHeader(UMPPart part) {
        MediaHeader mediaHeader;

        try {
            mediaHeader = MediaHeader.parseFrom(part.toStream());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        try {
            ProcessMediaHeaderResult result = processor.processMediaHeader(mediaHeader);

            return result.sabrPart;
        } catch (MediaSegmentMismatchError e) {
            // For livestreams, the server may not know the exact segment for a given player time.
            // For segments near stream head, it estimates using segment duration, which can cause off-by-one segment mismatches.
            // If a segment is much longer or shorter than expected, the server may return a segment ahead or behind.
            // In such cases, retry with an adjusted player time to resync.
            if (processor.isLive() && e.receivedSequenceNumber == e.expectedSequenceNumber - 1) {
                // The segment before the previous segment was possibly longer than expected.
                // Move the player time forward to try to adjust for this.
                ClientAbrState state = processor.getClientAbrState().toBuilder()
                        .setPlayerTimeMs(processor.getClientAbrState().getPlayerTimeMs() + processor.getLiveSegmentTargetDurationToleranceMs())
                        .build();
                processor.setClientAbrState(state);
                sqMismatchForwardCount += 1;
                return null;
            } else if (processor.isLive() && e.receivedSequenceNumber == e.expectedSequenceNumber + 2) {
                // The previous segment was possibly shorter than expected
                // Move the player time backwards to try to adjust for this.
                ClientAbrState state = processor.getClientAbrState().toBuilder()
                        .setPlayerTimeMs(Math.max(0, processor.getClientAbrState().getPlayerTimeMs() - processor.getLiveSegmentTargetDurationToleranceMs()))
                        .build();
                processor.setClientAbrState(state);
                sqMismatchBacktrackCount += 1;
                return null;
            }

            throw e;
        }
    }

    private MediaSegmentDataSabrPart processMedia(UMPPart part) {
        try {
            long position = part.data.getPosition();
            long headerId = decoder.readVarInt(part.data);
            long offset = part.data.getPosition() - position;
            int contentLength = part.size - (int) offset;

            ProcessMediaResult result = processor.processMedia(headerId, contentLength, part.data);

            return result.sabrPart;
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private MediaSegmentEndSabrPart processMediaEnd(UMPPart part) {
        try {
            long headerId = decoder.readVarInt(part.data);
            Log.d(TAG, "Header ID: %s", headerId);

            ProcessMediaEndResult result = processor.processMediaEnd(headerId);

            if (result.isNewSegment) {
                receivedNewSegments = true;
            }

            return result.sabrPart;
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private PoTokenStatusSabrPart processStreamProtectionStatus(UMPPart part) {
        StreamProtectionStatus sps;

        try {
            sps = StreamProtectionStatus.parseFrom(part.toStream());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        Log.d(TAG, "Process StreamProtectionStatus: %s", sps);
        ProcessStreamProtectionStatusResult result = processor.processStreamProtectionStatus(sps);

        return result.sabrPart;
    }

    private void processSabrRedirect(UMPPart part) {
        SabrRedirect sabrRedirect;

        try {
            sabrRedirect = SabrRedirect.parseFrom(part.toStream());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        Log.d(TAG, "Process SabrRedirect: %s", sabrRedirect);

        if (!sabrRedirect.hasRedirectUrl()) {
            Log.d(TAG, "Server requested to redirect to an invalid URL");
            return;
        }

        setUrl(sabrRedirect.getRedirectUrl());
    }

    private FormatInitializedSabrPart processFormatInitializationMetadata(UMPPart part) {
        FormatInitializationMetadata fmtInitMetadata;

        try {
            fmtInitMetadata = FormatInitializationMetadata.parseFrom(part.toStream());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        Log.d(TAG, "Process FormatInitializationMetadata: %s", fmtInitMetadata);
        ProcessFormatInitializationMetadataResult result = processor.processFormatInitializationMetadata(fmtInitMetadata);

        return result.sabrPart;
    }

    private void processNextRequestPolicy(UMPPart part) {
        NextRequestPolicy nextRequestPolicy;

        try {
            nextRequestPolicy = NextRequestPolicy.parseFrom(part.toStream());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        Log.d(TAG, "Process NextRequestPolicy: %s", nextRequestPolicy);
        processor.processNextRequestPolicy(nextRequestPolicy);
    }

    private void processSabrError(UMPPart part) {
        SabrError sabrError;

        try {
            sabrError = SabrError.parseFrom(part.toStream());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        Log.d(TAG, "Process SabrError: %s", sabrError);
        throw new SabrStreamError(String.format("SABR Protocol Error: %s", sabrError));
    }

    private void processSabrContextUpdate(UMPPart part) {
        SabrContextUpdate sabrCtxUpdate;

        try {
            sabrCtxUpdate = SabrContextUpdate.parseFrom(part.toStream());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        Log.d(TAG, "Process SabrContextUpdate: %s", sabrCtxUpdate);
        processor.processSabrContextUpdate(sabrCtxUpdate);
    }

    private void processSabrContextSendingPolicy(UMPPart part) {
        SabrContextSendingPolicy sabrCtxSendingPolicy;

        try {
            sabrCtxSendingPolicy = SabrContextSendingPolicy.parseFrom(part.toStream());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        Log.d(TAG, "Process SabrContextSendingPolicy: %s", sabrCtxSendingPolicy);
        processor.processSabrContextSendingPolicy(sabrCtxSendingPolicy);
    }

    private RefreshPlayerResponseSabrPart processReloadPlayerResponse(UMPPart part) {
        ReloadPlayerResponse reloadPlayerResponse;

        try {
            reloadPlayerResponse = ReloadPlayerResponse.parseFrom(part.toStream());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        Log.d(TAG, "Process ReloadPlayerResponse: %s", reloadPlayerResponse);
        return new RefreshPlayerResponseSabrPart(
                RefreshPlayerResponseSabrPart.Reason.SABR_RELOAD_PLAYER_RESPONSE,
                reloadPlayerResponse.hasReloadPlaybackParams() && reloadPlayerResponse.getReloadPlaybackParams().hasToken()
                        ? reloadPlayerResponse.getReloadPlaybackParams().getToken() : null
        );
    }

    private List<MediaSeekSabrPart> processLiveMetadata(UMPPart part) {
        LiveMetadata liveMetadata;

        try {
            liveMetadata = LiveMetadata.parseFrom(part.toStream());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        Log.d(TAG, "Process LiveMetadata: %s", liveMetadata);
        return processor.processLiveMetadata(liveMetadata).seekSabrParts;
    }

    private List<MediaSeekSabrPart> processSabrSeek(UMPPart part) {
        SabrSeek sabrSeek;

        try {
            sabrSeek = SabrSeek.parseFrom(part.toStream());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        Log.d(TAG, "Process SabrSeek: %s", sabrSeek);
        return processor.processSabrSeek(sabrSeek).seekSabrParts;
    }

    public static boolean contains(int[] array, int value) {
        for (int num : array) {
            if (num == value) {
                return true;
            }
        }
        return false;
    }

    private UMPPart nextKnownUMPPart(@NonNull ExtractorInput extractorInput) {
        UMPPart part;

        while (true) {
            part = decoder.decode(extractorInput);

            if (part == null) {
                break;
            }

            if (contains(KNOWN_PARTS, part.partId)) {
                break;
            } else {
                Log.d(TAG, "Unknown part encountered: %s", part.partId);
            }
        }

        return part;
    }

    private void setUrl(String url) {
        Log.d(TAG, "New URL: %s", url);
        UrlQueryString newQueryString = UrlQueryStringFactory.parse(url);
        UrlQueryString oldQueryString = UrlQueryStringFactory.parse(this.url);
        String bn = newQueryString.get("id");
        String bc = oldQueryString.get("id");
        if (processor.isLive() && this.url != null && !Helpers.equals(bn, bc)) {
            throw new SabrStreamError(String.format("Broadcast ID changed from %s to %s. The download will need to be restarted.", bc, bn));
        }
        this.url = url;
        if (Helpers.equals(newQueryString.get("source"), "yt_live_broadcast")) {
            processor.setLive(true);
        }
    }
}
