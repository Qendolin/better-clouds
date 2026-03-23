package com.qendolin.betterclouds.gui;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface YACLOptionBuilder<T> {

    static <T> YACLOptionBuilder<T> create(Option.Builder<T> delegate) {
        return new YACL36OptionBuilderImpl<>(delegate);
    }

    /**
     * Sets the name to be used by the option.
     *
     * @param name
     * @see Option#name()
     */
    YACLOptionBuilder<T> name(@NotNull Component name);

    /**
     * Sets the controller for the option.
     * This is how you interact and change the options.
     *
     * @param control
     * @see dev.isxander.yacl3.gui.controllers
     */
    YACLOptionBuilder<T> customController(@NotNull Function<Option<T>, Controller<T>> control);

    /**
     * Sets if the option can be configured
     *
     * @param available
     * @see Option#available()
     */
    YACLOptionBuilder<T> available(boolean available);

    Option<T> build();

    /**
     * Sets the description to be used by the option.
     *
     * @param description the static description.
     * @return this builder
     * @see OptionDescription
     */
    YACLOptionBuilder<T> description(@NotNull OptionDescription description);

    /**
     * Adds multiple listeners to the option. Invoked upon changing the pending value.
     *
     * @param listeners
     * @see Option#addListener(BiConsumer)
     */
    YACLOptionBuilder<T> listeners(@NotNull Collection<BiConsumer<Option<T>, T>> listeners);

    /**
     * Sets the function to get the description by the option's current value.
     *
     * @param tOptionDescriptionFunction the function to get the description by the option's current value.
     * @return this builder
     * @see OptionDescription
     */
    YACLOptionBuilder<T> description(@NotNull Function<T, OptionDescription> tOptionDescriptionFunction);

    /**
     * Sets the binding for the option.
     * Used for default, getter and setter.
     * Under-the-hood, this creates a state manager that is individual to the option, sharing state with no options.
     *
     * @param binding
     * @see Binding
     */
    YACLOptionBuilder<T> binding(@NotNull Binding<T> binding);

    /**
     * Adds a listener to the option. Invoked upon changing the pending value.
     *
     * @param listener
     * @see Option#addListener(BiConsumer)
     */
    YACLOptionBuilder<T> listener(@NotNull BiConsumer<Option<T>, T> listener);

    /**
     * Supplies a controller for this option. A controller is the GUI control to interact with the option.
     *
     * @param controllerBuilder
     * @return this builder
     */
    YACLOptionBuilder<T> controller(@NotNull Function<Option<T>, ControllerBuilder<T>> controllerBuilder);

    /**
     * Sets the binding for the option.
     * Shorthand of {@link Binding#generic(Object, Supplier, Consumer)}
     *
     * @param def    default value of the option, used to reset
     * @param getter should return the current value of the option
     * @param setter should set the option to the supplied value
     * @see Binding
     */
    YACLOptionBuilder<T> binding(T def, @NotNull Supplier<T> getter, @NotNull Consumer<T> setter);

    /**
     * Adds a flag to the option.
     * Upon applying changes, all flags are executed.
     * {@link Option#flags()}
     *
     * @param flag
     */
    YACLOptionBuilder<T> flag(@NotNull OptionFlag... flag);

    /**
     * Instantly invokes the binder's setter when modified in the GUI.
     * Prevents the user from undoing the change
     * <p>
     * Does not support {@link Option#flags()}!
     *
     * @param instant
     */
    YACLOptionBuilder<T> instant(boolean instant);
}
