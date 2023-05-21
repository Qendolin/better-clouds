package com.qendolin.betterclouds.clouds;

import com.qendolin.betterclouds.Main;
import org.joml.Matrix4f;

import java.nio.FloatBuffer;

public class ViewboxTransform {
//    private final FloatBuffer matrixBuffer = FloatBuffer.wrap(new float[16]);

    private double farPlane;
    private double nearPlane;
    private double minFarPlane;
    private double maxNearPlane;
    private final Matrix4f projection = new Matrix4f();
    private boolean invalid = false;

    public double farPlane() {
        return farPlane;
    }

    public double nearPlane() {
        return nearPlane;
    }

    public double minFarPlane() {
        return minFarPlane;
    }

    public double maxNearPlane() {
        return maxNearPlane;
    }

    public double linearizeFactor() {
        return ((farPlane-nearPlane)/(2*farPlane*nearPlane));
    }

    public double linearizeAddend() {
        return -(farPlane+nearPlane)/(2*farPlane*nearPlane);
    }

    public double hyperbolizeFactor() {
        return (2*minFarPlane*maxNearPlane)/(minFarPlane-maxNearPlane);
    }

    public double hyperbolizeAddend() {
        return (minFarPlane+maxNearPlane)/(minFarPlane-maxNearPlane);
    }

//    /**
//     * Calculates the farthest near plane and the nearest far plane
//     * @param projection
//     * @param cameraY
//     * @param cloudsHeight
//     * @param pitchDeg in radiants
//     */
//    public void update(Matrix4f projection, float cameraY, float cloudsHeight, float pitchDeg) {
//        projection.write(matrixBuffer, true);
//
//        float tanFov = 1 / matrixBuffer.get(5);
//        float fov = (float) Math.atan(tanFov);
//        double m11 = matrixBuffer.get(11);
//        double m10 = matrixBuffer.get(10);
//        farPlane = m11 / (m10 + 1);
//        nearPlane = m11 / (m10 - 1);
//
//        // FIXME: Some values of Main.CONFIG.spreadY cause a too far near plane
//        // FIXME: near plane is too far for low fov values
//
//        double cloudPlaneMinY = (cloudsHeight - cameraY) - Main.CONFIG.sizeY;
//        double cloudPlaneMaxY = (cloudsHeight - cameraY) + Main.CONFIG.sizeY + Main.CONFIG.spreadY;
//        double cloudPlaneNear, cloudPlaneFar;
//        if(Math.abs(cloudPlaneMinY) < Math.abs(cloudPlaneMaxY)) {
//            cloudPlaneNear = cloudPlaneMinY;
//            cloudPlaneFar = cloudPlaneMaxY;
//        } else {
//            cloudPlaneNear = cloudPlaneMaxY;
//            cloudPlaneFar = cloudPlaneMinY;
//        }
//        double pitchRad = -Math.toRadians(pitchDeg);
//        float nearSign = Math.signum((float) cloudPlaneNear);
//
//        // frustum_size(near_distance) = near_distance * tan(fov)
//        // height(frustum_size) = frustum_size * cos(pitch)
//        // view_height(near_distance) = near_distance * sin(pitch)
//        // cloud_distance(near_distance) = height(frustum_size(near_distance)) + view_height(near_distance)
//        //
//        // cloud_distance = near_distance * sin(pitch) + near_distance * tan(fov) * cos(pitch)
//        // near_distance = cloud_distance / (sin(pitch) + tan(fov) * cos(pitch))
//
//        if(nearSign == -1 && cloudPlaneNear == cloudPlaneMinY) {
//            maxNearPlane = 0.5f;
//        } else {
//            maxNearPlane = (float) ((cloudPlaneNear) / (Math.sin(pitchRad) + nearSign * Math.cos(pitchRad) * tanFov));
////            maxNearPlane = (float) (Math.cos(fov) * (cloudPlaneNear) / Math.sin(fov + Math.abs(pitchRad)));
//        }
//
//        // This also works
//        // maxNearPlane = (float) (Math.cos(fov) * (cloudPlaneNear-cameraY) / Math.sin(fov + Math.abs(pitchRad)));
//
//        double maxDistanceXZ = Main.CONFIG.blockDistance() + Main.CONFIG.sizeXZ;
//        // FIXME: this is suboptimal
//        minFarPlane = (float) Math.sqrt(maxDistanceXZ * maxDistanceXZ + cloudPlaneFar * cloudPlaneFar);
//
//        if(maxNearPlane >= minFarPlane || maxNearPlane < 0) {
//            // TODO: skip render
//        }
//
//        maxNearPlane = Math.max(maxNearPlane, 0.5f);
//
//        matrixBuffer.put(10, (float) (-(minFarPlane+maxNearPlane)/(minFarPlane-maxNearPlane)));
//        matrixBuffer.put(11, (float) (-(2*minFarPlane*maxNearPlane)/(minFarPlane-maxNearPlane)));
//
//        this.projection.read(matrixBuffer, true);
//    }

    /**
     * Calculates the farthest near plane and the nearest far plane
     * @param projection
     * @param cameraY
     * @param cloudsHeight
     * @param pitchDeg in radiants
     */
    public void update(Matrix4f projection, float cameraY, float cloudsHeight, float pitchDeg) {
        // 00 01 02 03
        // 04 05 06 07
        // 08 09 10 11
        // 12 13 14 15

        float tanFov = 1 / projection.m11();
        // this is the total fov/2
        float fov = (float) Math.atan(tanFov);
        double m11 = projection.m32();
        double m10 = projection.m22();
        farPlane = m11 / (m10 + 1);
        nearPlane = m11 / (m10 - 1);

        // FIXME: Some values of Main.CONFIG.spreadY cause a too far near plane
        // FIXME: near plane is too far for low fov values
        // FIXME: far plane is too near for very low view and cloud distances

        double cloudPlaneMinY = (cloudsHeight - cameraY) - Main.CONFIG.sizeY;
        double cloudPlaneMaxY = (cloudsHeight - cameraY) + Main.CONFIG.sizeY + Main.CONFIG.spreadY;
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

        // frustum_size(near_distance) = near_distance * tan(fov)
        // height(frustum_size) = frustum_size * cos(pitch)
        // view_height(near_distance) = near_distance * sin(pitch)
        // cloud_distance(near_distance) = height(frustum_size(near_distance)) + view_height(near_distance)
        //
        // cloud_distance = near_distance * sin(pitch) + near_distance * tan(fov) * cos(pitch)
        // near_distance = cloud_distance / (sin(pitch) + tan(fov) * cos(pitch))

        if(cloudPlaneFar * cloudPlaneNear <= 0.0f) {
            maxNearPlane = 0.5f;
        } else {
//            maxNearPlane = (float) ((cloudPlaneNear) / (Math.sin(pitchRad) + nearSign * Math.cos(pitchRad) * tanFov));
//            maxNearPlane = (float) (Math.cos(fov) * (cloudPlaneNear) / Math.sin(fov + Math.abs(pitchRad)));

            // maxNearPlane = (cloudPlaneNear / Math.sin(nearSign * fov + pitchRad)) / Math.sqrt(1.0 + tanFov*tanFov);
            // simplifies to:
            float signedFov = nearSign * fov;
            maxNearPlane = cloudPlaneNear * Math.cos(signedFov) / Math.sin(signedFov + pitchRad);
        }

        // This also works
        // maxNearPlane = (float) (Math.cos(fov) * (cloudPlaneNear-cameraY) / Math.sin(fov + Math.abs(pitchRad)));

        double maxDistanceXZ = Main.CONFIG.blockDistance() + Main.CONFIG.sizeXZ;
        // FIXME: this is not the closest near plane
        minFarPlane = (float) Math.sqrt(maxDistanceXZ * maxDistanceXZ + cloudPlaneFar * cloudPlaneFar);

        invalid = maxNearPlane >= minFarPlane || maxNearPlane < 0;

        maxNearPlane = Math.max(maxNearPlane, 0.5f);

        this.projection.set(projection);
        this.projection.m22((float) (-(minFarPlane+maxNearPlane)/(minFarPlane-maxNearPlane)));
        this.projection.m32((float) (-(2*minFarPlane*maxNearPlane)/(minFarPlane-maxNearPlane)));
    }

    public boolean isInvalid() {
        return invalid;
    }

    public Matrix4f getProjection() {
        return projection;
    }
}
