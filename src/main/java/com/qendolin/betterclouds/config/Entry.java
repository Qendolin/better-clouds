package com.qendolin.betterclouds.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Function;

public abstract class Entry {
    static void init() {
        ConfigScreen.registerWidgetFactory(IntRange.class, (a, x, y, w, h, f, o, s, c) -> Widgets.intRange(x,
                y,
                w,
                a.min(),
                a.max(),
                f.getInt(o),
                (Function<Integer, String>) s,
                (ConfigScreen.ValueChangeCallback<Integer>) c));
        ConfigScreen.registerWidgetFactory(FloatRange.class, (a, x, y, w, h, f, o, s, c) -> Widgets.floatRange(x,
                y,
                w,
                a.min(),
                a.max(),
                a.step(),
                f.getFloat(o),
                (Function<Float, String>) s,
                (ConfigScreen.ValueChangeCallback<Float>) c));
        ConfigScreen.registerWidgetFactory(ToggleButton.class, (a, x, y, w, h, f, o, s, c) -> Widgets.toggleButton(x,
                y,
                w,
                f.getBoolean(o),
                (Function<Boolean, String>) s,
                (ConfigScreen.ValueChangeCallback<Boolean>) c));
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface IntRange {
        int min() default 0;
        int max();
        String stringer() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface FloatRange {
        float min() default 0f;
        float max() default 1f;
        float step() default 0.05f;
        String stringer() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ToggleButton {
        String stringer() default "";
    }
}
