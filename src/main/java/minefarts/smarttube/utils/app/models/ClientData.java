package minefarts.smarttube.utils.app.models;

import minefarts.smarttube.google.common.converters.regexp.RegExp;

/**
 * Data contained inside m=base js file (modern clients) or m=main js file (Cobalt/Legacy)<br/>
 * NOTE: Same pattern can be encountered 3 times or more<br/>
 * We should use the first one because other ones contain wrong client id.
 */
public class ClientData {

    /**
     *  We need first occurrence of the pattern (Android TV device).<br/>
     *  NOTE: old patterns (if match found) contain wrong client id (Http 401 error).
     */
    @RegExp({
        "clientId:\"([-\\w]+\\.apps\\.googleusercontent\\.com)\",\\n?[$\\w]+:\"\\w+\""
    })
    public String mClientId;

    /**
     *  We need first occurrence of the pattern (Android TV device).<br/>
     *  NOTE: old patterns (if match found) contain wrong client id (Http 401 error).
     */
    @RegExp({
        "clientId:\"[-\\w]+\\.apps\\.googleusercontent\\.com\",\\n?[$\\w]+:\"(\\w+)\""
    })
    public String mClientSecret;

}
