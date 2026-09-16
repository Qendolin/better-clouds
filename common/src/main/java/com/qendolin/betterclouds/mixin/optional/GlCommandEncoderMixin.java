package com.qendolin.betterclouds.mixin.optional;

import com.bawnorton.mixinsquared.TargetHandler;
import com.qendolin.betterclouds.compat.IrisCompat;
import com.qendolin.betterclouds.rendering.blaze3d.IrisFramebuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.mojang.renderpearl.backend.opengl.GlCommandEncoder", priority = 1500)
public abstract class GlCommandEncoderMixin {
    // this is where iris sets up the framebuffer, so we must set up our own afterward
    @TargetHandler(
            mixin = "net.irisshaders.iris.mixin.MixinGlCommandEncoder",
            name = "iris$bypassSetup",
            prefix = "handler"
    )
    @Inject(method = "@MixinSquared:Handler", at = @At("TAIL"), require = 1)
    private void injectFramebuffer(
            @Coerce Object glRenderPass, CallbackInfo cir, CallbackInfo ci
    ) {
        if (!IrisFramebuffer.isActive() || !IrisCompat.instance().isShadersEnabled())
            return;

        IrisFramebuffer.bind();
    }
}
