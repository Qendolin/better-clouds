package com.qendolin.betterclouds.mixin;

import net.minecraft.client.render.Shader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Shader.class)
public interface ShaderProgramAccessor {
    @Accessor("activeShaderId")
    static int getActiveShaderId() {
        throw new AssertionError();
    }

    @Accessor("activeShaderId")
    static void setActiveShaderId(int id) {
        throw new AssertionError();
    }
}
