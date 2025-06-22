package com.qendolin.betterclouds.mixin.runtime;

//? if >=1.21.6 {
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.qendolin.betterclouds.BetterClouds;
import com.qendolin.betterclouds.config.ConfigManager;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@SuppressWarnings("UnusedMixin")
@Mixin(DimensionType.class)
public class DimensionTypeMixin {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @ModifyReturnValue(method = "cloudHeight", at = @At("RETURN"))
    private Optional<Integer> addCloudsYOffset(Optional<Integer> original) {
        if(original.isEmpty() || !BetterClouds.isEnabled()) return original;

        return Optional.of(original.get() + (int) ConfigManager.instance().yOffset);
    }
}
//?}