package minefarts.sharedutils.lounge.models.info;

import minefarts.googlecommon.common.converters.jsonpath.JsonPath;

import java.util.List;

public class TokenInfoList {
    @JsonPath("$.screens[*]")
    private List<TokenInfo> mTokenInfos;

    public List<TokenInfo> getTokenInfos() {
        return mTokenInfos;
    }
}
