package com.qendolin.betterclouds.mixin;

import net.minecraft.client.gl.ShaderProgram;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShaderProgram.class)
public interface ShaderProgramAccessor {
    @Accessor("activeProgramGlRef")
    static int getActiveProgramGlRef() {
        // can be called during hot swap
        return 0;
    }

    @Accessor("activeProgramGlRef")
    static void setActiveProgramGlRef(int id) {
        throw new AssertionError();
    }
}
