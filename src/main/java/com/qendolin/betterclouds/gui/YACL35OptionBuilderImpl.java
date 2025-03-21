package com.qendolin.betterclouds.gui;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class YACL35OptionBuilderImpl<T> implements YACLOptionBuilder<T> {

    private final Option.Builder<T> delegate;

    public YACL35OptionBuilderImpl(Option.Builder<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public YACLOptionBuilder<T> name(@NotNull Text name) {
        delegate.name(name);
        return this;
    }

    @Override
    public YACLOptionBuilder<T> customController(@NotNull Function<Option<T>, Controller<T>> control) {
        delegate.customController(control);
        return this;
    }

    @Override
    public YACLOptionBuilder<T> available(boolean available) {
        delegate.available(available);
        return this;
    }

    @Override
    public Option<T> build() {
        return delegate.build();
    }

    @Override
    public YACLOptionBuilder<T> description(@NotNull OptionDescription description) {
        delegate.description(description);
        return this;
    }

    @Override
    public YACLOptionBuilder<T> listeners(@NotNull Collection<BiConsumer<Option<T>, T>> listeners) {
        delegate.listeners(listeners);
        return this;
    }

    @Override
    public YACLOptionBuilder<T> description(@NotNull Function<T, OptionDescription> tOptionDescriptionFunction) {
        delegate.description(tOptionDescriptionFunction);
        return this;
    }

    @Override
    public YACLOptionBuilder<T> binding(@NotNull Binding<T> binding) {
        delegate.binding(binding);

        return this;
    }

    @Override
    public YACLOptionBuilder<T> listener(@NotNull BiConsumer<Option<T>, T> listener) {
        delegate.listener(listener);
        return this;
    }

    @Override
    public YACLOptionBuilder<T> controller(@NotNull Function<Option<T>, ControllerBuilder<T>> controllerBuilder) {
        delegate.controller(controllerBuilder);
        return this;
    }

    @Override
    public YACLOptionBuilder<T> binding(@NotNull T def, @NotNull Supplier<@NotNull T> getter, @NotNull Consumer<@NotNull T> setter) {
        binding(Binding.generic(def, getter, setter));
        return this;
    }

    @Override
    public YACLOptionBuilder<T> flag(@NotNull OptionFlag... flag) {
        delegate.flag(flag);
        return this;
    }

    @Override
    public YACLOptionBuilder<T> instant(boolean instant) {
        delegate.instant(instant);
        return this;
    }
}
