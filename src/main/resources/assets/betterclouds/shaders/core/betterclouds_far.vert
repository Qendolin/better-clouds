#version 440 core

#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shader_draw_parameters : require


#define REGIONS _REGIONS_

uniform mat4 u_mvp_matrix;
uniform float u_spacing;

layout(std430, binding = 0) buffer RegionOffsets
{
    ivec2 ssbo_region_offsets[REGIONS * REGIONS];
    int ssbo_region_map[REGIONS * REGIONS];
};

layout(location = 0) out vec2 out_pos;

vec3 vertices[4] = vec3[] (
    vec3(0.0, 0.0, 0.0),
    vec3(1.0, 0.0, 0.0),
    vec3(0.0, 0.0, 1.0),
    vec3(1.0, 0.0, 1.0)
);

void main() {
    vec3 v = vertices[gl_VertexID] * REGIONS;
    float size = 128.0 * u_spacing;
    // FIXME: this is wrong
    int region_0 = ssbo_region_map[0];
    vec3 w = vec3(ssbo_region_offsets[region_0].x, 0.0, ssbo_region_offsets[region_0].y);
    w += v;
    w *= size;

    // basically texcoords
    out_pos = v.xz * size;

    gl_Position = u_mvp_matrix * vec4(w, 1.0);
}