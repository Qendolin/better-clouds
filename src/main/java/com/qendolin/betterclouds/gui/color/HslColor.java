package com.qendolin.betterclouds.gui.color;

import org.joml.Vector4f;

import java.util.Objects;

public class HslColor implements IColor<HslColor, GammaRgbColor> {

    private static final Meta[] meta = new Meta[]{
        new Meta("hsl.hue", 0, 360, true, 1, ChannelFormat.DEGREES),
        new Meta( "hsl.saturation", 0, 1, false, 100, ChannelFormat.PERCENT),
        new Meta( "hsl.lightness", 0, 1, false, 100, ChannelFormat.PERCENT)};

    public float hue;
    public float saturation;
    public float lightness;
    public float alpha;

    public HslColor(float hue, float saturation, float lightness) {
        this.hue = hue;
        this.saturation = saturation;
        this.lightness = lightness;
        this.alpha = 1;
    }

    public HslColor(float hue, float saturation, float lightness, float alpha) {
        this.hue = hue;
        this.saturation = saturation;
        this.lightness = lightness;
        this.alpha = alpha;
    }

    @Override
    public XYZColor toXYZ() {
        return toRgb().toXYZ();
    }

    public GammaRgbColor toRgb() {
        // based on https://stackoverflow.com/a/9493060/7448536
        float r, g, b;

        float h = hue, s = saturation, l = lightness;

        h = h % 360;
        if (h < 0) {
            h += 360;
        }
        h /= 360f;

        if (s == 0) {
            r = g = b = l; // achromatic
        } else {
        float q = l < 0.5 ? l * (1 + s) : l + s - l * s;
            float p = 2 * l - q;
            r = hueToRgb(p, q, h + 1/3f);
            g = hueToRgb(p, q, h);
            b = hueToRgb(p, q, h - 1/3f);
        }

        return new GammaRgbColor(r, g, b, alpha);
    }

    private float hueToRgb(float p, float q, float t) {
        if (t < 0) t += 1;
        if (t > 1) t -= 1;
        if (t < 1/6f) return p + (q - p) * 6 * t;
        if (t < 1/2f) return q;
        if (t < 2/3f) return p + (q - p) * (2/3f - t) * 6;
        return p;
    }


    @Override
    public HslColor lerp(IColor<?, ?> color, float factor, ArcMode mode) {
        HslColor other = color.to(HslColor.class);
        Vector4f res = Interpolate.linear(new Vector4f(0, saturation, lightness, alpha), new Vector4f(0, other.saturation, other.lightness, other.alpha), factor);
        float hue = Interpolate.arc(this.hue, other.hue, factor, mode);
        return new HslColor(hue, res.y, res.z, res.w);
    }

    @Override
    public Meta[] coordsMeta() {
        return meta;
    }

    @Override
    public Class<GammaRgbColor> baseSpace() {
        return null;
    }

    @Override
    public void setCoord(int i, float value) {
        switch (i) {
            case 0 -> hue = value;
            case 1 -> saturation = value;
            case 2 -> lightness = value;
        }
    }

    @Override
    public float getCoord(int i) {
        return switch (i) {
            case 0 -> hue;
            case 1 -> saturation;
            case 2 -> lightness;
            default -> 0;
        };
    }


    @SuppressWarnings("unchecked")
    @Override
    public <S extends IColor<S, ?>> S to(Class<S> space) {
        if(space == GammaRgbColor.class) return (S) toRgb();
        return IColor.super.to(space);
    }

    @Override
    public HslColor copy() {
        return new HslColor(hue, saturation, lightness, alpha);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HslColor hslColor = (HslColor) o;
        return Float.compare(hue, hslColor.hue) == 0 && Float.compare(saturation, hslColor.saturation) == 0 && Float.compare(lightness, hslColor.lightness) == 0 && Float.compare(alpha, hslColor.alpha) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hue, saturation, lightness, alpha);
    }
}
