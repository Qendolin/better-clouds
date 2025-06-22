package com.qendolin.betterclouds.mixin.runtime;

//? if >=1.21.6 {
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@SuppressWarnings("UnusedMixin")
@Mixin(SimpleOption.class)
public interface SimpleOptionAccessor {

    @Accessor
    <T> T getDefaultValue();
}
//? }