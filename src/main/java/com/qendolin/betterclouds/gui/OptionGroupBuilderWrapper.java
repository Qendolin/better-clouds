package com.qendolin.betterclouds.gui;

import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public record OptionGroupBuilderWrapper(ListOption.Builder<?> delegate) implements OptionGroup.Builder {
    @Override
    public OptionGroup.Builder name(@NotNull Text name) {
        throw new AssertionError();
    }

    @Override
    public OptionGroup.Builder description(@NotNull OptionDescription description) {
        throw new AssertionError();
    }

    @Override
    public OptionGroup.Builder option(@NotNull Option<?> option) {
        throw new AssertionError();
    }

    @Override
    public OptionGroup.Builder options(@NotNull Collection<? extends Option<?>> options) {
        throw new AssertionError();
    }

    @Override
    public OptionGroup.Builder collapsed(boolean collapsible) {
        throw new AssertionError();
    }

    @Override
    public OptionGroup build() {
        return delegate.build();
    }
}
