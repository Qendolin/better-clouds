package com.qendolin.betterclouds.clouds;

import com.qendolin.betterclouds.Config;
import org.joml.Matrix4f;

public interface IViewboxTransform {
    boolean isInvalid();

    Matrix4f getProjection();

    double farPlane();

    double nearPlane();

    double minFarPlane();

    double maxNearPlane();

    double linearizeFactor();

    double inverseLinearizeFactor();

    double linearizeAddend();

    double inverseLinearizeAddend();

    double hyperbolizeFactor();

    double inverseHyperbolizeFactor();

    double hyperbolizeAddend();

    double inverseHyperbolizeAddend();

    void update(Matrix4f projection, float cameraY, float pitchDeg, float cloudsHeight, Config generatorConfig);
}
