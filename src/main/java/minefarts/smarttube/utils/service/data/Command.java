package minefarts.smarttube.utils.service.data;

import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.google.common.helpers.ServiceHelper;
import minefarts.smarttube.utils.lounge.models.commands.CommandItem;
import minefarts.smarttube.utils.lounge.models.commands.RemoteParams;
import minefarts.smarttube.utils.lounge.models.commands.SeekToParams;
import minefarts.smarttube.utils.lounge.models.commands.PlaylistParams;
import minefarts.smarttube.utils.lounge.models.commands.VoiceParams;
import minefarts.smarttube.utils.lounge.models.commands.VolumeParams;

public class Command {

    public static final int TYPE_UNDEFINED = -1;
    public static final int TYPE_OPEN_VIDEO = 0;
    public static final int TYPE_SEEK = 1;
    public static final int TYPE_PLAY = 2;
    public static final int TYPE_PAUSE = 3;
    public static final int TYPE_GET_STATE = 4;
    public static final int TYPE_CONNECTED = 5;
    public static final int TYPE_DISCONNECTED = 6;
    public static final int TYPE_UPDATE_PLAYLIST = 7;
    public static final int TYPE_NEXT = 8;
    public static final int TYPE_PREVIOUS = 9;
    public static final int TYPE_VOLUME = 10;
    public static final int TYPE_IDLE = 11;
    public static final int TYPE_STOP = 12;
    public static final int TYPE_DPAD = 13;
    public static final int TYPE_VOICE = 14;
    public static final int TYPE_SUBTITLES = 15;
    public static final int KEY_UNDEFINED = -1;
    public static final int KEY_UP = 0;
    public static final int KEY_DOWN = 1;
    public static final int KEY_LEFT = 2;
    public static final int KEY_RIGHT = 3;
    public static final int KEY_ENTER = 4;
    public static final int KEY_BACK = 5;

    private int mType = TYPE_UNDEFINED;
    private String mVideoId;
    private String mPlaylistId;
    private long mCurrentTimeMs;
    private String mDeviceName;
    private String mDeviceId;
    private int mPlaylistIndex;
    private int mVolume;
    private int mDelta;
    private int mKey = KEY_UNDEFINED;
    private boolean mIsVoiceStarted;
    private String mSubLanguageCode;

    public static Command from(CommandItem info) {
        if (info == null) return null;

        Command command = new Command();

        switch (info.getType()) {

            case CommandItem.TYPE_SET_PLAYLIST:
                command.mType = TYPE_OPEN_VIDEO;
                PlaylistParams playlistParams = info.getPlaylistParams();
                command.mVideoId = playlistParams.getVideoId();
                command.mPlaylistId = playlistParams.getPlaylistId();
                command.mPlaylistIndex = Helpers.parseInt(playlistParams.getPlaylistIndex());
                command.mCurrentTimeMs = ServiceHelper.toMillis(playlistParams.getCurrentTimeSec());
                break;

            case CommandItem.TYPE_UPDATE_PLAYLIST:
                command.mType = TYPE_UPDATE_PLAYLIST;
                PlaylistParams playlistParams2 = info.getPlaylistParams();
                command.mPlaylistId = playlistParams2.getPlaylistId();
                break;

            case CommandItem.TYPE_SEEK_TO:
                command.mType = TYPE_SEEK;
                SeekToParams seekToParams = info.getSeekToParams();
                command.mCurrentTimeMs = ServiceHelper.toMillis(seekToParams.getNewTimeSec());
                break;

            case CommandItem.TYPE_SET_VOLUME:
                command.mType = TYPE_VOLUME;
                VolumeParams volumeParams = info.getVolumeParams();
                command.mVolume = Helpers.parseInt(volumeParams.getVolume());
                command.mDelta = Helpers.parseInt(volumeParams.getDelta());
                break;

            case CommandItem.TYPE_PLAY:
                command.mType = TYPE_PLAY;
                break;

            case CommandItem.TYPE_PAUSE:
                command.mType = TYPE_PAUSE;
                break;

            case CommandItem.TYPE_NEXT:
                command.mType = TYPE_NEXT;
                break;

            case CommandItem.TYPE_PREVIOUS:
                command.mType = TYPE_PREVIOUS;
                break;

            case CommandItem.TYPE_GET_NOW_PLAYING:
                command.mType = TYPE_GET_STATE;
                break;

            case CommandItem.TYPE_STOP_VIDEO:
                command.mType = TYPE_STOP;
                break;

            case CommandItem.TYPE_REMOTE_CONNECTED:
                command.mType = TYPE_CONNECTED;
                RemoteParams remoteParams = info.getRemoteParams();
                command.mDeviceName = remoteParams.getDeviceName();
                command.mDeviceId = remoteParams.getDeviceId();
                break;

            case CommandItem.TYPE_REMOTE_DISCONNECTED:
                command.mType = TYPE_DISCONNECTED;
                RemoteParams remoteParams2 = info.getRemoteParams();
                command.mDeviceName = remoteParams2.getDeviceName();
                command.mDeviceId = remoteParams2.getDeviceId();
                break;

            case CommandItem.TYPE_NOOP:
                command.mType = TYPE_IDLE;
                break;

            case CommandItem.TYPE_DPAD:
                command.mType = TYPE_DPAD;
                switch (info.getKey()) {
                    case CommandItem.KEY_UP:
                        command.mKey = KEY_UP;
                        break;
                    case CommandItem.KEY_DOWN:
                        command.mKey = KEY_DOWN;
                        break;
                    case CommandItem.KEY_LEFT:
                        command.mKey = KEY_LEFT;
                        break;
                    case CommandItem.KEY_RIGHT:
                        command.mKey = KEY_RIGHT;
                        break;
                    case CommandItem.KEY_ENTER:
                        command.mKey = KEY_ENTER;
                        break;
                    case CommandItem.KEY_BACK:
                        command.mKey = KEY_BACK;
                        break;
                }
                break;

            case CommandItem.TYPE_VOICE:
                command.mType = TYPE_VOICE;
                VoiceParams voiceParams = info.getVoiceParams();
                command.mIsVoiceStarted = VoiceParams.STATUS_START.equals(voiceParams.getStatus());
                break;

            case CommandItem.TYPE_SUBTITLES:
                command.mType = TYPE_SUBTITLES;
                PlaylistParams playlistParams3 = info.getPlaylistParams();
                command.mVideoId = playlistParams3.getVideoId();
                command.mSubLanguageCode = playlistParams3.getLanguageCode();
                break;

        }

        return command;
    }

    public String getSubLanguageCode() {
        return mSubLanguageCode;
    }

    public int getType() {
        return mType;
    }

    public String getVideoId() {
        return mVideoId;
    }

    public String getPlaylistId() {
        return mPlaylistId;
    }

    public int getPlaylistIndex() {
        return mPlaylistIndex;
    }

    public long getCurrentTimeMs() {
        return mCurrentTimeMs;
    }

    public String getDeviceName() {
        return mDeviceName;
    }

    public String getDeviceId() {
        return mDeviceId;
    }

    public int getVolume() {
        return mVolume;
    }

    public int getDelta() {
        return mDelta;
    }

    public int getKey() {
        return mKey;
    }

    public boolean isVoiceStarted() {
        return mIsVoiceStarted;
    }
}
