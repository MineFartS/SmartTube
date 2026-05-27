package minefarts.smarttube.utils.lounge.models.info;

import minefarts.smarttube.google.common.converters.jsonpath.JsonPath;
import minefarts.smarttube.google.common.helpers.ServiceHelper;

public class PairingCodeV2 {
    @JsonPath("$.code")
    private String mPairingCode;
    private String mPairingCodeAlt;

    public String getPairingCode() {
        if (mPairingCode == null) {
            return null;
        }

        // Format pairing code to XXX-XXX-XXX-XXX
        if (mPairingCodeAlt == null) {
            mPairingCodeAlt = ServiceHelper.insertSeparator(mPairingCode, " ", 3);
        }

        return mPairingCodeAlt;
    }
}
