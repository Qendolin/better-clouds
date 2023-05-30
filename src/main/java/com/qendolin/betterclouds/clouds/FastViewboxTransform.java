package com.qendolin.betterclouds.clouds;

import com.qendolin.betterclouds.Config;
import net.minecraft.client.MinecraftClient;
import org.joml.Matrix4f;

public class FastViewboxTransform implements IViewboxTransform {

    private final Matrix4f projection = new Matrix4f();

    @Override
    public boolean isInvalid() {
        return false;
    }

    @Override
    public Matrix4f getProjection() {
        return projection;
    }

    @Override
    public double linearizeFactor() {
        return 1;
    }

    @Override
    public double inverseLinearizeFactor() {
        return 1;
    }

    @Override
    public double linearizeAddend() {
        return 0;
    }

    @Override
    public double inverseLinearizeAddend() {
        return 0;
    }

    @Override
    public double hyperbolizeFactor() {
        return 1;
    }

    @Override
    public double inverseHyperbolizeFactor() {
        return 1;
    }

    @Override
    public double hyperbolizeAddend() {
        return 0;
    }

    @Override
    public double inverseHyperbolizeAddend() {
        return 0;
    }

    @Override
    public void update(Matrix4f projection, float cameraY, float pitchDeg, float cloudsHeight, Config generatorConfig) {
        this.projection.set(projection);
    }
}
