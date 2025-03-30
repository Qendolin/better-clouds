package com.qendolin.betterclouds.mixin.required;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.qendolin.betterclouds.BetterClouds;
import com.qendolin.betterclouds.config.ConfigManager;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

// Run before Iris (priority 1010)
@Mixin(value = GameOptions.class, priority = 1000)
public abstract class CloudSettingMixin {
    @Shadow
    @Final
    private SimpleOption<CloudRenderMode> cloudRenderMode;

    @SuppressWarnings("UnresolvedMixinReference")
    @ModifyReturnValue(method = {"getCloudRenderModeValue", "getCloudsType", "method_1632", "m_92174_"}, remap = false, at = @At("RETURN"))
    private CloudRenderMode overrideCloudRenderMode(CloudRenderMode value) {
        if (!BetterClouds.isEnabled())
            return value;
        if (ConfigManager.instance().cloudOverride) {
            value = cloudRenderMode.getValue();
        }
        return value;
    }
}
