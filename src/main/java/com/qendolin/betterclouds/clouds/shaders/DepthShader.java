package com.qendolin.betterclouds.clouds.shaders;

import com.qendolin.betterclouds.Main;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.Map;

public class DepthShader extends Shader {
    public static final Identifier VERTEX_SHADER_ID = new Identifier(Main.MODID, "shaders/core/betterclouds_depth.vsh");
    public static final Identifier FRAGMENT_SHADER_ID = new Identifier(Main.MODID, "shaders/core/betterclouds_depth.fsh");
    public static final String DEF_DEPTH_LAYOUT_QUALIFIER_KEY = "_DEPTH_LAYOUT_QUALIFIER_";

    public final Uniform uDepthTexture;

    public DepthShader(ResourceManager resMan, Map<String, String> defs) throws IOException {
        super(resMan, VERTEX_SHADER_ID, FRAGMENT_SHADER_ID, defs);

        uDepthTexture = getUniform("u_depth_texture", false);
    }

    public static DepthShader create(ResourceManager manager, boolean depthLayoutQualifier) throws IOException {
        Map<String, String> defs = Map.ofEntries(
            Map.entry(DepthShader.DEF_DEPTH_LAYOUT_QUALIFIER_KEY, depthLayoutQualifier ? "1" : "0"),
            Map.entry(Shader.DEF_VERSION_KEY, depthLayoutQualifier ? "400" : "130")
        );
        return new DepthShader(manager, defs);
    }
}
