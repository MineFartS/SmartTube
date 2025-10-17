package com.liskovsoft.smartyoutubetv2.common.app.views;

/** View used to show pairing code / flow for remote device pairing. */
public interface AddDeviceView {
    void showCode(String userCode);
    void close();
}
