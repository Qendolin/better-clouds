package com.qendolin.betterclouds.gui.color;

import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.NotImplementedException;
import org.joml.Vector4f;

public interface IColor<T extends IColor<T, B>, B extends IColor<B, ?>> {
    XYZColor toXYZ();
    default T lerp(IColor<?, ?> other, float factor) {
        return lerp(other, factor, ArcMode.SHORT);
    }
    T lerp(IColor<?, ?> other, float factor, ArcMode arcMode);

    default boolean isPolar() {
        return false;
    }

    Meta[] coordsMeta();

    Class<B> baseSpace();

    void setCoord(int i, float value);
    float getCoord(int i);
    default Vector4f getCoords() {
        Vector4f v = new Vector4f();
        int c = coordsMeta().length;
        for (int i = 0; i < c; i++) {
            v.setComponent(i, getCoord(i));
        }
        return v;
    }

    default void setCoords(Vector4f coords) {
        int c = coordsMeta().length;
        for (int i = 0; i < c; i++) {
            setCoord(i, coords.get(i));
        }
    }

    @SuppressWarnings("unchecked")
    default <S extends IColor<S, ?>> S to(Class<S> space) {
        if(this.getClass().equals(space)) {
            return (S) this;
        }

        XYZColor xyz = this.toXYZ();

        if (space == LabColor.class) {
            return (S) xyz.toLab();
        } else if(space == LchColor.class) {
            return (S) xyz.toLab().toLch();
        } else if(space == LinearRgbColor.class) {
            return (S) xyz.toLinearRgb();
        } else if(space == GammaRgbColor.class) {
            return (S) xyz.toLinearRgb().toGamma();
        } else if(space == HslColor.class) {
            return (S) xyz.toLinearRgb().toGamma().toHsl();
        } else {
            throw new NotImplementedException("conversion to " + space.getName() + " is not implemented");
        }
    }

    @SuppressWarnings("unchecked")
    default T convert(IColor<?,?> color) {
        return (T) color.to(this.getClass());
    }

    default boolean inGamut() {
        if(this.isPolar()) {
            return this.to(this.baseSpace()).inGamut();
        }

        float epsilon = 0.000075f;

        Meta[] meta = coordsMeta();

        for (int i = 0; i < meta.length; i++) {
            float c = getCoord(i);
            if(Float.isNaN(c)) continue;

            Meta m = meta[i];
            if(c >= m.min - epsilon && c <= m.max + epsilon) continue;

            return false;
        }

        return true;
    }

    default boolean toGamut() {

        if(inGamut()) return true;

        Meta[] meta = coordsMeta();

        for (int i = 0; i < meta.length; i++) {
            float c = getCoord(i);
            if(Float.isNaN(c)) continue;

            Meta m = meta[i];
            if(m.cyclic) {
                float range = m.max - m.min;
                c = (((c - m.min) % range) + range) % range + m.min;
                setCoord(i, c);
            } else {
                setCoord(i, MathHelper.clamp(c, m.min, m.max));
            }
        }

        return false;
    }

    enum ArcMode {
        SHORT, LONG, INCREASE, DECREASE
    }

    record Meta(String nameKey, float min, float max, boolean cyclic, float displayScale, ChannelFormat format) {}

    enum ChannelFormat {
        DECIMAL("%.3f"), INTEGER("%.0f"), DEGREES("%.1f°"), PERCENT("%.1f%%");

        final String formatString;
        ChannelFormat(String formatString) {
            this.formatString = formatString;
        }

        public String apply(float value) {
            return String.format(formatString, value);
        }
    }

    T copy();
}
