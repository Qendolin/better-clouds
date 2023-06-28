#version 330 core

#extension GL_ARB_separate_shader_objects : enable

#define BLIT_DEPTH _BLIT_DEPTH_
#define UINT_COVERAGE _UINT_COVERAGE_

in vec3 pass_dir;
in vec2 pass_uv;

layout (location=0) out vec4 out_color;

#if BLIT_DEPTH
uniform sampler2D u_depth_texture;
#endif

uniform sampler2D u_data_texture;
#if UINT_COVERAGE
uniform usampler2D u_coverage_texture;
#else
uniform sampler2D u_coverage_texture;
#endif
uniform sampler2D u_light_texture;
// x, y, z, time of day
uniform vec4 u_sun_direction;
// x, y, z
uniform vec3 u_sun_axis;
// opacity, opacity factor, opacity exponent
uniform vec3 u_opacity;
// brightness, gamma, desaturated brightness, saturation
uniform vec4 u_color_grading;
// r, g, b
uniform vec3 u_tint;
// color noise factor
uniform float u_noise_factor;

const float pi = 3.14159265359;
const float sqrt2 = 1.41421356237;

void main() {
    // initialize out variables
    out_color = vec4(0.0);

    vec3 cloudData = texelFetch(u_data_texture, ivec2(gl_FragCoord), 0).rgb;
    #if BLIT_DEPTH
    if(cloudData == vec3(0.0)) discard;
    #else
    if(cloudData == vec3(0.0)) return;
    #endif

#if UINT_COVERAGE
    float coverage = float(texelFetch(u_coverage_texture, ivec2(gl_FragCoord), 0).r);
#else
    float coverage = texelFetch(u_coverage_texture, ivec2(gl_FragCoord), 0).r * 255.0;
#endif
    // This is the "correct" formula
    // frag_color.a = 1.0 - pow((1.0-u_opacity.x), coverage);
    out_color.a = pow(coverage, u_opacity.z) / (1.0/(u_opacity.x)+pow(coverage, u_opacity.z)-1.0);

    vec3 sunDir = u_sun_direction.xyz;
    vec3 fragDir = normalize(pass_dir);

    vec3 xzProj = fragDir - sunDir * dot(fragDir, sunDir);
    float projAngle = acos(dot(normalize(xzProj), u_sun_axis));

    // if sunDir.z is always 0, this can be optimized, but who cares
    float sphere = dot(sunDir, fragDir);
    // TODO: document how I arrived at this formula
    float superellipseFalloff = dot(sunDir, fragDir);
    // Higher values -> smaller size
    const float superellipseSize = 3.0;
    float superellipse = (
        (1.0 + (1.0/3.0) * (pow(sin(2.0*projAngle + pi/2.0), 2.0)))
        * (superellipseSize-abs(superellipseFalloff)*superellipseSize) - 1.0
    ) * sign(-superellipseFalloff);
    float lightUVx = mix(sphere, superellipse, smoothstep(0.75, 1.0, abs(sphere)));

    // (1, 0) to (0.5, 1)
    if(lightUVx > 0.5) lightUVx = (-2.0 * lightUVx + 2.0) * 0.375;
    // (0.5, 0) to (-0.5, 1)
    else if(lightUVx > -0.5) lightUVx = 0.375 + (-1.0 * lightUVx + 0.5) * 0.25;
    // (-0.5, 0) to (-1, 1)
    else lightUVx = 0.625 + (-2.0 * lightUVx - 1.0) * 0.375;

    vec2 lightUV = vec2(lightUVx, u_sun_direction.w);

    // Prevent sampling the horizontally interpolated vertical edges
    lightUV.x -= (lightUV.x - 0.5) / textureSize(u_light_texture, 0).x;
    out_color.rgb = texture(u_light_texture, lightUV).rgb;

    float colorLumi = dot(out_color.rgb, vec3(0.2126, 0.7152, 0.072)) + 0.001;
    vec3 colorChroma = out_color.rgb / colorLumi;

    float colorVariance = length(vec2(1.0 - pow(1.0 - cloudData.g, 3.) * 0.75, cloudData.b * 0.75 + 0.25)) / sqrt2;
    colorLumi = mix(colorLumi, colorVariance * 0.35 * (0.3 + 0.7 * colorLumi) + 0.75 * colorLumi, u_noise_factor);

    colorChroma = mix(vec3(1.0), colorChroma, u_color_grading.z);
    colorChroma = mix(vec3(1.0), colorChroma, u_color_grading.w);
    colorLumi *= u_color_grading.z;
    colorLumi *= u_color_grading.x;
    colorLumi = pow(colorLumi, u_color_grading.y);

    out_color.rgb = colorChroma * colorLumi;
    out_color.rgb *= u_tint;
    out_color.a *= u_opacity.y;

#if BLIT_DEPTH
    gl_FragDepth = texture(u_depth_texture, pass_uv).r;
#endif
}