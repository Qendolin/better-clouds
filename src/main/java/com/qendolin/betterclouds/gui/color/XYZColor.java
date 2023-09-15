package com.qendolin.betterclouds.gui.color;

import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Objects;

public class XYZColor implements IColor<XYZColor, XYZColor> {
    private static final Meta[] meta = new Meta[]{
        new Meta( "xyz.x", Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, false, 1,  ChannelFormat.DECIMAL),
        new Meta( "xyz.y", Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, false, 1,  ChannelFormat.DECIMAL),
        new Meta("xyz.z", Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, false, 1,  ChannelFormat.DECIMAL)};

    public double x;
    public double y;
    public double z;
    public float alpha;

    public XYZColor(float x, float y, float z, float alpha) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.alpha = alpha;
    }

    public XYZColor(double x, double y, double z, float alpha) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.alpha = alpha;
    }

    public XYZColor(Vector3f xyz, float alpha) {
        this.x = xyz.x;
        this.y = xyz.y;
        this.z = xyz.z;
        this.alpha = alpha;
    }

    public XYZColor(Vector3d xyz, float alpha) {
        this.x = xyz.x;
        this.y = xyz.y;
        this.z = xyz.z;
        this.alpha = alpha;
    }

    public LinearRgbColor toLinearRgb() {
        Vector3d vec = new Vector3d(x, y, z);
        vec.mul(LinearRgbColor.cieXyzToRgb);
        return new LinearRgbColor((float) vec.x, (float) vec.y, (float) vec.z, alpha);
    }

    public LabColor toLab() {
        Vector3d vec = new Vector3d(x, y, z);
        vec.mul(LabColor.cieXyzToLms);
        vec.x = (float) Math.pow(vec.x, 1d/3d);
        vec.y = (float) Math.pow(vec.y, 1d/3d);
        vec.z = (float) Math.pow(vec.z, 1d/3d);
        vec.mul(LabColor.lmsToLab);
        return new LabColor((float) vec.x, (float) vec.y, (float) vec.z, alpha);
    }

    @Override
    public XYZColor toXYZ() {
        return this;
    }

    @Override
    public XYZColor lerp(IColor<?, ?> color, float factor, ArcMode mode) {
        XYZColor other = color.to(XYZColor.class);
        Vector4f res = Interpolate.linear(new Vector4f((float) x, (float) y, (float) z, alpha), new Vector4f((float) other.x, (float) other.y, (float) other.z, other.alpha), factor);
        return new XYZColor(res.x, res.y, res.z, res.w);
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
            case 0 -> x = value;
            case 1 -> y = value;
            case 2 -> z = value;
        }
    }

    @Override
    public float getCoord(int i) {
        return switch (i) {
            case 0 -> (float) x;
            case 1 -> (float) y;
            case 2 -> (float) z;
            default -> 0;
        };
    }

    @Override
    public XYZColor copy() {
        return new XYZColor(x, y, z, alpha);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        XYZColor xyzColor = (XYZColor) o;
        return Double.compare(x, xyzColor.x) == 0 && Double.compare(y, xyzColor.y) == 0 && Double.compare(z, xyzColor.z) == 0 && Float.compare(alpha, xyzColor.alpha) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, alpha);
    }
}
