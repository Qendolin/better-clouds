#version 460

#define BLIT_DEPTH _BLIT_DEPTH_
#define REMAP_DEPTH _REMAP_DEPTH_

#if !BLIT_DEPTH
layout(early_fragment_tests) in;
#endif

in vec3 pass_dir;
in vec2 pass_uv;

layout (location=0) out vec4 frag_color;

#if BLIT_DEPTH
uniform sampler2D u_depth;
#if REMAP_DEPTH
uniform vec4 u_depthCoeffs;
#endif
#endif
// TODO: use uniform structs
uniform sampler2D u_data;
uniform usampler2D u_coverage;
uniform sampler2D u_lightTexture;
uniform vec4 u_sunData;
uniform vec4 u_effectColor;
uniform vec4 u_colorGrading;
uniform vec3 u_tint;
uniform mat4 u_inverseMat;

const float pi = 3.14159265359;

float linearize_depth(float hyp, float a, float b)
{
    return (hyp * 2 - 1) * a + b;
}

float hyperbolize_depth(float lin, float a, float b)
{
    return (lin * a + b) * 0.5 + 0.5;
}

float remap_depth(float d, float x, float y, float z, float w)
{
    return d*x*z - 0.5*x*z + 0.5*y*z + 0.5*w + 0.5;
}

void main() {
    vec3 cloudData = texelFetch(u_data, ivec2(gl_FragCoord), 0).rgb;
    #if BLIT_DEPTH
    if(cloudData == vec3(0.0)) discard;
    #else
    if(cloudData == vec3(0.0)) return;
    #endif

    float coverage = float(texelFetch(u_coverage, ivec2(gl_FragCoord), 0).r);
    // This is the "correct" formula
    // frag_color.a = 1. - pow((1.-u_effectColor.a), coverage);
    frag_color.a = pow(coverage, 1.5) / (1./(u_effectColor.a)+pow(coverage, 1.5)-1);

    vec3 sunDir = u_sunData.xyz;
    vec3 fragDir = normalize(pass_dir);
    vec3 xzDir = normalize(vec3(pass_dir.x, 0, pass_dir.z));

    vec3 xzProj = fragDir - sunDir * dot(fragDir, sunDir);
    float projAngle = acos(dot(normalize(xzProj), vec3(0, 0, 1)));

    float dotXY = dot(normalize(pass_dir.xy), normalize(sunDir.xy));
    float dotYZ = dot(normalize(pass_dir.yz), normalize(sunDir.yz));
    float dotX = dot(fragDir, vec3(1, 0, 0));
    float dotZ = dot(fragDir, vec3(0, 0, 1));

    float cotx = tan(fragDir.x / fragDir.y);
    float cotz = tan(fragDir.z / fragDir.y);

    // if sunDir.z is always 0, this can be optimized, but who cares
    float sphere = dot(sunDir, normalize(pass_dir));
    float square = abs(pass_dir.x) > abs(pass_dir.z) ? dotXY : dotYZ;
    float rsquare = (1-length(max(abs(vec2(1-abs(dotXY), 1-abs(dotYZ))), vec2(0)))) * sign(sunDir.y * fragDir.y);
    float superellipse = ((1.0 + (1./3.) * (pow(sin(2*projAngle + pi/2.), 2.0))) * (1.-abs(dot(sunDir, fragDir))) - 1.0) * sign(dot(sunDir, -fragDir));
    float lightUVx = mix(sphere, superellipse, smoothstep(0.75, 1.0, abs(sphere)));

    // (1, 0) to (0.5, 1)
    if(lightUVx > 0.5) lightUVx = (-2 * lightUVx + 2) * 0.375;
    // (0.5, 0) to (-0.5, 1)
    else if(lightUVx > -0.5) lightUVx = 0.375 + (-1 * lightUVx + 0.5) * 0.25;
    // (-0.5, 0) to (-1, 1)
    else lightUVx = 0.625 + (-2 * lightUVx - 1) * 0.375;

    vec2 lightUV = vec2(lightUVx, u_sunData.w);

    // Prevent sampling the horizontally interpolated vertical edges
    lightUV.x -= (lightUV.x - 0.5) / textureSize(u_lightTexture, 0).x;
    frag_color.rgb = texture(u_lightTexture, lightUV).rgb;

    float colorLumi = dot(frag_color.rgb, vec3(0.2126, 0.7152, 0.072)) + 0.001;
    vec3 colorChroma = frag_color.rgb / colorLumi;

    vec3 dataWeights = vec3(1, 3, 3);
    float colorVariance = length(vec3(1., 1. - pow(1. - cloudData.g, 3.) * 0.75, cloudData.b * 0.75 + 0.25) * dataWeights.rgb) / length(dataWeights);
    colorLumi = colorVariance * 0.35 * (0.3 + 0.7 * colorLumi) + 0.75 * colorLumi;
    colorLumi *= u_effectColor.r;
    colorChroma = mix(vec3(1.0), colorChroma, u_effectColor.r);
    colorChroma = mix(vec3(1.0), colorChroma, u_colorGrading.w);

    colorLumi *= u_colorGrading.x;
    colorLumi = pow(colorLumi, u_colorGrading.y);

    frag_color.rgb = colorChroma * colorLumi;
    frag_color.rgb *= u_tint;
    frag_color.a *= u_colorGrading.z;

#if BLIT_DEPTH
#if REMAP_DEPTH
    gl_FragDepth = hyperbolize_depth(linearize_depth(texture(u_depth, pass_uv).r, u_depthCoeffs.x, u_depthCoeffs.y), u_depthCoeffs.z, u_depthCoeffs.w);
//    gl_FragDepth = remap_depth(texture(u_depth, pass_uv).r, u_depthCoeffs.x, u_depthCoeffs.y, u_depthCoeffs.z, u_depthCoeffs.w);
#else
    gl_FragDepth = texture(u_depth, pass_uv).r;
#endif
#endif
}