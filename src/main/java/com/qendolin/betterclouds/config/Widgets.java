package com.qendolin.betterclouds.config;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.Text;

import java.util.function.Function;

public class Widgets {
    public static RangeWidget<Integer> intRange(int x, int y, int width, int min, int max, int value, Function<Integer, String> messageMapper, ConfigScreen.ValueChangeCallback<Integer> onChange) {
        if(messageMapper == null) messageMapper = v -> String.format("%d", v);
        return new RangeWidget<>(x, y, width, value, v -> RangeWidget.mapIntToRange(v, min, max), messageMapper, onChange);
    }

    public static RangeWidget<Float> floatRange(int x, int y, int width, float min, float max, float step, float value, Function<Float, String> messageMapper, ConfigScreen.ValueChangeCallback<Float> onChange) {
        if(messageMapper == null) messageMapper = v -> String.format("%.4f", v).replaceAll("(?<=[\\.,]\\d{1,4})0+$", "");
        return new RangeWidget<>(x, y, width, value, (v) -> RangeWidget.mapFloatToRange(v, min, max, step), messageMapper, onChange);
    }

    public static ToggleButtonWidget toggleButton(int x, int y, int width, boolean value, Function<Boolean, String> messageMapper, ConfigScreen.ValueChangeCallback<Boolean> onChange) {
        if(messageMapper == null) messageMapper = v -> v ? "On" : "Off";
        return new ToggleButtonWidget(x, y, width, value, messageMapper, onChange);
    }
}

interface ValueHolder<V> {
    V getValue();
    void setValue(V value);
}

class RangeWidget<T extends Number> extends SliderWidget implements ValueHolder<T> {
    private final Function<Double, T> valueMapper;
    private final Function<T, String> messageMapper;
    private final ConfigScreen.ValueChangeCallback<T> onChange;
    private T mappedValue;
    public RangeWidget(int x, int y, int width, T value, Function<Double, T> valueMapper, Function<T, String> messageMapper, ConfigScreen.ValueChangeCallback<T> onChange) {
        super(x, y, width, 20, null, 0);
        this.valueMapper = valueMapper;
        this.messageMapper = messageMapper;
        this.onChange = onChange;
        this.value = reverseMap(value);
        applyValue();
        updateMessage();
    }

    private double reverseMap(T value) {
        T min = valueMapper.apply(0d);
        T max = valueMapper.apply(1d);
        if(min.doubleValue() == 0d && max.doubleValue() == 1d) return value.doubleValue();
        double mapped = (value.doubleValue() - min.doubleValue()) / (max.doubleValue() - min.doubleValue());
        return Math.round(mapped * 10e5) / 10e5;
    }

    public static float mapFloatToRange(Double v, float min, float max, float step) {
        v = v * (max - min) + min;
        if(step != 0) {
            v = (double) Math.round(v / step) * step;
        }
        return (float) (Math.round(v * 10e5) / 10e5);
    }

    public static int mapIntToRange(Double v, int min, int max) {
        v = v * (max - min) + min;
        return (int) Math.round(v);
    }

    @Override
    protected void updateMessage() {
        setMessage(Text.literal(this.messageMapper.apply(mappedValue)));
    }

    @Override
    protected void applyValue() {
        T old = mappedValue;
        mappedValue = valueMapper.apply(value);
        onChange.invoke(old, mappedValue);
    }

    @Override
    public T getValue() {
        return mappedValue;
    }

    @Override
    public void setValue(T v) {
        value = reverseMap(v);
        applyValue();
        updateMessage();
    }
}

class ToggleButtonWidget extends ButtonWidget implements ValueHolder<Boolean> {
    private final Function<Boolean, String> messageMapper;
    private final ConfigScreen.ValueChangeCallback<Boolean> onChange;
    private boolean value;
    public ToggleButtonWidget(int x, int y, int width, boolean value, Function<Boolean, String> messageMapper, ConfigScreen.ValueChangeCallback<Boolean> onChange) {
        super(x, y, width, 20, null, null);
        this.messageMapper = messageMapper;
        this.onChange = onChange;
        this.value = value;
        updateMessage();
    }

    @Override
    public void onPress() {
        this.value = !this.value;
        updateMessage();
    }

    protected void updateMessage() {
        setMessage(Text.literal(this.messageMapper.apply(value)));
    }

    @Override
    public Boolean getValue() {
        return this.value;
    }

    @Override
    public void setValue(Boolean value) {
        boolean old = this.value;
        this.value = value;
        onChange.invoke(old, value);
        updateMessage();
    }
}
