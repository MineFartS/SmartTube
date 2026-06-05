package minefarts.smarttube.utils.app.models;

import androidx.annotation.NonNull;

import minefarts.smarttube.utils.helpers.Helpers;
import minefarts.smarttube.google.common.converters.FieldNullable;
import minefarts.smarttube.google.common.converters.regexp.RegExp;

import java.util.regex.Pattern;

/**
 * Parser for https://www.youtube.com/s/player/11aba956/tv-player-ias.vflset/tv-player-ias.js
 */
public class PlayerData {

    // Begin DecipherFunction

    private static final Pattern SIGNATURE_DECIPHER = Pattern.compile("function [$\\w]+\\(([\\w])\\)");

    private static final String DELIM = "%pdc%";
    private final String mPlayerUrl;

    public PlayerData(
        String playerUrl,
        String decipherFunction,
        String signatureTimestamp
    ) {
        mPlayerUrl = playerUrl;
        mDecipherFunction = decipherFunction;
        mSignatureTimestamp = signatureTimestamp;
    }

    /**
     * Return JS decipher function as string.<br/>
     * Used when deciphering music items.<br/>
     * Player url example: <b>https://www.youtube.com/s/player/e49bfb00/tv-player-ias.vflset/tv-player-ias.js</b>
     */
    @RegExp({
        ";\\w+ [$\\w]+=\\{[\\S\\s]{10,200}?[\\w]\\.reverse\\(\\)[\\S\\s]*?function [$\\w]+\\([\\w]\\)\\{.*[\\w]\\.split\\(\"\"\\).*;return [\\w]\\.join\\(\"\"\\)\\}",
    })
    private String mDecipherFunction;

    @RegExp({
            ";\\w+ [$\\w]+=\\{[\\S\\s]{10,200}?[\\w]\\.reverse\\(\\)[\\S\\s]*?function [$\\w]+\\([\\w]\\)\\{.*[\\w]\\.split\\(.+\\).*;return [\\w]\\.join\\([$\\w]+\\[\\d+\\]\\)\\}",
    })
    private String mDecipherFunctionPart1;

    @RegExp({
            "'use strict';(var [$\\w]+=[.\\S\\s]+?\\.split\\(.+?\\);)",
    })
    private String mDecipherFunctionPart2;

    public String getDecipherFunction() {
        String deFunc = null;

        if (mDecipherFunction != null) {
            deFunc = Helpers.replace(mDecipherFunction, SIGNATURE_DECIPHER, "function decipherSignature($1)");
        } else if (mDecipherFunctionPart1 != null && mDecipherFunctionPart2 != null) {
            deFunc = Helpers.replace(mDecipherFunctionPart1, SIGNATURE_DECIPHER, "function decipherSignature($1)") + ";" + mDecipherFunctionPart2;
        }

        return deFunc;
    }

    // End DecipherFunction

    // Begin SignatureTimestamp

    @RegExp("signatureTimestamp:(\\d+)")
    private String mSignatureTimestamp;

    public String getSignatureTimestamp() {
        return mSignatureTimestamp;
    }

    // End SignatureTimestamp

    public static PlayerData fromString(String spec) {
        
        if (spec == null) return null;

        String[] split = Helpers.split(spec, DELIM);

        return new PlayerData(
            Helpers.parseStr(split, 0),
            Helpers.parseStr(split, 3),
            Helpers.parseStr(split, 4)
        );
        
    }
    
    @NonNull
    @Override
    public String toString() {
        return Helpers.merge(DELIM, mPlayerUrl, mDecipherFunction, mSignatureTimestamp);
    }

    public static PlayerData from(String playerUrl, PlayerData playerData) {
        
        if (playerData == null) return null;

        return new PlayerData(
            playerUrl,
            playerData.getDecipherFunction(),
            playerData.getSignatureTimestamp()
        );

    }

    public String getPlayerUrl() {
        return mPlayerUrl;
    }

    public boolean validate() {
        return mDecipherFunction != null && mSignatureTimestamp != null;
    }
}
