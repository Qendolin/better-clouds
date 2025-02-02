#version 330 core

#extension GL_ARB_separate_shader_objects : enable

vec3 vertex_positions[14] = vec3[](
vec3(-.5f, -.5f, -.5f),
vec3(+.5f, -.5f, -.5f),
vec3(-.5f, -.5f, +.5f),
vec3(+.5f, -.5f, +.5f),
vec3(+.5f, +.5f, +.5f),
vec3(+.5f, -.5f, -.5f),
vec3(+.5f, +.5f, -.5f),
vec3(-.5f, -.5f, -.5f),
vec3(-.5f, +.5f, -.5f),
vec3(-.5f, -.5f, +.5f),
vec3(-.5f, +.5f, +.5f),
vec3(+.5f, +.5f, +.5f),
vec3(-.5f, +.5f, -.5f),
vec3(+.5f, +.5f, -.5f)
);

// Geometry attributes
#define SIZE vec3(_SIZE_XZ_, _SIZE_Y_, _SIZE_XZ_)
#define NEAR_VISIBILITY_START 10.0 + _SIZE_XZ_
#define NEAR_VISIBILITY_END 20.0 + _SIZE_XZ_
#define FAR_VISIBILITY_EDGE _VISIBILITY_EDGE_

#define POSITIONAL_COLORING _POSITIONAL_COLORING_
#define WORLD_CURVATURE _WORLD_CURVATURE_

#define DISTANT_HORIZONS _DISTANT_HORIZONS_

layout(location = 0) in vec3 in_pos; // instanced per cloud cube

uniform mat4 u_mvp_matrix;

void main() {
    vec3 vertexPos = SIZE * vertex_positions[gl_VertexID] + in_pos;
    gl_Position = u_mvp_matrix * vec4(vertexPos, 1.0);
}
