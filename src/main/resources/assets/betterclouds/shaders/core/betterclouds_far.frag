#version 440 core

#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable

#define REGIONS _REGIONS_

layout (location=0) in vec2 in_pos;
layout (location=1) in vec2 in_world;

layout (location=0) out vec3 out_color;
layout (location=1) out float out_one;

layout(binding = 6) uniform sampler2DArray u_height;
uniform float u_spacing;
uniform vec3 u_circle;

uniform sampler2D u_depth_texture;

const float dither_matrix[16] = float[](
    0.0, 0.5, 0.125, 0.625,
    0.75, 0.25, 0.875, 0.375,
    0.0625, 0.5625, 0.03125, 0.53125,
    0.8125, 0.4375, 0.78125, 0.40625
);


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
    region_i = ssbo_region_map[region_i];

    ivec2 texpos = ivec2(ipos.x % 128, ipos.y % 128);
    vec2 uv = vec2(ipos.x % 128, ipos.y % 128) / 128.0;

//    float value = texelFetch(u_height, ivec3(texpos, region_i), 0).r;
    vec4 value4 = textureGather(u_height, vec3(uv, region_i), 0); // PCF, breaks at boundaries, but whatever
//    float value01 = texelFetch(u_height, ivec3(texpos + ivec2(0, 1), region_i), 0).r;
//    float value10 = texelFetch(u_height, ivec3(texpos + ivec2(1, 0), region_i), 0).r;
//    float value11 = texelFetch(u_height, ivec3(texpos + ivec2(1, 1), region_i), 0).r;

//    value00 = (value00 == 0.0 ? 0.0 : 1.0);
//    value01 = (value01 == 0.0 ? 0.0 : 1.0);
//    value10 = (value10 == 0.0 ? 0.0 : 1.0);
//    value11 = (value11 == 0.0 ? 0.0 : 1.0);
//    float value = (value00 + value01 + value10 + value11) / 4.0;
    value4.x = value4.x == 0.0 ? 0.0 : 1.0;
    value4.y = value4.y == 0.0 ? 0.0 : 1.0;
    value4.z = value4.z == 0.0 ? 0.0 : 1.0;
    value4.w = value4.w == 0.0 ? 0.0 : 1.0;
    float value = dot(value4, vec4(0.25));
//    value = 1.0;

    // debug grid lines
//    if(texpos.x > 120 || texpos.y > 120) value = 1.0;

    out_color = vec3(0.0);
    out_one = 0.0;

//    vec2 center = vec2(0.5 * (REGIONS * 128.0 * u_spacing));
    vec2 center = u_circle.xy;
    float circle_fade = smoothstep(u_circle.z * (15.0 / 16.0), u_circle.z + (2.0 / 16.0), length(in_world - center));

    int x = int(gl_FragCoord.x) % 4;
    int y = int(gl_FragCoord.y) % 4;
    int index = x + y * 4;

    // FIXME: kind of distracting, but better then not having it?
    if(circle_fade <= dither_matrix[index]) {
        discard;
    }
    value *= circle_fade;

    if(value <= 0.0) discard;

    float depth = texelFetch(u_depth_texture, ivec2(gl_FragCoord.xy), 0).r;
    if(min(gl_FragCoord.z, 1.0) > depth) discard;

    out_color.r = value;
    out_color.gb = vec2(1.0);
    out_one = 2.0 / 255.0;
}