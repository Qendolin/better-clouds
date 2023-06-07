package com.qendolin.betterclouds.clouds.shaders;

import com.qendolin.betterclouds.Main;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.Map;

public class ShadingShader extends Shader {
    public static final Identifier VERTEX_SHADER_ID = new Identifier(Main.MODID, "shaders/core/betterclouds_shading.vsh");
    public static final Identifier FRAGMENT_SHADER_ID = new Identifier(Main.MODID, "shaders/core/betterclouds_shading.fsh");

    public static final String DEF_BLIT_DEPTH_KEY = "_BLIT_DEPTH_";

    public final Uniform uDataTexture;
    public final Uniform uDepthTexture;
    public final Uniform uCoverageTexture;
    public final Uniform uLightTexture;
    public final Uniform uVPMatrix;
    public final Uniform uSunDirection;
    public final Uniform uSunAxis;
    public final Uniform uColorGrading;
    public final Uniform uOpacity;
    public final Uniform uTint;
    public final Uniform uNoiseFactor;

    public ShadingShader(ResourceManager resMan, Map<String, String> defs) throws IOException {
        super(resMan, VERTEX_SHADER_ID, FRAGMENT_SHADER_ID, defs);

        uDataTexture = getUniform("u_data_texture", false);
        uDepthTexture = getUniform("u_depth_texture", false);
        uCoverageTexture = getUniform("u_coverage_texture", false);
        uLightTexture = getUniform("u_light_texture", false);
        uVPMatrix = getUniform("u_vp_matrix", false);
        uSunDirection = getUniform("u_sun_direction", true);
        uSunAxis = getUniform("u_sun_axis", true);
        uColorGrading = getUniform("u_color_grading", true);
        uOpacity = getUniform("u_opacity", true);
        uTint = getUniform("u_tint", true);
        uNoiseFactor = getUniform("u_noise_factor", true);
    }

    public static ShadingShader create(ResourceManager manager, boolean writeDepth) throws IOException {
        Map<String, String> defs = Map.ofEntries(
            Map.entry(ShadingShader.DEF_BLIT_DEPTH_KEY, writeDepth ? "1" : "0")
        );
        return new ShadingShader(manager, defs);
    }
}
