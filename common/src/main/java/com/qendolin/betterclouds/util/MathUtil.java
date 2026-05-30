package com.qendolin.betterclouds.util;

import net.minecraft.util.Mth;
import org.joml.*;

public interface MathUtil {

    static float interpolateDayNightFactor(float time, float riseStart, float riseEnd, float setStart, float setEnd) {
        if (time <= 6000 || time > 18000) {
            // sunrise time
            if (time > 18000) time -= 24000;
            return smoothstep(time, riseStart, riseEnd);
        } else {
            // sunset time
            return 1 - smoothstep(time, setStart, setEnd);
        }
    }

    static float mapTimeOfDay(float time, float riseStart, float riseEnd, float setStart, float setEnd) {
        if (time <= 6000 || time > 18000) {
            // sunrise time
            if (time > 18000) time -= 24000;
            if (time < riseStart) {
                time = map(time, -6000, riseStart, -6000, -785);
            } else if (time > riseEnd) {
                time = map(time, riseEnd, 6000, 1163, 6000);
            } else {
                time = map(time, riseStart, riseEnd, -785, 1163);
            }
        } else {
            // sunset time
            if (time < setStart) {
                time = map(time, 6000, setStart, 6000, 10837);
            } else if (time > setEnd) {
                time = map(time, setEnd, 18000, 12785, 18000);
            } else {
                time = map(time, setStart, setEnd, 10837, 12785);
            }
        }
        return time;
    }

    static float map(float x, float fromMin, float fromMax, float toMin, float toMax) {
        float f = (x - fromMin) / (fromMax - fromMin);
        return f * (toMax - toMin) + toMin;
    }

    static float smoothstep(float x, float e0, float e1) {
        x = Mth.clamp((x - e0) / (e1 - e0), 0, 1);
        return x * x * (3 - 2 * x);
    }


    static Vector2d calculateClippingPlanes(Matrix4d projInverseMatrix) {
        // "Fast" computation of the near and far plane doesn't work because of nausea and view bobbing.
        // This is slightly slower but should always give the correct results.
        Vector4d farPlane = new Vector4d(0, 0, 1, 1);
        Vector4d nearPlane = new Vector4d(0, 0, -1, 1);
        projInverseMatrix.transform(farPlane);
        projInverseMatrix.transform(nearPlane);
        return new Vector2d((float) (-nearPlane.z / nearPlane.w), (float) (-farPlane.z / farPlane.w));
    }
}
