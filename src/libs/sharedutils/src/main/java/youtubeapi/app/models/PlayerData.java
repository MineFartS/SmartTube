package minefarts.sharedutils.app.models;

import minefarts.sharedutils.helpers.Helpers;
import minefarts.googlecommon.common.converters.FieldNullable;
import minefarts.googlecommon.common.converters.regexp.RegExp;

import java.util.regex.Pattern;

/**
 * Parser for https://www.youtube.com/s/player/11aba956/tv-player-ias.vflset/tv-player-ias.js
 */
public class PlayerData {

    // Begin DecipherFunction

    private static final Pattern SIGNATURE_DECIPHER = Pattern.compile("function [$\\w]+\\(([\\w])\\)");

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
}
