package com.qendolin.betterclouds.clouds;

import com.qendolin.betterclouds.Main;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.Map;

public class BlitShader extends Shader {
    public static final Identifier VERTEX_SHADER_ID = new Identifier(Main.MODID, "shaders/core/betterclouds_blit.vsh");
    public static final Identifier FRAGMENT_SHADER_ID = new Identifier(Main.MODID, "shaders/core/betterclouds_blit.fsh");

    public static final String DEF_BLIT_DEPTH_KEY = "_BLIT_DEPTH_";
    public static final String DEF_REMAP_DEPTH_KEY = "_REMAP_DEPTH_";

    public final Uniform uData;
    public final Uniform uDepth;
    public final Uniform uCoverage;
    public final Uniform uDepthCoeffs;
    public final Uniform uDepthRange;
    public final Uniform uEffectColor;
    public final Uniform uSunData;
    public final Uniform uColorGrading;
    public final Uniform uInverseMat;
    public final Uniform uLightTexture;
    public final Uniform uTint;

    public BlitShader(ResourceManager resMan, Map<String, String> defs) throws IOException {
        super(resMan, VERTEX_SHADER_ID, FRAGMENT_SHADER_ID, defs);

        uData = getUniform("u_data", false);
        uDepth = getUniform("u_depth", false);
        uCoverage = getUniform("u_coverage", false);
        uLightTexture = getUniform("u_lightTexture", false);
        uDepthCoeffs = getUniform("u_depthCoeffs", true);
        uDepthRange = getUniform("u_depthRange", true);
        uEffectColor = getUniform("u_effectColor", true);
        uSunData = getUniform("u_sunData", true);
        uColorGrading = getUniform("u_colorGrading", true);
        uInverseMat = getUniform("u_inverseMat", false);
        uTint = getUniform("u_tint", true);
    }
}
