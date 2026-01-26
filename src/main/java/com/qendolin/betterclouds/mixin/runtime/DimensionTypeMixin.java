package com.qendolin.betterclouds.mixin.runtime;

//? if >=1.21.6 && <1.21.11 {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.qendolin.betterclouds.BetterClouds;
import com.qendolin.betterclouds.config.ConfigManager;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

@SuppressWarnings("UnusedMixin")
@Mixin(DimensionType.class)
public class DimensionTypeMixin {
    // Have to use WrapMethod because of Sodium Extra
    @WrapMethod(method = "cloudHeight")
    private Optional<Integer> addCloudsYOffset(Operation<Optional<Integer>> original) {
        var result = original.call();
        if(result.isEmpty() || !BetterClouds.isEnabled()) return result;

        return Optional.of(result.get() + (int) ConfigManager.instance().yOffset);
    }
}
*///?}
