package com.qendolin.betterclouds.mixin.required;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.qendolin.betterclouds.BetterClouds;
import com.qendolin.betterclouds.config.ConfigManager;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

// Run before Iris (priority 1010)
@Mixin(value = Options.class, priority = 1000)
public abstract class CloudSettingMixin {
    @Shadow
    @Final
    private OptionInstance<CloudStatus> cloudStatus;

    @SuppressWarnings("UnresolvedMixinReference")
    @WrapMethod(method = {"getCloudStatus", "method_1632", "m_92174_"}, remap = false)
    private CloudStatus overrideCloudRenderMode(Operation<CloudStatus> original) {
        if (BetterClouds.isEnabled() && ConfigManager.instance().cloudOverride) {
            return cloudStatus.get();
        }
        return original.call();
    }
}
