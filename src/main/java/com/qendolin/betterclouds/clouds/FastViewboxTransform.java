package com.qendolin.betterclouds.clouds;

import com.qendolin.betterclouds.Config;
import org.joml.Matrix4f;

public class FastViewboxTransform implements IViewboxTransform {

    private final Matrix4f projection = new Matrix4f();
    private double farPlane;
    private double nearPlane;

    @Override
    public boolean isInvalid() {
        return false;
    }

    @Override
    public Matrix4f getProjection() {
        return projection;
    }

    @Override
    public double farPlane() {
        return farPlane;
    }

    @Override
    public double nearPlane() {
        return nearPlane;
    }

    @Override
    public double minFarPlane() {
        return farPlane;
    }

    @Override
    public double maxNearPlane() {
        return nearPlane;
    }

    @Override
    public double linearizeFactor() {
        return ((farPlane-nearPlane)/(2*farPlane*nearPlane));
    }

    @Override
    public double inverseLinearizeFactor() {
        return ((farPlane-nearPlane)/(2*farPlane*nearPlane));
    }

    @Override
    public double linearizeAddend() {
        return -(farPlane+nearPlane)/(2*farPlane*nearPlane);
    }

    @Override
    public double inverseLinearizeAddend() {
        return -(farPlane+nearPlane)/(2*farPlane*nearPlane);
    }

    @Override
    public double hyperbolizeFactor() {
        return (2*farPlane*nearPlane)/(farPlane-nearPlane);
    }

    @Override
    public double inverseHyperbolizeFactor() {
        return (2*farPlane*nearPlane)/(farPlane-nearPlane);
    }

    @Override
    public double hyperbolizeAddend() {
        return (farPlane+nearPlane)/(farPlane-nearPlane);
    }

    @Override
    public double inverseHyperbolizeAddend() {
        return (farPlane+nearPlane)/(farPlane-nearPlane);
    }

    @Override
    public void update(Matrix4f projection, float cameraY, float pitchDeg, float cloudsHeight, Config generatorConfig) {
        double m11 = projection.m32();
        double m10 = projection.m22();
        farPlane = m11 / (m10 + 1);
        nearPlane = m11 / (m10 - 1);

        this.projection.set(projection);
        this.projection.m22((float) (-(farPlane+nearPlane)/(farPlane-nearPlane)));
        this.projection.m32((float) (-(2*farPlane*nearPlane)/(farPlane-nearPlane)));
    }
}
