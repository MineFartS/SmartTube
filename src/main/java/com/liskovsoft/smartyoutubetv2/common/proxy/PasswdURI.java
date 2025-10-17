package com.liskovsoft.smartyoutubetv2.common.proxy;

/**
 * Simple URI parser that extracts username/password from authority section if present.
 * Used to parse proxy URIs like: http://user:pass@host:port
 *
 * http_proxy=http://USERNAME:PASSWORD@PROXYIP:PROXYPORT
 */
public class PasswdURI {
    private final URI mURI;

    public PasswdURI(String uriString) throws URISyntaxException {
        mURI = new URI(uriString);
    }

    public String getScheme() {
        return mURI.getScheme();
    }

    public String getHost() {
        return mURI.getHost();
    }

    public int getPort() {
        return mURI.getPort();
    }

    public String getUsername() {
        String authority = mURI.getAuthority();
        String[] split = authority.split("@");

        String result = null;

        if (split.length == 2) {
            String[] split2 = split[0].split(":");

            if (split2.length == 2) {
                result = split2[0];
            }
        }

        return result;
    }

    public String getPassword() {
        String authority = mURI.getAuthority();
        String[] split = authority.split("@");
        
        String result = null;

        if (split.length == 2) {
            String[] split2 = split[0].split(":");

            if (split2.length == 2) {
                result = split2[1];
            }
        }

        return result;
    }
}
