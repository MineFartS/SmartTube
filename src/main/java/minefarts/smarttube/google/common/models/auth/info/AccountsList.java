package minefarts.smarttube.google.common.models.auth.info;

import minefarts.smarttube.google.common.converters.jsonpath.JsonPath;

import java.util.List;

public class AccountsList {
    @JsonPath("$.contents[0].accountSectionListRenderer.contents[0].accountItemSectionRenderer.contents[*].accountItem")
    private List<AccountInt> mAccounts;

    public List<AccountInt> getAccounts() {
        return mAccounts;
    }
}
