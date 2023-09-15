package com.qendolin.betterclouds.gui.color;

import org.joml.Vector3f;
import org.joml.Vector4f;

public class Interpolate {
    public static Vector4f linear(Vector4f start, Vector4f end, float f) {
        return new Vector4f(
            linear(start.x, end.x, f),
            linear(start.y, end.y, f),
            linear(start.z, end.z, f),
            linear(start.w, end.w, f)
        );
    }

    public static float linear(float start, float end, float f) {
        if (Float.isNaN(start)) {
            return end;
        }

        if (Float.isNaN(end)) {
            return start;
        }

        return start + (end - start) * f;
    }

    public static float arc(float start, float end, float f, IColor.ArcMode mode) {
        start = constrain(start);
        if(end != 360.0f) end = constrain(end);
        float diff = end - start;

        // Based on https://github.com/LeaVerou/color.js/blob/d6e70db82bb59940149b33b45c408cab952b67e3/src/angles.js#L5
        switch (mode) {
            case SHORT -> {
                if (diff > 180) {
                    start += 360;
                } else if (diff < -180) {
                    end += 360;
                }
            }
            case LONG -> {
                if (-180 < diff && diff < 180) {
                    if (diff > 0) {
                        start += 360;
                    } else {
                        end += 360;
                    }
                }
            }
            case DECREASE -> {
                if (diff > 0) {
                    end += 360;
                }
            }
            case INCREASE -> {
                if (diff < 0) {
                    end += 360;
                }
            }
        }

        return linear(start, end, f);
    }

    private static float constrain(float angle) {
        return ((angle % 360) + 360) % 360;
    }
}
