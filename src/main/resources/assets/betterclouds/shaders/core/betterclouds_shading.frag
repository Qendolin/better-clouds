#version 330 core

#extension GL_ARB_separate_shader_objects : enable

#define BLIT_DEPTH _BLIT_DEPTH_
#define UINT_COVERAGE _UINT_COVERAGE_
#define CELESTIAL_BODY_HALO _CELESTIAL_BODY_HALO_

in vec3 pass_dir;

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
// brightness, gamma, unused, saturation
uniform vec4 u_color_grading;
// r, g, b
uniform vec3 u_tint;
// color noise factor
uniform float u_noise_factor;

// near, far
uniform vec2 u_depth_range;

const float pi = 3.14159265359;
const float sqrt2 = 1.41421356237;

void main() {
    // initialize out variables
    out_color = vec4(0.0);

    // r,g,b = fog fade, positional coloring, noise
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

    vec3 sun_dir = u_sun_direction.xyz;
    vec3 frag_dir = normalize(pass_dir);

    vec3 xz_proj = frag_dir - sun_dir * dot(frag_dir, sun_dir);
    float proj_angle = acos(dot(normalize(xz_proj), u_sun_axis));

    // if sunDir.z is always 0, this can be optimized, but who cares
    float sphere = dot(sun_dir, frag_dir);

#if CELESTIAL_BODY_HALO
    // TODO: document how I arrived at this formula
    float superellipse_falloff = dot(sun_dir, frag_dir);
    // Higher values -> smaller size
    const float superellipse_size = 3.0;
    float superellipse = (
        (1.0 + (1.0/3.0) * (pow(sin(2.0*proj_angle + pi/2.0), 2.0)))
        * (superellipse_size-abs(superellipse_falloff)*superellipse_size) - 1.0
    ) * sign(-superellipse_falloff);
    float light_uv_x = mix(sphere, superellipse, smoothstep(0.75, 1.0, abs(sphere)));
#else
    // FIXME: This is a dirty hack
    float light_uv_x = sphere * 0.9;
#endif

    // (1, 0) to (0.5, 1)
    if(light_uv_x > 0.5) light_uv_x = (-2.0 * light_uv_x + 2.0) * 0.375;
    // (0.5, 0) to (-0.5, 1)
    else if(light_uv_x > -0.5) light_uv_x = 0.375 + (-1.0 * light_uv_x + 0.5) * 0.25;
    // (-0.5, 0) to (-1, 1)
    else light_uv_x = 0.625 + (-2.0 * light_uv_x - 1.0) * 0.375;

    vec2 light_uv = vec2(light_uv_x, u_sun_direction.w);

    // Prevent sampling the horizontally interpolated vertical edges
    light_uv.x -= (light_uv.x - 0.5) / textureSize(u_light_texture, 0).x;
    out_color.rgb = texture(u_light_texture, light_uv).rgb;

    float color_lumi = dot(out_color.rgb, vec3(0.2126, 0.7152, 0.072)) + 0.001;
    vec3 color_chroma = out_color.rgb / color_lumi;

    float color_variance = length(vec2(1.0 - pow(1.0 - cloudData.g, 3.) * 0.75, cloudData.b * 0.75 + 0.25)) / sqrt2;
    color_lumi = mix(color_lumi, color_variance * 0.35 * (0.3 + 0.7 * color_lumi) + 0.75 * color_lumi, u_noise_factor);

    color_chroma = mix(vec3(1.0), color_chroma, u_color_grading.w);
    color_lumi *= u_color_grading.x;
    color_lumi = pow(color_lumi, u_color_grading.y);

    out_color.rgb = color_chroma * color_lumi;
    out_color.rgb *= u_tint;
    out_color.a *= u_opacity.y;
    out_color.a *= cloudData.r;

#if BLIT_DEPTH
    gl_FragDepth = texelFetch(u_depth_texture, ivec2(gl_FragCoord.xy), 0).r;
#endif
}