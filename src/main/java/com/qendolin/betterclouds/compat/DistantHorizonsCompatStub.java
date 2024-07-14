package com.qendolin.betterclouds.compat;

import org.joml.Matrix4f;

import java.util.Optional;

class DistantHorizonsCompatStub extends DistantHorizonsCompat {
    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public Matrix4f getProjectionMatrix() {
        return NOOP_MATRIX;
    }

    @Override
    public Optional<Integer> getDepthTextureId() {
        return Optional.empty();
    }

    @Override
    public void disableLodClouds() {

    }
}
