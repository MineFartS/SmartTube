package minefarts.smarttube.utils.lounge;

import minefarts.smarttube.utils.helpers.AppInfoHelpers;
import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.utils.mylogger.Log;
import minefarts.smarttube.utils.prefs.GlobalPreferences;
import minefarts.smarttube.google.common.converters.jsonpath.typeadapter.JsonPathTypeAdapter;
import minefarts.smarttube.google.common.helpers.RetrofitHelper;
import minefarts.smarttube.google.common.helpers.ServiceHelper;
import minefarts.smarttube.utils.lounge.models.bind.ScreenId;
import minefarts.smarttube.utils.lounge.models.commands.CommandItem;
import minefarts.smarttube.utils.lounge.models.commands.CommandList;
import minefarts.smarttube.utils.lounge.models.commands.PlaylistParams;
import minefarts.smarttube.utils.lounge.models.info.PairingCodeV2;
import minefarts.smarttube.utils.lounge.models.info.TokenInfo;
import minefarts.smarttube.utils.lounge.models.info.TokenInfoList;
import minefarts.smarttube.utils.service.internal.MediaServiceData;
import minefarts.smarttube.google.common.converters.jsonpath.typeadapter.JsonPathSkipTypeAdapter;
import minefarts.smarttube.google.common.converters.jsonpath.typeadapter.JsonPathTypeAdapter;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;

import retrofit2.Call;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.JsonPath;

public class LoungeService {
    
    private static final String TAG = LoungeService.class.getSimpleName();
    private static final long TOKEN_ACTIVE_TIME_MS = 30 * 60 * 1_000;

    private static LoungeService sInstance;

    BindManager mBindManager;
    InfoManager mInfoManager;
    CommandManager mCommandManager;
    JsonPathTypeAdapter<CommandList> mLineSkipAdapter;
    
    private String mScreenName;
    private String mLoungeToken;
    private String mDeviceId;
    private long mTokenInitTimeMs;
    private String mScreenId;
    private String mSessionId;
    private String mGSessionId;
    private String mCtt;
    private String mPlaylistIndex;
    private String mPlaylistId;

    public static LoungeService instance() {
        if (sInstance == null) {
            sInstance = new LoungeService();
            sInstance.mBindManager = RetrofitHelper.create(BindManager.class);
            sInstance.mInfoManager = RetrofitHelper.create(InfoManager.class);
            sInstance.mCommandManager = RetrofitHelper.create(CommandManager.class);

            Configuration conf = Configuration
                .builder()
                .mappingProvider(new GsonMappingProvider())
                .jsonProvider(new GsonJsonProvider())
                .build();

            sInstance.mLineSkipAdapter = new JsonPathSkipTypeAdapter<>(
                JsonPath.using(conf), 
                CommandList.class
            );
        }

        return sInstance;
    }

    public String getPairingCode() {
        initConstants();

        return getPairingCodeInt();
    }

    private String getPairingCodeInt() {
        if (mLoungeToken == null || mScreenId == null) {
            return null;
        }

        Call<PairingCodeV2> pairingCodeWrapper = mInfoManager.getPairingCodeV2(
                mLoungeToken,
                mScreenId,
                mScreenName,
                BindParams.ACCESS_TYPE,
                BindParams.APP,
                mDeviceId,
                BindParams.QR);
        PairingCodeV2 pairingCode = RetrofitHelper.get(pairingCodeWrapper);

        mLoungeToken = null; // apply changes (restart the service)

        // Pairing code XXX-XXX-XXX-XXX
        return pairingCode != null ? pairingCode.getPairingCode() : null;
    }

    /**
     * Process couldn't be stopped, only interrupted.
     */
    public void startListening(OnCommand callback) {
        // It's common to stream to be interrupted multiple times
        while (true) {
            try {
                initConstants();
                startListeningInt(callback);
                Thread.sleep(3_000); // fix too frequent request
            } catch (SocketTimeoutException e) {
                Log.e(TAG, "Connection hanged. Reconnecting...");
            } catch (InterruptedIOException e) {
                Log.e(TAG, "Oops. Stopping. Listening thread interrupted.");
                break;
            } catch (InterruptedException e) {
                Log.e(TAG, "Oops. Stopping. Listening thread interrupted.");
                break;
            } catch (NullPointerException e) {
                Log.e(TAG, "Oops. Stopping. Got NPE.");
                e.printStackTrace();
                break;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                // Continue to listen whichever is happening.
            }
        }
    }

    private void initConstants() {
        if (mScreenName == null) {
            mScreenName = String.format(
                    "%s (%s)",
                    Helpers.getUserDeviceName(GlobalPreferences.sInstance.getContext()),
                    AppInfoHelpers.getAppLabel(GlobalPreferences.sInstance.getContext())
            );
        }

        if (mLoungeToken == null || isTokenOutdated()) {
            TokenInfo screen = getTokenInfo();

            if (screen != null) {
                mLoungeToken = screen.getLoungeToken();
                mScreenId = screen.getScreenId();
                mTokenInitTimeMs = System.currentTimeMillis();
            }
        }

        if (mDeviceId == null) {
            mDeviceId = MediaServiceData.instance().getDeviceId();
        }
    }

    private void startListeningInt(OnCommand callback) throws IOException {
        Log.d(TAG, "Opening session...");

        CommandList sessionInfos = getSessionBind();

        if (sessionInfos == null) {
            Log.e(TAG, "Can't open a session because it's empty. Expired lounge token or too frequent request?");
            mLoungeToken = null;
            return;
        }

        mSessionId = sessionInfos.getParam(CommandItem.TYPE_SESSION_ID);
        mGSessionId = sessionInfos.getParam(CommandItem.TYPE_G_SESSION_ID);

        Log.d(TAG, "SID: %s, gsessionid: %s", mSessionId, mGSessionId);

        String url = BindParams.createBindRpcUrl(
                mScreenName,
                mDeviceId,
                mLoungeToken,
                mSessionId,
                mGSessionId);
        Request request = new Builder().url(url).build();
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        // No command during one minute could be a sign of hanged connection.
        builder.readTimeout(60_000, TimeUnit.MILLISECONDS);

        OkHttpClient client = builder.build();

        Log.d(TAG, "Starting read session...");

        Response response = client.newCall(request).execute();

        InputStream in = response.body().byteStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String result = "";
        String line = "";

        // Skip initial commands: TYPE_SESSION_ID, TYPE_G_SESSION_ID, TYPE_LOUNGE_STATUS, TYPE_GET_NOW_PLAYING
        //processCommands(sessionInfos, callback);

        while((line = reader.readLine()) != null) {
            if (mLoungeToken == null) {
                // restart service
                break;
            }

            result += line + "\n";

            boolean isLastLine = line.equals("]") && !result.endsWith("\"noop\"]\n]\n");

            if (isLastLine) {
                Log.d(TAG, "New command: \n" + result);

                CommandList infos = toCommandInfos(result);

                processCommands(infos, callback);

                result = "";
            }
        }

        Log.d(TAG, "Closing session...");

        response.body().close();
    }

    private void processCommands(CommandList commandList, OnCommand callback) {
        for (CommandItem commandItem : commandList.getCommands()) {
            updateData(commandItem);
            callback.onCommand(commandItem);
        }
    }

    public void postStartPlaying(String videoId, long positionMs, long durationMs, boolean isPlaying) {
        Log.d(TAG, "Post nowPlaying id: %s, pos: %s, dur: %s...", videoId, positionMs, durationMs);
        postCommand(CommandParams.getNowPlaying(videoId, positionMs, durationMs, mCtt, mPlaylistId, mPlaylistIndex));
        postStateChange(positionMs, durationMs, isPlaying);
    }

    public void postStateChange(long positionMs, long durationMs, boolean isPlaying) {
        // Live stream fix (negative position)
        if (positionMs < 0) {
            positionMs = Math.abs(positionMs);
        }

        if (durationMs > 0 && positionMs <= durationMs) {
            Log.d(TAG, "Post onStateChange pos: %s, dur: %s, playing: %s...", positionMs, durationMs, isPlaying);

            Map<String, String> stateChange = CommandParams.getOnStateChange(
                    positionMs,
                    durationMs,
                    isPlaying ? CommandParams.STATE_PLAYING : CommandParams.STATE_PAUSED
            );

            postCommand(stateChange);
        }
    }

    public void postVolumeChange(int volume) {
        if (volume == -1) return;
        Log.d(TAG, "Post onVolumeChanged: %s...", volume);
        postCommand(CommandParams.getOnVolumeChanged(volume));
    }

    public void resetData() {
        MediaServiceData.instance().setScreenId(null);
        MediaServiceData.instance().setDeviceId(null);
        mLoungeToken = null;
    }

    private void updateData(CommandItem info) {
        if (info != null && info.getPlaylistParams() != null) {
            PlaylistParams playlistData = info.getPlaylistParams();
            mCtt = playlistData.getCtt() != null ? playlistData.getCtt() : mCtt;
            mPlaylistIndex = playlistData.getPlaylistIndex() != null ? playlistData.getPlaylistIndex() : mPlaylistIndex;
            mPlaylistId = playlistData.getPlaylistId() != null ? playlistData.getPlaylistId() : mPlaylistId;
        }
    }

    private TokenInfo getTokenInfo() {
        TokenInfo tokenInfo = null;
        String screenId = MediaServiceData.instance().getScreenId();

        if (screenId == null) {
            Call<ScreenId> screenIdWrapper = mBindManager.createScreenId();
            ScreenId screenIdContainer = RetrofitHelper.get(screenIdWrapper);
            if (screenIdContainer != null) {
                screenId = screenIdContainer.getScreenId();
                MediaServiceData.instance().setScreenId(screenId);
            }
        }

        if (screenId != null) {
            Call<TokenInfoList> tokenInfoListWrapper = mInfoManager.getTokenInfo(screenId);
            TokenInfoList tokenInfoList = RetrofitHelper.get(tokenInfoListWrapper);

            if (tokenInfoList != null && tokenInfoList.getTokenInfos() != null) {
                tokenInfo = tokenInfoList.getTokenInfos().get(0);
            }
        }

        return tokenInfo;
    }

    private CommandList getSessionBind() {
        Call<CommandList> bindDataWrapper = mCommandManager.getSessionData(mScreenName, mDeviceId, mLoungeToken, 0);

        return RetrofitHelper.get(bindDataWrapper);
    }

    private CommandList toCommandInfos(String result) {
        return mLineSkipAdapter.read(new ByteArrayInputStream(result.getBytes(Charset.forName("UTF-8"))));
    }

    private void postCommand(Map<String, String> command) {
        if (!ServiceHelper.checkNonNull(mSessionId, mGSessionId)) {
            Log.e(TAG, "Can't send command. Error: mSessionId, mGSessionId is null");
            return;
        }

        Call<Void> wrapper = mCommandManager.postCommand(
                mScreenName, mDeviceId, mLoungeToken, mSessionId, mGSessionId,
                command);
        RetrofitHelper.get(wrapper);
    }

    private boolean isTokenOutdated() {
        return System.currentTimeMillis() - mTokenInitTimeMs > TOKEN_ACTIVE_TIME_MS;
    }

    public interface OnCommand {
        void onCommand(CommandItem info);
    }
}
