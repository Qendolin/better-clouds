package com.qendolin.betterclouds.mixin.required;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.qendolin.betterclouds.BetterClouds;
import com.qendolin.betterclouds.config.ConfigManager;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

// Run before Iris (priority 1010)
@Mixin(value = GameOptions.class, priority = 1000)
public abstract class CloudSettingMixin {
    @Shadow
    @Final
    private SimpleOption<CloudRenderMode> cloudRenderMode;

    @SuppressWarnings("UnresolvedMixinReference")
    @WrapMethod(method = {"getCloudRenderModeValue", "getCloudsType", "method_1632", "m_92174_"}, remap = false)
    private CloudRenderMode overrideCloudRenderMode(Operation<CloudRenderMode> original) {
        if (BetterClouds.isEnabled() && ConfigManager.instance().cloudOverride) {
            return cloudRenderMode.getValue();
        }
        return original.call();
    }
}
