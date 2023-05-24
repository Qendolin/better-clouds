package com.qendolin.betterclouds.clouds;

import com.qendolin.betterclouds.Main;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.Map;

public class DepthShader extends Shader {
    public static final Identifier VERTEX_SHADER_ID = new Identifier(Main.MODID, "shaders/core/betterclouds_depth.vsh");
    public static final Identifier FRAGMENT_SHADER_ID = new Identifier(Main.MODID, "shaders/core/betterclouds_depth.fsh");

    public final Uniform uDepth;
    public final Uniform uDepthCoeffs;

    public DepthShader(ResourceManager resMan, Map<String, String> defs) throws IOException {
        super(resMan, VERTEX_SHADER_ID, FRAGMENT_SHADER_ID, defs);

        uDepth = getUniform("u_depth", false);
        uDepthCoeffs = getUniform("u_depthCoeffs", true);
    }
}
