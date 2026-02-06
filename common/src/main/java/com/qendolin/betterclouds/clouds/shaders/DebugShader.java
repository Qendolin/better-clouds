package com.qendolin.betterclouds.clouds.shaders;

import com.qendolin.betterclouds.BetterCloudsStatic;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.Map;

public class DebugShader extends Shader {

    public static final Identifier VERTEX_SHADER_ID = Identifier.of(BetterCloudsStatic.MODID, "shaders/core/debug.vsh");
    public static final Identifier FRAGMENT_SHADER_ID = Identifier.of(BetterCloudsStatic.MODID, "shaders/core/debug.fsh");

    public final Uniform uModelViewMatrix;
    public final Uniform uProjectionMatrix;
    public final Uniform uColorModulator;

    public DebugShader(ResourceManager resMan) throws IOException {
        super(resMan, VERTEX_SHADER_ID, FRAGMENT_SHADER_ID, Map.of());
        uModelViewMatrix = getUniform("u_model_view_matrix", false);
        uProjectionMatrix = getUniform("u_projection_matrix", false);
        uColorModulator = getUniform("u_color_modulator", true);

    }
}
