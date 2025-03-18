package com.qendolin.betterclouds.compat;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.FogShape;

//? if >1.21.4 {
//?} elif >=1.21.3 {
import net.minecraft.client.gl.ShaderProgramKeys;
//?} else {
/*import net.minecraft.client.render.GameRenderer;
*///?}

public class RenderSystemWrapper {

    //? if <=1.21.4 {
    public static void setPositionColorShader() {
        //? if >=1.21.3 {
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        //?} else {
        /*RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        *///?}
    }
    //?}

    //? if <=1.21.4 {
    public static void setShader(ShaderProgram program) {
        //? if >=1.21.3 {
        RenderSystem.setShader(program);
        //?} else {
        /*RenderSystem.setShader(() -> program);
        *///?}
    }
    //?}

    public static Fog getFog() {
        //? if >=1.21.3 {
        var fog = RenderSystem.getShaderFog();
        return new Fog(fog);
        //?} else {
        /*float[] color = RenderSystem.getShaderFogColor();
        return new Fog(RenderSystem.getShaderFogStart(), RenderSystem.getShaderFogEnd(), RenderSystem.getShaderFogShape(), color[0], color[1], color[2], color[3]);
        *///?}
    }

    public record Fog(float start, float end, FogShape shape, float red, float green, float blue, float alpha) {
        public void apply() {
            //? if >=1.21.3 {
            RenderSystem.setShaderFog(new net.minecraft.client.render.Fog(start, end, shape, red, green, blue, alpha));
            //?} else {
            /*RenderSystem.setShaderFogStart(start);
            RenderSystem.setShaderFogEnd(end);
            RenderSystem.setShaderFogShape(shape);
            RenderSystem.setShaderFogColor(red, green, blue, alpha);
            *///?}
        }
        //? if >=1.21.3 {
        public Fog(net.minecraft.client.render.Fog fog) {
            this(fog.start(), fog.end(), fog.shape(), fog.red(), fog.green(), fog.blue(), fog.alpha());
        }
        //?}
    }
}
