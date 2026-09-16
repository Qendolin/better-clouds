#version 330 core
#extension GL_ARB_separate_shader_objects : require
#define pi 3.1415926536f

layout(location = 0) out vec4 fragColor;
layout(location = 0) in float fogFade;
layout(location = 1) in float tintInterp;
layout(location = 2) in vec3 lightSampleDir;

uniform sampler2D LightTexture;

layout (std140) uniform CloudFragData {
// note to self: don't mix floats and vecs, otherwise padding issues may occur
    float opacity, opacityFactor, opacityExponent;
    float brightness, gamma, saturation;
    float tintRed, tintGreen, tintBlue, bottomColorRed, bottomColorGreen, bottomColorBlue;
    float haloSize, mappedTime;
    float sunX, sunY, sunZ, sunAxisY, sunAxisZ;
};

#if LOD_ENABLED
layout(location = 3) in float lodDepth;
uniform sampler2D LodDepthTexture;
#endif

void main() {
    #if LOD_ENABLED
    // dhDepth is always 0 if the depth texture cloud not be set.
    // This is a "safety" check to prevent reading from an unbound texture
    if (lodDepth != 0) {
        float depth = texelFetch(LodDepthTexture, ivec2(gl_FragCoord.xy), 0).r;
        #if REVERSE_Z
        if (lodDepth < depth) discard;
        #else
        if (lodDepth > depth) discard;
        #endif
    }
    #endif

    vec3 sunDir = vec3(sunX, sunY, sunZ);
    vec3 fragDir = normalize(lightSampleDir);
    float lightUvX = dot(sunDir, fragDir) * 0.9;

    # if CELESTIAL_BODY_HALO
    vec3 xzProj = fragDir - sunDir * dot(fragDir, sunDir);
    float projAngle = acos(clamp(dot(xzProj / max(length(xzProj), 1e-6), vec3(0, sunAxisY, sunAxisZ)), -1.0, 1.0));
    float superellipseFalloff = dot(sunDir, fragDir);
    float sphere = dot(sunDir, fragDir);

    // i still have no idea how this formula works but it seems to work fine
    float cosine = cos(2.0 * projAngle);    // cosine may be negative, use cosine * cosine to avoid pow. in glsl, for whatever reason, pow(x, y) is nan with x is negative.
    float superellipse = (
    (1.0 + (1.0 / 3.0) * (cosine * cosine)) * haloSize * (1.0 - abs(superellipseFalloff)) - 1.0
    ) * sign(-superellipseFalloff);

    lightUvX = mix(sphere, superellipse, smoothstep(0.75, 1, abs(sphere)));
    #endif

    // (1, 0) to (0.5, 1)
    if (lightUvX > 0.5) lightUvX = (-2.0 * lightUvX + 2.0) * 0.375;
    // (0.5, 0) to (-0.5, 1)
    else if (lightUvX > -0.5) lightUvX = 0.375 + (-1.0 * lightUvX + 0.5) * 0.25;
    // (-0.5, 0) to (-1, 1)
    else lightUvX = 0.625 + (-2.0 * lightUvX - 1.0) * 0.375;

    vec2 lightUv = vec2(lightUvX, mappedTime);

    // prevent sampling the horizontally interpolated vertical edges
    lightUv.x -= (lightUv.x - 0.5) / textureSize(LightTexture, 0).x;

    vec3 lightColorRaw = texture(LightTexture, lightUv).rgb;
    float lightLumaRaw = dot(lightColorRaw, vec3(0.2126, 0.7152, 0.072)) + 0.001;
    vec3 lightChromaRaw = lightColorRaw / lightLumaRaw;

    vec3 lightChromaAdjusted = mix(vec3(1.0), lightChromaRaw, saturation);

    float lightLumaAdjusted = lightLumaRaw * brightness;
    lightLumaAdjusted = pow(lightLumaAdjusted, 1 / gamma);

    vec3 lightColor = lightChromaAdjusted * lightLumaAdjusted;

    vec3 rawColor = tintInterp * vec3(tintRed, tintGreen, tintBlue) + (1 - tintInterp) * vec3(bottomColorRed, bottomColorGreen, bottomColorBlue);
    vec3 color = lightColor * rawColor;
    float alpha = opacityFactor * opacity * pow(fogFade, opacityExponent);
    fragColor = vec4(color, alpha);
}
