package com.qendolin.betterclouds.gui.color;

import org.joml.Vector4f;

import java.util.Objects;

// Based on OKLCH https://github.com/evilmartians/oklch-picker
public class LchColor implements IColor<LchColor, LabColor> {

    private static final Meta[] meta = new Meta[]{
        new Meta( "lch.lightness", 0, 1, false, 100,  ChannelFormat.PERCENT),
        new Meta( "lch.chroma", 0, 0.4f, false, 1,  ChannelFormat.DECIMAL),
        new Meta("lch.hue", 0, 360, true, 1, ChannelFormat.DEGREES)};
    public float lightness;
    public float chroma;
    public float hue;
    public float alpha;

    public LchColor(float lightness, float chroma, float hue) {
        this.lightness = lightness;
        this.chroma = chroma;
        this.hue = hue;
        this.alpha = 1;
    }

    public LchColor(float lightness, float chroma, float hue, float alpha) {
        this.lightness = lightness;
        this.chroma = chroma;
        this.hue = hue;
        this.alpha = alpha;
    }

    @Override
    public XYZColor toXYZ() {
        return toLab().toXYZ();
    }

    public LabColor toLab() {
        float a = 0, b = 0;

        if (!Float.isNaN(hue)) {
            a = (float) (chroma * Math.cos(hue * Math.PI / 180));
            b = (float) (chroma * Math.sin(hue * Math.PI / 180));
        }

        return new LabColor(lightness, a, b, alpha);
    }

    @Override
    public LchColor lerp(IColor<?, ?> color, float factor, ArcMode mode) {
        LchColor other = color.to(LchColor.class);
        Vector4f res = Interpolate.linear(new Vector4f(lightness, chroma, 0, alpha), new Vector4f(other.lightness, other.chroma, 0, other.alpha), factor);
        float hue = Interpolate.arc(this.hue, other.hue, factor, mode);
        return new LchColor(res.x, res.y, hue, res.w);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S extends IColor<S, ?>> S to(Class<S> space) {
        if(space == LabColor.class) return (S) toLab();
        return IColor.super.to(space);
    }

    @Override
    public boolean isPolar() {
        return true;
    }

    @Override
    public Class<LabColor> baseSpace() {
        return LabColor.class;
    }

    @Override
    public Meta[] coordsMeta() {
        return meta;
    }

    @Override
    public void setCoord(int i, float value) {
        switch (i) {
            case 0 -> lightness = value;
            case 1 -> chroma = value;
            case 2 -> hue = value;
        }
    }

    @Override
    public float getCoord(int i) {
        return switch (i) {
            case 0 -> lightness;
            case 1 -> chroma;
            case 2 -> hue;
            default -> 0;
        };
    }

    @Override
    public LchColor copy() {
        return new LchColor(lightness, chroma, hue, alpha);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LchColor lchColor = (LchColor) o;
        return Float.compare(lightness, lchColor.lightness) == 0 && Float.compare(chroma, lchColor.chroma) == 0 && Float.compare(hue, lchColor.hue) == 0 && Float.compare(alpha, lchColor.alpha) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lightness, chroma, hue, alpha);
    }
}
