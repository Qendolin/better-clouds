package com.qendolin.betterclouds.clouds;

import com.qendolin.betterclouds.Config;
import com.qendolin.betterclouds.Main;
import net.minecraft.client.MinecraftClient;
import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.joml.Vector4d;
import org.joml.Vector4f;


public class QualityViewboxTransform implements IViewboxTransform {

    private static final Matrix4d tempMatrix = new Matrix4d();

    private final Matrix4f cachedMatrix = new Matrix4f();
    private final Vector4d nearVec = new Vector4d();
    private final Vector4d farVec = new Vector4d();
    private final Vector4d fovVec = new Vector4d();
    private double farPlane;
    private double nearPlane;
    private double minFarPlane;
    private double maxNearPlane;
    private final Matrix4f projection = new Matrix4f();
    private boolean invalid = false;

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

    @Override
    public boolean isInvalid() {
        return invalid;
    }

    @Override
    public Matrix4f getProjection() {
        return projection;
    }

    /**
     * Calculates the farthest near plane and the nearest far plane
     */
    @Override
    public void update(Matrix4f projection, float cameraY, float pitchDeg, float cloudsHeight, Config generatorConfig) {
        if(fastEquals(cachedMatrix, projection)) return;
        cachedMatrix.set(projection);

        tempMatrix.set(projection);
        tempMatrix.invert();
        nearVec.set(0, 0, -1, 1)
            .mul(tempMatrix).div(nearVec.w)
            .sub(0, 0, 0, 1);
        farVec.set(0, 0, 1, 1)
            .mul(tempMatrix).div(farVec.w)
            .sub(0, 0, 0, 1);
        fovVec.set(0, 1, 1, 1)
            .mul(tempMatrix).div(fovVec.w)
            .sub(0, 0, 0, 1);

        nearPlane = nearVec.length();
        farPlane = farVec.length();
        double fov = farVec.angle(fovVec);

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

        if(cloudPlaneFar * cloudPlaneNear <= 0.0) {
            // Inside the two cloud planes
            maxNearPlane = 0.5;
        } else {
            // edgeDy = sin(nearSign * fov + pitchRad) - the y change per 1 unit along the lower / top frustum edge
            // cloudPlaneNear / edgeDy                 - how far along the edge the intersection with the cloud plane happens
            // 1 / sqrt(1^2 + tanFov^2)                - converts edge distance to center distance

            // maxNearPlane = (cloudPlaneNear / Math.sin(nearSign * fov + pitchRad)) / Math.sqrt(1.0 + tanFov*tanFov);
            // simplifies to:
            double signedFov = nearSign * fov;
            maxNearPlane = cloudPlaneNear * Math.cos(signedFov) / Math.sin(signedFov + pitchRad);
        }

        double maxDistanceXZ = generatorConfig.blockDistance() + config.sizeXZ;
        // This isn't always the nearest far-plane, but it's good enough
        minFarPlane = (float) Math.sqrt(maxDistanceXZ * maxDistanceXZ + cloudPlaneFar * cloudPlaneFar);

        invalid = maxNearPlane >= minFarPlane || maxNearPlane < 0;

        maxNearPlane = Math.max(maxNearPlane, 0.5);

        this.projection.set(projection);
        this.projection.m22((float) (-(minFarPlane+maxNearPlane)/(minFarPlane-maxNearPlane)));
        this.projection.m32((float) (-(2*minFarPlane*maxNearPlane)/(minFarPlane-maxNearPlane)));
    }

    private boolean fastEquals(Matrix4f a, Matrix4f b) {
        return a.m00() == b.m00() && a.m11() == b.m11() && a.m22() == b.m22() && a.m33() == b.m33()
            && a.m01() == b.m01() && a.m02() == b.m02() && a.m03() == b.m03()
            && a.m10() == b.m10() && a.m12() == b.m12() && a.m13() == b.m13()
            && a.m20() == b.m20() && a.m21() == b.m21() && a.m23() == b.m23()
            && a.m30() == b.m30() && a.m31() == b.m31() && a.m32() == b.m32();
    }
}
