package minefarts.smarttube.utils.lounge.models.info;

import minefarts.smarttube.google.common.converters.jsonpath.JsonPath;

import java.util.List;

public class TokenInfoList {
    @JsonPath("$.screens[*]")
    private List<TokenInfo> mTokenInfos;

    public List<TokenInfo> getTokenInfos() {
        return mTokenInfos;
    }
}
