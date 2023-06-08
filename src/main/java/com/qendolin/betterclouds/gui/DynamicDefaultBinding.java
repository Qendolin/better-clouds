package com.qendolin.betterclouds.gui;

import dev.isxander.yacl3.api.Binding;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class DynamicDefaultBinding<T> implements Binding<T> {
    private final Supplier<T> def;
    private final Supplier<T> getter;
    private final Consumer<T> setter;

    public DynamicDefaultBinding(Supplier<T> def, Supplier<T> getter, Consumer<T> setting) {
        this.def = def;
        this.getter = getter;
        this.setter = setting;
    }

    @Override
    public T getValue() {
        return getter.get();
    }

    @Override
    public void setValue(T value) {
        setter.accept(value);
    }

    @Override
    public T defaultValue() {
        return def.get();
    }
}
