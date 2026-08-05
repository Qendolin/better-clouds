package com.qendolin.betterclouds.mixin.optional;

import com.bawnorton.mixinsquared.TargetHandler;
import com.qendolin.betterclouds.compat.IrisCompat;
import com.qendolin.betterclouds.rendering.blaze3d.IrisFramebuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(targets = "com.mojang.blaze3d.opengl.GlCommandEncoder", priority = 1500)
public abstract class GlCommandEncoderMixin {
    @TargetHandler(
            mixin = "net.irisshaders.iris.mixin.MixinGlCommandEncoder",
            name = "iris$setupState",
            prefix = "handler"
    )
    @Inject(method = "@MixinSquared:Handler", at = @At("TAIL"), require = 1)
    private void injectFramebuffer(
            @Coerce Object glRenderPass,
            Collection<String> requiredUniforms,
            CallbackInfoReturnable<Boolean> originalCir,
            CallbackInfo ci
    ) {
        if (!originalCir.getReturnValueZ() || !IrisFramebuffer.isActive() || !IrisCompat.instance().isShadersEnabled())
            return;

        IrisCompat.instance().bindFramebuffer();
    }
}
