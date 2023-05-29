package com.qendolin.betterclouds.clouds;

import com.qendolin.betterclouds.Main;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.Map;

public class BlitShader extends Shader {
    public static final Identifier VERTEX_SHADER_ID = new Identifier(Main.MODID, "shaders/core/betterclouds_shading.vsh");
    public static final Identifier FRAGMENT_SHADER_ID = new Identifier(Main.MODID, "shaders/core/betterclouds_shading.fsh");

    public static final String DEF_BLIT_DEPTH_KEY = "_BLIT_DEPTH_";
    public static final String DEF_REMAP_DEPTH_KEY = "_REMAP_DEPTH_";

    public final Uniform uDataTexture;
    public final Uniform uDepthTexture;
    public final Uniform uCoverageTexture;
    public final Uniform uLightTexture;
    public final Uniform uInverseVPMatrix;
    public final Uniform uDepthTransform;
    public final Uniform uSunDirection;
    public final Uniform uColorGrading;
    public final Uniform uOpacity;
    public final Uniform uTint;

    public BlitShader(ResourceManager resMan, Map<String, String> defs) throws IOException {
        super(resMan, VERTEX_SHADER_ID, FRAGMENT_SHADER_ID, defs);

        uDataTexture = getUniform("u_data_texture", false);
        uDepthTexture = getUniform("u_depth_texture", false);
        uCoverageTexture = getUniform("u_coverage_texture", false);
        uLightTexture = getUniform("u_light_texture", false);
        uInverseVPMatrix = getUniform("u_inverse_vp_matrix", false);
        uDepthTransform = getUniform("u_depth_transform", true);
        uSunDirection = getUniform("u_sun_direction", true);
        uColorGrading = getUniform("u_color_grading", true);
        uOpacity = getUniform("u_opacity", true);
        uTint = getUniform("u_tint", true);
    }
}
