package minefarts.smarttube.utils.lounge.models.bind;

import minefarts.smarttube.google.common.converters.regexp.RegExp;

public class ScreenId {
    @RegExp(".*")
    private String mScreenId;

    public String getScreenId() {
        return mScreenId;
    }
}
