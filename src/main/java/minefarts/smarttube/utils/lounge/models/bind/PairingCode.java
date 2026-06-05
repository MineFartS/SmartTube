package minefarts.smarttube.utils.lounge.models.bind;

import minefarts.smarttube.google.common.converters.regexp.RegExp;
import minefarts.smarttube.google.common.helpers.ServiceHelper;

public class PairingCode {
    @RegExp(".*")
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
