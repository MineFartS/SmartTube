package minefarts.sharedutils.app.models.cached;

import androidx.annotation.NonNull;

import minefarts.sharedutils.helpers.Helpers;
import minefarts.sharedutils.app.models.PlayerData;

public class PlayerDataCached extends PlayerData {
    private static final String DELIM = "%pdc%";
    private final String mPlayerUrl;
    private final String mDecipherFunction;
    private final String mSignatureTimestamp;

    public PlayerDataCached(
        String playerUrl,
        String decipherFunction,
        String signatureTimestamp
    ) {
        mPlayerUrl = playerUrl;
        mDecipherFunction = decipherFunction;
        mSignatureTimestamp = signatureTimestamp;
    }

    public static PlayerDataCached fromString(String spec) {
        
        if (spec == null) return null;

        String[] split = Helpers.split(spec, DELIM);

        return new PlayerDataCached(
            Helpers.parseStr(split, 0),
            Helpers.parseStr(split, 3),
            Helpers.parseStr(split, 4)
        );
        
    }

    public static PlayerDataCached from(String playerUrl, PlayerData playerData) {
        
        if (playerData == null) return null;

        return new PlayerDataCached(
            playerUrl,
            playerData.getDecipherFunction(),
            playerData.getSignatureTimestamp()
        );

    }

    @NonNull
    @Override
    public String toString() {
        return Helpers.merge(DELIM, mPlayerUrl, mDecipherFunction, mSignatureTimestamp);
    }

    @Override
    public String getDecipherFunction() {
        return mDecipherFunction;
    }

    @Override
    public String getSignatureTimestamp() {
        return mSignatureTimestamp;
    }

    public String getPlayerUrl() {
        return mPlayerUrl;
    }

    public boolean validate() {
        return mDecipherFunction != null && mSignatureTimestamp != null;
    }
}
