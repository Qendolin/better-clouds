package com.qendolin.betterclouds.clouds.shaders;

import com.google.common.collect.ImmutableMap;
import com.qendolin.betterclouds.Main;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.Map;

public class CoverageFarShader extends Shader {
    public static final String DEF_REGIONS = "_REGIONS_";

    public static final Identifier VERTEX_SHADER_ID = Identifier.of(Main.MODID, "shaders/core/betterclouds_far.vert");
    public static final Identifier FRAGMENT_SHADER_ID = Identifier.of(Main.MODID, "shaders/core/betterclouds_far.frag");

    public final Uniform uDepthTexture;
    public final Uniform uMVPMatrix;
    public final Uniform uSpacing;
    public final Uniform uCircle;

    public CoverageFarShader(ResourceManager resMan, Map<String, String> defs) throws IOException {
        super(resMan, VERTEX_SHADER_ID, FRAGMENT_SHADER_ID, defs);

        uDepthTexture = getUniform("u_depth_texture", false);
        uMVPMatrix = getUniform("u_mvp_matrix", false);
        uSpacing = getUniform("u_spacing", true);
        uCircle = getUniform("u_circle", true);
    }

    public static CoverageFarShader create(ResourceManager manager, int regions) throws IOException {
        Map<String, String> defs = ImmutableMap.ofEntries(
            Map.entry(CoverageFarShader.DEF_REGIONS, Integer.toString(regions))
        );
        return new CoverageFarShader(manager, defs);
    }
}
