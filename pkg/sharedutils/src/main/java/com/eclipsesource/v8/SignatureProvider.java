package com.liskovsoft.sharedutils;

public interface SignatureProvider {
    public byte[] getSignature(String uri);
}
