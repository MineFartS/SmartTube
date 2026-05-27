package minefarts.sharedutils.lounge.models.bind;

import minefarts.googlecommon.common.converters.regexp.RegExp;

public class ScreenId {
    @RegExp(".*")
    private String mScreenId;

    public String getScreenId() {
        return mScreenId;
    }
}
