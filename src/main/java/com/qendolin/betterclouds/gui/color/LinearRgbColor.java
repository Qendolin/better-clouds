package com.qendolin.betterclouds.gui.color;

import org.joml.*;

import java.lang.Math;
import java.util.Objects;

// Linear srgb color
public class LinearRgbColor implements IColor<LinearRgbColor, XYZColor> {
    private static final Meta[] meta = new Meta[]{
        new Meta( "rgb.red", 0, 1, false, 1,  ChannelFormat.DECIMAL),
        new Meta( "rgb.green", 0, 1, false, 1,  ChannelFormat.DECIMAL),
        new Meta("rgb.blue", 0, 1, false, 1,  ChannelFormat.DECIMAL)};

    public static final Matrix3d rgbToCieXyz = new Matrix3d(
        0.41239079926595934, 0.21263900587151027, 0.01933081871559182,
        0.357584339383878, 0.715168678767756, 0.11919477979462598,
        0.1804807884018343, 0.07219231536073371, 0.9505321522496607);

    public static final Matrix3d cieXyzToRgb = new Matrix3d(
        3.2409699419045226, -0.9692436362808796, 0.05563007969699366,
        -1.537383177570094, 1.8759675015077202, -0.20397695888897652,
        -0.4986107602930034, 0.04155505740717559, 1.0569715142428786);


    public float red;
    public float green;
    public float blue;
    public float alpha;

    public LinearRgbColor(float red, float green, float blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = 1;
    }

    public LinearRgbColor(float red, float green, float blue, float alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    @Override
    public XYZColor toXYZ() {
        Vector3d vec = new Vector3d(red, green, blue);
        vec.mul(rgbToCieXyz);
        return new XYZColor(vec, alpha);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S extends IColor<S, ?>> S to(Class<S> space) {
        if(space == GammaRgbColor.class) return (S) toGamma();
        return IColor.super.to(space);
    }

    public GammaRgbColor toGamma() {
        float gr = toGamma(red);
        float gg = toGamma(green);
        float gb = toGamma(blue);
        return new GammaRgbColor(gr, gg, gb, alpha);
    }

    private float toGamma(float c) {
        if (c <= 0.0031308) return c*12.92f;
        return 1.055f * (float) Math.pow(c, 1/2.4) - 0.055f;
    }

    @Override
    public LinearRgbColor lerp(IColor<?, ?> color, float factor, ArcMode mode) {
        LinearRgbColor other = color.to(LinearRgbColor.class);
        Vector4f res = Interpolate.linear(new Vector4f(red, green, blue, alpha), new Vector4f(other.red, other.green, other.blue, other.alpha), factor);
        return new LinearRgbColor(res.x, res.y, res.z, res.w);
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
            case 0 -> red = value;
            case 1 -> green = value;
            case 2 -> blue = value;
        }
    }

    @Override
    public float getCoord(int i) {
        return switch (i) {
            case 0 -> red;
            case 1 -> green;
            case 2 -> blue;
            default -> 0;
        };
    }

    @Override
    public LinearRgbColor copy() {
        return new LinearRgbColor(red, green, blue, alpha);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinearRgbColor that = (LinearRgbColor) o;
        return Float.compare(red, that.red) == 0 && Float.compare(green, that.green) == 0 && Float.compare(blue, that.blue) == 0 && Float.compare(alpha, that.alpha) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(red, green, blue, alpha);
    }
}
