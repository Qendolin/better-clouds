package com.qendolin.betterclouds.clouds;

import com.qendolin.betterclouds.Config;
import com.qendolin.betterclouds.Main;
import org.joml.Matrix4f;


public class QualityViewboxTransform implements IViewboxTransform {
    private double farPlane;
    private double nearPlane;
    private double minFarPlane;
    private double maxNearPlane;
    private final Matrix4f projection = new Matrix4f();
    private boolean invalid = false;

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
        return minFarPlane;
    }

    @Override
    public double maxNearPlane() {
        return maxNearPlane;
    }

    @Override
    public double linearizeFactor() {
        return ((farPlane-nearPlane)/(2*farPlane*nearPlane));
    }

    @Override
    public double inverseLinearizeFactor() {
        return ((minFarPlane-maxNearPlane)/(2*minFarPlane*maxNearPlane));
    }

    @Override
    public double linearizeAddend() {
        return -(farPlane+nearPlane)/(2*farPlane*nearPlane);
    }

    @Override
    public double inverseLinearizeAddend() {
        return -(minFarPlane+maxNearPlane)/(2*minFarPlane*maxNearPlane);
    }

    @Override
    public double hyperbolizeFactor() {
        return (2*minFarPlane*maxNearPlane)/(minFarPlane-maxNearPlane);
    }

    @Override
    public double inverseHyperbolizeFactor() {
        return (2*farPlane*nearPlane)/(farPlane-nearPlane);
    }

    @Override
    public double hyperbolizeAddend() {
        return (minFarPlane+maxNearPlane)/(minFarPlane-maxNearPlane);
    }

    @Override
    public double inverseHyperbolizeAddend() {
        return (farPlane+nearPlane)/(farPlane-nearPlane);
    }

    /**
     * Calculates the farthest near plane and the nearest far plane
     */
    @Override
    public void update(Matrix4f projection, float cameraY, float pitchDeg, float cloudsHeight, Config generatorConfig) {
        float tanFov = 1 / projection.m11();
        // this is actually the total fov / 2
        float fov = (float) Math.atan(tanFov);
        double m11 = projection.m32();
        double m10 = projection.m22();
        farPlane = m11 / (m10 + 1);
        nearPlane = m11 / (m10 - 1);

        Config config = Main.getConfig();

        double cloudPlaneMinY = (cloudsHeight - cameraY) - config.sizeY;
        double cloudPlaneMaxY = (cloudsHeight - cameraY) + config.sizeY + generatorConfig.yRange;
        double cloudPlaneNear, cloudPlaneFar;
        if(Math.abs(cloudPlaneMinY) < Math.abs(cloudPlaneMaxY)) {
            cloudPlaneNear = cloudPlaneMinY;
            cloudPlaneFar = cloudPlaneMaxY;
        } else {
            cloudPlaneNear = cloudPlaneMaxY;
            cloudPlaneFar = cloudPlaneMinY;
        }
        double pitchRad = -Math.toRadians(pitchDeg);
        float nearSign = Math.signum((float) cloudPlaneNear);

        if(cloudPlaneFar * cloudPlaneNear <= 0.0f) {
            // Inside the two cloud planes
            maxNearPlane = 0.5f;
        } else {
            // edgeDy = sin(nearSign * fov + pitchRad) - the y change per 1 unit along the lower / top frustum edge
            // cloudPlaneNear / edgeDy                 - how far along the edge the intersection with the cloud plane happens
            // 1 / sqrt(1^2 + tanFov^2)                - converts edge distance to center distance

            // maxNearPlane = (cloudPlaneNear / Math.sin(nearSign * fov + pitchRad)) / Math.sqrt(1.0 + tanFov*tanFov);
            // simplifies to:
            float signedFov = nearSign * fov;
            maxNearPlane = cloudPlaneNear * Math.cos(signedFov) / Math.sin(signedFov + pitchRad);
        }

        double maxDistanceXZ = generatorConfig.blockDistance() + config.sizeXZ;
        // TODO: Find a better formula, this is not the closest near plane
        minFarPlane = (float) Math.sqrt(maxDistanceXZ * maxDistanceXZ + cloudPlaneFar * cloudPlaneFar);

        invalid = maxNearPlane >= minFarPlane || maxNearPlane < 0;

        maxNearPlane = Math.max(maxNearPlane, 0.5f);

        this.projection.set(projection);
        this.projection.m22((float) (-(minFarPlane+maxNearPlane)/(minFarPlane-maxNearPlane)));
        this.projection.m32((float) (-(2*minFarPlane*maxNearPlane)/(minFarPlane-maxNearPlane)));
    }

    @Override
    public boolean isInvalid() {
        return invalid;
    }

    @Override
    public Matrix4f getProjection() {
        return projection;
    }
}
