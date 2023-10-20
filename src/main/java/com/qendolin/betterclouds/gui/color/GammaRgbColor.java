package com.qendolin.betterclouds.gui.color;

import net.minecraft.util.math.MathHelper;
import org.joml.Vector3i;
import org.joml.Vector4f;

import java.util.Objects;

// sRGB color
public final class GammaRgbColor implements IColor<GammaRgbColor, LinearRgbColor> {

    private static final Meta[] meta = new Meta[]{
        new Meta( "rgb.red", 0, 1, false, 255,  ChannelFormat.INTEGER),
        new Meta( "rgb.green", 0, 1, false, 255,  ChannelFormat.INTEGER),
        new Meta("rgb.blue", 0, 1, false, 255,  ChannelFormat.INTEGER)};

    public float red;
    public float green;
    public float blue;
    public float alpha;

    public GammaRgbColor(float red, float green, float blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = 1;
    }

    public GammaRgbColor(float red, float green, float blue, float alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public static GammaRgbColor hex(int red, int green, int blue) {
        return new GammaRgbColor(red / 255f, green / 255f, blue / 255f, 1.0f);
    }

    public static GammaRgbColor hex(int rgb) {
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = (rgb) & 0xff;
        return hex(r, g, b);
    }

    public static GammaRgbColor hex(int red, int green, int blue, int alpha) {
        return new GammaRgbColor(red / 255f, green / 255f, blue / 255f, alpha / 255f);
    }

    public int pack() {
        int r = Math.round(red * 0xff);
        int g = Math.round(green * 0xff);
        int b = Math.round(blue * 0xff);
        int a = Math.round(alpha * 0xff);
        return a << 24 | r << 16 | g << 8 | b;
    }

    public int packABGR() {
        int r = Math.round(red * 0xff);
        int g = Math.round(green * 0xff);
        int b = Math.round(blue * 0xff);
        int a = Math.round(alpha * 0xff);
        return a << 24 | b << 16 | g << 8 | r;
    }


    public LinearRgbColor toLinear() {
        float lr = toLinear(red);
        float lg = toLinear(green);
        float lb = toLinear(blue);
        return new LinearRgbColor(lr, lg, lb, alpha);
    }

    @Override
    public XYZColor toXYZ() {
        return toLinear().toXYZ();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S extends IColor<S, ?>> S to(Class<S> space) {
        if(space == LinearRgbColor.class) return (S) toLinear();
        if(space == HslColor.class) return (S) toHsl();
        return IColor.super.to(space);
    }

    private float toLinear(float c) {
        if (c <= 0.04045) return c/12.92f;
        return (float) Math.pow((c + 0.055) / 1.055, 2.4);
    }

    @Override
    public GammaRgbColor lerp(IColor<?, ?> color, float factor, ArcMode mode) {
        GammaRgbColor other = color.to(GammaRgbColor.class);
        Vector4f res = Interpolate.linear(new Vector4f(red, green, blue, alpha), new Vector4f(other.red, other.green, other.blue, other.alpha), factor);
        return new GammaRgbColor(res.x, res.y, res.z, res.w);
    }

    @Override
    public Meta[] coordsMeta() {
        return meta;
    }

    @Override
    public Class<LinearRgbColor> baseSpace() {
        return LinearRgbColor.class;
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

    public String toHex() {
        int r = MathHelper.clamp(Math.round(red * 0xff), 0, 0xff);
        int g = MathHelper.clamp(Math.round(green * 0xff), 0, 0xff);
        int b = MathHelper.clamp(Math.round(blue * 0xff), 0, 0xff);
        int a = MathHelper.clamp(Math.round(alpha * 0xff), 0, 0xff);
        return String.format("%02x%02x%02x%02x", r, g, b, a);
    }

    @Override
    public GammaRgbColor copy() {
        return new GammaRgbColor(red, green, blue, alpha);
    }

    public HslColor toHsl() {
        // based on https://stackoverflow.com/a/9493060/7448536
        float r = red, g = green, b = blue;

        float vmax = Math.max(r, Math.max(g, b));
        float vmin = Math.min(r, Math.min(g, b));
        float h = 0, s, l = (vmax + vmin) / 2;

        if (vmax == vmin) {
            return new HslColor(0, 0, l, alpha);
        }

        float d = vmax - vmin;
        s = l > 0.5 ? d / (2 - vmax - vmin) : d / (vmax + vmin);
        if (vmax == r) h = (g - b) / d + (g < b ? 6 : 0);
        if (vmax == g) h = (b - r) / d + 2;
        if (vmax == b) h = (r - g) / d + 4;
        h /= 6;

        return new HslColor(h * 360f, s, l);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GammaRgbColor that = (GammaRgbColor) o;
        return Float.compare(red, that.red) == 0 && Float.compare(green, that.green) == 0 && Float.compare(blue, that.blue) == 0 && Float.compare(alpha, that.alpha) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(red, green, blue, alpha);
    }
}
