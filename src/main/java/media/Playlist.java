

package SmartTubeApp.media;

import androidx.tvprovider.media.tv.TvContractCompat;

import com.liskovsoft.googlecommon.common.helpers.ServiceHelper;
import SmartTubeApp.media.ClipService.GroupCallback;
import com.liskovsoft.sharedutils.service.ContentService;
import SmartTubeApp.misc.ServiceManager;
import com.liskovsoft.sharedutils.data.MediaGroup;
import com.liskovsoft.sharedutils.data.MediaItem;
import com.liskovsoft.sharedutils.helpers.Helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Playlist {
    private final String mName;
    private final String mDescription;
    private final String mVideoUri;
    private final String mBgImage;
    private final String mTitle;
    private final String mPlaylistId;
    private List<Clip> mClips;
    private boolean mChannelPublished;
    private long mChannelId = -1;
    private static final String DELIM = ",";
    private String mChannelKey;
    private String mProgramsKey;
    private String mPlaylistUrl;
    private int mLogoResId = -1;
    private final GroupCallback mCallback;
    private final boolean mIsDefault;
    private static final int MIN_PLAYLIST_SIZE = 40;

    public Playlist(String name, String playlistId, GroupCallback callback) {
        this(name, playlistId, callback, false);
    }

    public Playlist(String name, String playlistId, GroupCallback callback, boolean isDefault) {
        this(name, Collections.emptyList(), playlistId, callback, isDefault);
    }

    public Playlist(String name, List<Clip> clips, String playlistId, GroupCallback callback, boolean isDefault) {
        mName = name;
        mTitle = "playlist title";
        mDescription = "playlist description";
        mVideoUri = "dsf";
        mBgImage = "asdf";
        mClips = clips;
        mPlaylistId = playlistId;
        mCallback = callback;
        mIsDefault = isDefault;
    }

    public String getName() {
        return mName;
    }

    public List<Clip> getClips() {
        if (mClips == null || mClips.isEmpty()) {
            mClips = createClips();
        }

        return mClips;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getPlaylistId() {
        return mPlaylistId;
    }

    /**
     * Google TV fix: no dialog to set-up the channels.
     */
    public boolean isDefault() {
        return mIsDefault;
    }

    public boolean isChannelPublished() {
        return mChannelPublished;
    }

    public void setIsPublished(boolean channelPublished) {
        mChannelPublished = channelPublished;
    }

    public void setPublishedId(long id) {
        mChannelPublished = true;
        mChannelId = id;
    }

    public long getPublishedId() {
        return mChannelId;
    }

    public String toString() {
        return "Playlist { mName = '" + mName + "' mDescription = '" + mDescription
                + "' mVideoUri = '" + mVideoUri + "' mBgImage = '" + mBgImage + "' mTitle = '"
                + mTitle + "' mList = '" + mClips + "' mId = '" + mPlaylistId
                + "' mChannelPublished" + mChannelPublished + "'";
    }

    public String getPublishedClipsIds() {
        StringBuilder result = new StringBuilder();

        if (mClips != null) {
            for (Clip c : mClips) {
                long programId = c.getProgramId();

                if (programId == -1) { // not published
                    continue;
                }

                if (result.length() == 0) {
                    result.append(programId);
                } else {
                    result.append(DELIM);
                    result.append(programId);
                }

            }
        }

        return result.toString();
    }

    private static List<Long> parseClipIds(String clipsIds) {
        List<Long> result = new ArrayList<>();

        if (clipsIds != null && !clipsIds.isEmpty()) {
            String[] split = clipsIds.split(DELIM);

            for (String id : split) {
                result.add(Long.parseLong(id));
            }
        }

        return result;
    }

    public void restoreClipsIds(String clipsIds) {
        if (clipsIds != null && mClips != null) {
            List<Long> ids = parseClipIds(clipsIds);
            for (int i = 0; i < ids.size(); i++) {
                if (mClips.size() > i) { // avoid index of bound exception
                    Clip clip = mClips.get(i);
                    clip.setProgramId(ids.get(i));
                }
            }
        }
    }

    private List<Clip> createClips() {
        if (mCallback == null) return null;

        ContentService contentService = ServiceManager.getContentService();
        MediaGroup selectedGroup = mCallback.call(contentService);

        if (selectedGroup != null) {
            List<MediaItem> mediaItems = selectedGroup.getMediaItems();
            List<Clip> clips;

            if (mediaItems != null && !mediaItems.isEmpty()) {
                for (int i = 0; i < 3; i++) {
                    if (mediaItems.size() >= MIN_PLAYLIST_SIZE) {
                        break;
                    }

                    MediaGroup mediaGroup = contentService.continueGroup(selectedGroup);
                    if (mediaGroup == null) {
                        break;
                    }
                    mediaItems.addAll(mediaGroup.getMediaItems());
                }

                // Fix duplicated items inside ATV channels???
                Helpers.removeDuplicates(mediaItems);

                clips = convertToClips(mediaItems);
            } else {
                clips = new ArrayList<>();
            }

            return clips;
        }

        return null;
    }

    @SuppressWarnings("RestrictedApi")
    private List<Clip> convertToClips(List<MediaItem> videos) {
        if (videos != null) {
            List<Clip> clips = new ArrayList<>();

            for (MediaItem v : videos) {
                clips.add(new Clip(
                        v.getTitle(),
                        Helpers.toString(v.getSecondTitle()),
                        v.getDurationMs(),
                        v.getBackgroundImageUrl(),
                        v.getCardImageUrl(),
                        ServiceHelper.videoIdToFullUrl(v.getVideoId()),
                        null,
                        false,
                        v.isLive(),
                        null,
                        Integer.toString(v.getId()),
                        null,
                        TvContractCompat.PreviewProgramColumns.ASPECT_RATIO_16_9));
            }

            return clips;
        }

        return null;
    }

    public void setChannelKey(String key) {
        mChannelKey = key;
    }

    public String getChannelKey() {
        return mChannelKey;
    }

    public void setProgramsKey(String key) {
        mProgramsKey = key;
    }

    public String getProgramsKey() {
        return mProgramsKey;
    }

    public String getPlaylistUrl() {
        return mPlaylistUrl;
    }

    public void setPlaylistUrl(String url) {
        mPlaylistUrl = url;
    }

    public int getLogoResId() {
        return mLogoResId;
    }

    public void setLogoResId(int resId) {
        mLogoResId = resId;
    }
}
