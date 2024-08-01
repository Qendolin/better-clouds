package com.qendolin.betterclouds.compat;

public class IrisCompatStub extends IrisCompat {
    @Override
    public boolean isShadersEnabled() {
        return false;
    }

    @Override
    public boolean isFrustumCullingDisabled() {
        return false;
    }

    @Override
    public void bindFramebuffer() {

    }
}
