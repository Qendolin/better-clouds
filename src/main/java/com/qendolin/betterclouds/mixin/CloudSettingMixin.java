package com.qendolin.betterclouds.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.qendolin.betterclouds.Main;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Run before Iris (priority 1010)
@Mixin(value = GameOptions.class, priority = 1000)
public abstract class CloudSettingMixin {

    @Shadow
    @Final
    private SimpleOption<Integer> viewDistance;

    @Shadow
    @Final
    private SimpleOption<CloudRenderMode> cloudRenderMode;

//    @Inject(at = @At("HEAD"), method = "getCloudRenderModeValue", cancellable = true)
//    private void overrideCloudSetting(CallbackInfoReturnable<CloudRenderMode> cir) {
//        if (!Main.getConfig().cloudOverride) return;
//        if (viewDistance.getValue() < 4) {
//            return;
//        }
//        cir.setReturnValue(cloudRenderMode.getValue());
//    }

    @ModifyReturnValue(method = "getCloudRenderModeValue", at = @At("RETURN"))
    private CloudRenderMode overrideCloudRenderMode(CloudRenderMode value) {
        if (Main.getConfig().cloudOverride) {
            value = cloudRenderMode.getValue();
        }
        return value;
    }
}
