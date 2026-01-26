#version 440 core

#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shader_draw_parameters : require


#define REGIONS _REGIONS_

uniform mat4 u_mvp_matrix;
uniform float u_spacing;
uniform vec3 u_circle;

layout(std430, binding = 0) buffer RegionOffsets
{
    ivec2 ssbo_region_offsets[REGIONS * REGIONS];
    int ssbo_region_map[REGIONS * REGIONS];
};

layout(location = 0) out vec2 out_pos;
layout(location = 1) out vec2 out_world;

// plane with an octagon hole
vec3 vertices[18] = vec3[] (
    vec3(1.0, 0.0, 0.5),
    vec3(0.75, 0.0, 0.5),
    vec3(1.0, 0.0, 0.0),
    vec3(0.6768, 0.0, 0.3232),
    vec3(0.5, 0.0, 0.0),
    vec3(0.5, 0.0, 0.25),
    vec3(0.0, 0.0, 0.0),
    vec3(0.3232, 0.0, 0.3232),
    vec3(0.0, 0.0, 0.5),
    vec3(0.25, 0.0, 0.5),
    vec3(0.0, 0.0, 1.0),
    vec3(0.3232, 0.0, 0.6768),
    vec3(0.5, 0.0, 1.0),
    vec3(0.5, 0.0, 0.75),
    vec3(1.0, 0.0, 1.0),
    vec3(0.6768, 0.0, 0.6768),
    vec3(1.0, 0.0, 0.5),
    vec3(0.75, 0.0, 0.5)
);

void main() {
    vec3 v = (vertices[gl_VertexID] - 0.5) * REGIONS;
    v.y = 0.0;
    float size = 128.0 * u_spacing;
    // FIXME: this is wrong
    int region_0 = ssbo_region_map[0];
//    vec3 w = vec3(ssbo_region_offsets[region_0].x, 0.0, ssbo_region_offsets[region_0].y);
//    vec3 w = vec3(ssbo_region_offsets[1].x, 0.0, ssbo_region_offsets[1].y);
    vec3 w = vec3(0.0);
    w += v;
    w *= size;
    w += vec3(u_circle.x, 0.0, u_circle.y);

    // basically texcoords
    out_pos = v.xz * size;
    out_world = w.xz;

    gl_Position = u_mvp_matrix * vec4(w, 1.0);
}