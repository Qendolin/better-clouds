package com.qendolin.betterclouds.compat;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;

import java.util.function.Supplier;

public class RenderSystemWrapper {

    public static void setPositionColorShader() {
        //? if >=1.21.3 {
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        //?} else {
        /*RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        *///?}
    }

    public static void setShader(ShaderProgram program) {
        //? if >=1.21.3 {
        RenderSystem.setShader(program);
        //?} else {
        /*RenderSystem.setShader(() -> program);
        *///?}
    }
}
