package com.qendolin.betterclouds.gui;

import com.google.common.collect.ImmutableSet;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.text.Text;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class CustomButtonOption implements ButtonOption {

    private final Supplier<Text> name;
    private final OptionDescription description;
    private final BiConsumer<YACLScreen, ButtonOption> action;
    private boolean available;
    private final Controller<BiConsumer<YACLScreen, ButtonOption>> controller;
    private final Binding<BiConsumer<YACLScreen, ButtonOption>> binding;

    public CustomButtonOption(
        @NotNull Supplier<Text> name,
        @NotNull OptionDescription description,
        @NotNull BiConsumer<YACLScreen, ButtonOption> action,
        boolean available
    ) {
        this.name = name;
        this.description = description;
        this.action = action;
        this.available = available;
        this.controller = new CustomActionController(this);
        this.binding = new EmptyBinderImpl();
    }

    public static Builder createBuilder() {
        return new Builder();
    }

    @Override
    public @NotNull Text name() {
        return name.get();
    }

    @Override
    public @NotNull OptionDescription description() {
        return description;
    }

    @Override
    public @NotNull Text tooltip() {
        return description().text();
    }

    @Override
    public BiConsumer<YACLScreen, ButtonOption> action() {
        return action;
    }

    @Override
    public boolean available() {
        return available;
    }

    @Override
    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public @NotNull Controller<BiConsumer<YACLScreen, ButtonOption>> controller() {
        return controller;
    }

    @Override
    public @NotNull Binding<BiConsumer<YACLScreen, ButtonOption>> binding() {
        return binding;
    }

    @Override
    public @NotNull ImmutableSet<OptionFlag> flags() {
        return ImmutableSet.of();
    }

    @Override
    public boolean changed() {
        return false;
    }

    @Override
    public @NotNull BiConsumer<YACLScreen, ButtonOption> pendingValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void requestSet(BiConsumer<YACLScreen, ButtonOption> value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean applyValue() {
        return false;
    }

    @Override
    public void forgetPendingValue() {

    }

    @Override
    public void requestSetDefault() {

    }

    @Override
    public boolean isPendingValueDefault() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addListener(BiConsumer<Option<BiConsumer<YACLScreen, ButtonOption>>, BiConsumer<YACLScreen, ButtonOption>> changedListener) {

    }

    protected static class EmptyBinderImpl implements Binding<BiConsumer<YACLScreen, ButtonOption>> {
        @Override
        public BiConsumer<YACLScreen, ButtonOption> getValue() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setValue(BiConsumer<YACLScreen, ButtonOption> value) {

        }

        @Override
        public BiConsumer<YACLScreen, ButtonOption> defaultValue() {
            throw new UnsupportedOperationException();
        }
    }

    @ApiStatus.Internal
    public static final class Builder {
        private Supplier<Text> name;
        private OptionDescription description = OptionDescription.EMPTY;
        private boolean available = true;
        private BiConsumer<YACLScreen, ButtonOption> action;

        public Builder name(@NotNull Supplier<Text> name) {
            Validate.notNull(name, "`name` cannot be null");

            this.name = name;
            return this;
        }

        public Builder description(@NotNull OptionDescription description) {
            Validate.notNull(description, "`description` cannot be null");

            this.description = description;
            return this;
        }

        public Builder action(@NotNull BiConsumer<YACLScreen, ButtonOption> action) {
            Validate.notNull(action, "`action` cannot be null");

            this.action = action;
            return this;
        }


        public Builder available(boolean available) {
            this.available = available;
            return this;
        }

        public ButtonOption build() {
            Validate.notNull(name, "`name` must not be null when building `Option`");
            Validate.notNull(action, "`action` must not be null when building `Option`");

            return new CustomButtonOption(name, description, action, available);
        }
    }
}
