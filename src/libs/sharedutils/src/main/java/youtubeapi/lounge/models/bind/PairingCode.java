package minefarts.sharedutils.lounge.models.bind;

import minefarts.googlecommon.common.converters.regexp.RegExp;
import minefarts.googlecommon.common.helpers.ServiceHelper;

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
