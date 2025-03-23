#version 440 core

#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable

#define REGIONS _REGIONS_

layout (location=0) in vec2 in_pos;

layout (location=0) out vec3 out_color;
layout (location=1) out float out_one;

layout(binding = 6) uniform sampler2DArray u_height;
uniform float u_spacing;

uniform sampler2D u_depth_texture;

layout(std430, binding = 0) buffer RegionOffsets
{
    ivec2 ssbo_region_offsets[REGIONS * REGIONS];
    int ssbo_region_map[REGIONS * REGIONS];
};

void main() {

    ivec2 ipos = ivec2(in_pos / u_spacing);
    ivec2 region = ipos / 128;
    int region_i = region.x + REGIONS * region.y;
//    ivec2 region_off = ssbo_region_offsets[region_i];
//    region_i = ssbo_region_map[region_i];

    ivec2 texpos = ivec2(ipos.x % 128, ipos.y % 128);

    float value = texelFetch(u_height, ivec3(texpos, region_i), 0).r;

    out_color = vec3(0.0);
    out_one = 0.0;

    vec2 center = vec2(0.5 * (REGIONS * 128.0 * u_spacing));
    if(length(in_pos - center) < 128.0 * 16.0) discard;

    if(value == 0) discard;

    float depth = texelFetch(u_depth_texture, ivec2(gl_FragCoord.xy), 0).r;
    if(min(gl_FragCoord.z, 1.0) > depth) discard;

    out_color.r = 1.0;
    out_color.gb = vec2(1.0);
    out_one = 2.0 / 255.0;
}