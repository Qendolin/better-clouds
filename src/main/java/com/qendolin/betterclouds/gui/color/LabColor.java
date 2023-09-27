package com.qendolin.betterclouds.gui.color;

import org.joml.*;

import java.lang.Math;
import java.util.Objects;

// Based on OKLAB https://bottosson.github.io/posts/oklab/
public class LabColor implements IColor<LabColor, XYZColor> {

    public static final Matrix3d cieXyzToLms = new Matrix3d(
        +0.8189330101, +0.0329845436, +0.0482003018,
        +0.3618667424, +0.9293118715, +0.2643662691,
        -0.1288597137, +0.0361456387, +0.6338517070);

    public static final Matrix3d lmsToCieXyz = cieXyzToLms.invert(new Matrix3d());

    public static final Matrix3d lmsToLab = new Matrix3d(
        +0.2104542553, +1.9779984951, +0.0259040371,
        +0.7936177850, -2.4285922050, +0.7827717662,
        -0.0040720468, +0.4505937099, -0.8086757660);

    public static final Matrix3d labToLms = lmsToLab.invert(new Matrix3d());

    private static final Meta[] meta = new Meta[]{
        new Meta( "lab.lightness", 0, 1, false, 100,  ChannelFormat.PERCENT),
        new Meta( "lab.a", -0.4f, 0.4f, false, 1, ChannelFormat.DECIMAL),
        new Meta("lab.b", -0.4f, 0.4f, false, 1, ChannelFormat.DECIMAL)};

    public float lightness;
    public float a;
    public float b;
    public float alpha;

    public LabColor(float lightness, float a, float b) {
        this.lightness = lightness;
        this.a = a;
        this.b = b;
        this.alpha = 1;
    }

    public LabColor(float lightness, float a, float b, float alpha) {
        this.lightness = lightness;
        this.a = a;
        this.b = b;
        this.alpha = alpha;
    }

    @Override
    public XYZColor toXYZ() {
        Vector3d vec = new Vector3d(lightness, a, b);
        vec.mul(labToLms);
        vec.x = (float) Math.pow(vec.x, 3);
        vec.y = (float) Math.pow(vec.y, 3);
        vec.z = (float) Math.pow(vec.z, 3);
        vec.mul(lmsToCieXyz);
        return new XYZColor(vec, alpha);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S extends IColor<S, ?>> S to(Class<S> space) {
        if(space == LchColor.class) return (S) toLch();
        return IColor.super.to(space);
    }

    public LchColor toLch() {
        float hue;
		float epsilon = 0.0002f; // chromatic components much smaller than a,b

        if (Math.abs(a) < epsilon && Math.abs(b) < epsilon) {
            hue = 0;
        } else {
            hue = (float) (Math.atan2(b, a) * 180 / Math.PI);
        }

        float chroma = (float) Math.sqrt(a*a + b*b);
        return new LchColor(lightness, chroma, constrain(hue), alpha);
    }

    private float constrain(float angle) {
        return ((angle % 360) + 360) % 360;
    }

    @Override
    public LabColor lerp(IColor<?, ?> color, float factor, ArcMode mode) {
        LabColor other = color.to(LabColor.class);
        Vector4f res = Interpolate.linear(new Vector4f(lightness, a, b, alpha), new Vector4f(other.lightness, other.a, other.b, other.alpha), factor);
        return new LabColor(res.x, res.y, res.z, res.w);
    }

    @Override
    public Meta[] coordsMeta() {
        return meta;
    }

    @Override
    public Class<XYZColor> baseSpace() {
        return XYZColor.class;
    }

    @Override
    public void setCoord(int i, float value) {
        switch (i) {
            case 0 -> lightness = value;
            case 1 -> a = value;
            case 2 -> b = value;
        }
    }

    @Override
    public float getCoord(int i) {
        return switch (i) {
            case 0 -> lightness;
            case 1 -> a;
            case 2 -> b;
            default -> 0;
        };
    }

    @Override
    public LabColor copy() {
        return new LabColor(lightness, a, b, alpha);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LabColor labColor = (LabColor) o;
        return Float.compare(lightness, labColor.lightness) == 0 && Float.compare(a, labColor.a) == 0 && Float.compare(b, labColor.b) == 0 && Float.compare(alpha, labColor.alpha) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lightness, a, b, alpha);
    }
}
