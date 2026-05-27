package minefarts.sharedutils.lounge.models.commands;

import minefarts.googlecommon.common.converters.jsonpath.JsonPath;

public class RemoteParams {
    private static final String PAIRING_TYPE_MANUAL = "manual";

    @JsonPath("$.id")
    private String mDeviceId;

    @JsonPath("$.name")
    private String mDeviceName;

    @JsonPath("$.pairingType")
    private String mPairingType;

    public String getDeviceId() {
        return mDeviceId;
    }

    public String getDeviceName() {
        return mDeviceName;
    }

    public String getPairingType() {
        return mPairingType;
    }

    public boolean isPairedManually() {
        return PAIRING_TYPE_MANUAL.equals(mPairingType);
    }
}
