package com.qendolin.betterclouds.mixin.runtime;

import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@SuppressWarnings("UnusedMixin")
@Mixin(OptionInstance.class)
public interface SimpleOptionAccessor {

    @Accessor
    <T> T getInitialValue();
}
