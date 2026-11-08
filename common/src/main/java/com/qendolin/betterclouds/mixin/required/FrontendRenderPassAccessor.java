package com.qendolin.betterclouds.mixin.required;

import com.mojang.renderpearl.frontend.FrontendRenderPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FrontendRenderPass.class)
public interface FrontendRenderPassAccessor {
    @Accessor(value = "isClosed")
    boolean isClosed();
}
