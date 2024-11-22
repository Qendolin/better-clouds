package com.qendolin.betterclouds.gui;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import net.minecraft.text.Text;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class OptionBuilderWrapper<T> {

    private final Option.Builder<T> delegate;

    //? if yacl: >=3.6.0 {
    private boolean instant;
    private Binding<T> binding;
    private final Set<OptionFlag> flags = new HashSet<>();
    //?}

    public OptionBuilderWrapper(Option.Builder<T> delegate) {
        this.delegate = delegate;
    }

    /**
     * Sets the name to be used by the option.
     *
     * @param name
     * @see Option#name()
     */
    public OptionBuilderWrapper<T> name(@NotNull Text name) {
        delegate.name(name);
        return this;
    }

    /**
     * Sets the controller for the option.
     * This is how you interact and change the options.
     *
     * @param control
     * @see dev.isxander.yacl3.gui.controllers
     */
    public OptionBuilderWrapper<T> customController(@NotNull Function<Option<T>, Controller<T>> control) {
        delegate.customController(control);
        return this;
    }

    /**
     * Sets if the option can be configured
     *
     * @param available
     * @see Option#available()
     */
    public OptionBuilderWrapper<T> available(boolean available) {
        delegate.available(available);
        return this;
    }

    public Option<T> build() {
        //? if yacl: >=3.6.0 {
        if(instant) {
            if (binding == null) {
                throw new IllegalStateException("Cannot build option with instant when binding is not set");
            }
            Validate.isTrue(flags.isEmpty(), "instant application does not support option flags");

            delegate.stateManager(StateManager.createInstant(binding));
        } else {
            delegate.stateManager(StateManager.createSimple(binding));
        }
        //?}
        return delegate.build();
    }

    /**
     * Sets the description to be used by the option.
     *
     * @param description the static description.
     * @return this builder
     * @see OptionDescription
     */
    public OptionBuilderWrapper<T> description(@NotNull OptionDescription description) {
        delegate.description(description);
        return this;
    }

    /**
     * Adds multiple listeners to the option. Invoked upon changing the pending value.
     *
     * @param listeners
     * @see Option#addListener(BiConsumer)
     */
    public OptionBuilderWrapper<T> listeners(@NotNull Collection<BiConsumer<Option<T>, T>> listeners) {
        //? if yacl: >=3.6.0 {
        delegate.addListeners(listeners.stream()
            .map(listener ->
                (OptionEventListener<T>) (opt, event) ->
                    listener.accept(opt, opt.pendingValue())
            ).toList()
        );
        //?} else {
        /*delegate.listeners(listeners);
        *///?}
        return this;
    }

    /**
     * Sets the function to get the description by the option's current value.
     *
     * @param tOptionDescriptionFunction the function to get the description by the option's current value.
     * @return this builder
     * @see OptionDescription
     */
    public OptionBuilderWrapper<T> description(@NotNull Function<T, OptionDescription> tOptionDescriptionFunction) {
        delegate.description(tOptionDescriptionFunction);
        return this;
    }

    /**
     * Sets the binding for the option.
     * Used for default, getter and setter.
     * Under-the-hood, this creates a state manager that is individual to the option, sharing state with no options.
     *
     * @param binding
     * @see Binding
     */
    public OptionBuilderWrapper<T> binding(@NotNull Binding<T> binding) {
        //? if yacl: >=3.6.0 {
        this.binding = binding;
        //?} else {
        /*delegate.binding(binding);
        *///?}

        return this;
    }

    /**
     * Adds a flag to the option.
     * Upon applying changes, all flags are executed.
     * {@link Option#flags()}
     *
     * @param flags
     */
    public OptionBuilderWrapper<T> flags(@NotNull Collection<? extends OptionFlag> flags) {
        //? if yacl: >=3.6.0
        this.flags.addAll(flags);

        delegate.flags(flags);
        return this;
    }

    /**
     * Adds a listener to the option. Invoked upon changing the pending value.
     *
     * @param listener
     * @see Option#addListener(BiConsumer)
     */
    public OptionBuilderWrapper<T> listener(@NotNull BiConsumer<Option<T>, T> listener) {
        //? if yacl: >=3.6.0 {
        delegate.addListener((opt, event) -> listener.accept(opt, opt.pendingValue()));
        //?} else {
        /*delegate.listener(listener);
        *///?}
        return this;
    }

    /**
     * Supplies a controller for this option. A controller is the GUI control to interact with the option.
     *
     * @param controllerBuilder
     * @return this builder
     */
    public OptionBuilderWrapper<T> controller(@NotNull Function<Option<T>, ControllerBuilder<T>> controllerBuilder) {
        delegate.controller(controllerBuilder);
        return this;
    }

    /**
     * Sets the binding for the option.
     * Shorthand of {@link Binding#generic(Object, Supplier, Consumer)}
     *
     * @param def    default value of the option, used to reset
     * @param getter should return the current value of the option
     * @param setter should set the option to the supplied value
     * @see Binding
     */
    public OptionBuilderWrapper<T> binding(@NotNull T def, @NotNull Supplier<@NotNull T> getter, @NotNull Consumer<@NotNull T> setter) {
        binding(Binding.generic(def, getter, setter));
        return this;
    }

    /**
     * Adds a flag to the option.
     * Upon applying changes, all flags are executed.
     * {@link Option#flags()}
     *
     * @param flag
     */
    public OptionBuilderWrapper<T> flag(@NotNull OptionFlag... flag) {
        delegate.flag(flag);
        return this;
    }

    /**
     * Instantly invokes the binder's setter when modified in the GUI.
     * Prevents the user from undoing the change
     * <p>
     * Does not support {@link Option#flags()}!
     *
     * @param instant
     */
    public OptionBuilderWrapper<T> instant(boolean instant) {
        //? if yacl: >=3.6.0 {
        this.instant = instant;
        //?} else {
        /*delegate.instant(instant);
        *///?}
        return this;
    }
}
