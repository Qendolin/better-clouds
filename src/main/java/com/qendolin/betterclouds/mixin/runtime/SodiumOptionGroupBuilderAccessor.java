package com.qendolin.betterclouds.mixin.runtime;

//? if >=1.21.1 && <1.21.11 {
/*import net.caffeinemc.mods.sodium.client.gui.options.Option;
import net.caffeinemc.mods.sodium.client.gui.options.OptionGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@SuppressWarnings("UnusedMixin")
@Mixin(value = OptionGroup.Builder.class, remap = false)
public interface SodiumOptionGroupBuilderAccessor {
    @Accessor
    List<Option<?>> getOptions();
}
*///?}
