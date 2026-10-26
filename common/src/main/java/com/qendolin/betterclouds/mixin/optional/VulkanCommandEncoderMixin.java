package com.qendolin.betterclouds.mixin.optional;

import com.bawnorton.mixinsquared.TargetHandler;
import com.qendolin.betterclouds.compat.IrisCompat;
import com.qendolin.betterclouds.rendering.blaze3d.IrisFramebuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder", priority = 1500)
public abstract class VulkanCommandEncoderMixin {
	@TargetHandler(
			mixin = "net.irisshaders.iris.mixin.MixinVulkanCommandEncoder",
			name = "iris$beginRenderPass",
			prefix = "wrapOperation"
	)
	@Inject(method = "@MixinSquared:Handler", at = @At("TAIL"), require = 1)
	private void injectFramebuffer(CallbackInfo ci) {
		if (!IrisFramebuffer.isActive() || !IrisCompat.instance.isShadersEnabled())
			return;

		IrisFramebuffer.bind();
	}
}
