package com.qendolin.betterclouds.mixin;

import net.minecraft.client.gl.ShaderProgram;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShaderProgram.class)
public interface ShaderProgramAccessor {
    //? if >=1.21.3 {
    //?} else {
    /*@Accessor("activeProgramGlRef")
    static int getActiveProgramGlRef() {
        throw new AssertionError();
    }

    @Accessor("activeProgramGlRef")
    static void setActiveProgramGlRef(int id) {
        throw new AssertionError();
    }
    *///?}
}
