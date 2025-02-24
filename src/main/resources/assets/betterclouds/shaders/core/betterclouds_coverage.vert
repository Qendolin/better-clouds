#version 330 core

#extension GL_ARB_separate_shader_objects : enable

// tri-strip
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

// tri-strip, degen
//vec3 vertex_positions_degen[15] = vec3[](
//vec3(-.5f, -.5f, -.5f),
//vec3(+.5f, -.5f, -.5f),
//vec3(-.5f, -.5f, +.5f),
//vec3(+.5f, -.5f, +.5f),
//vec3(+.5f, +.5f, +.5f),
//vec3(+.5f, -.5f, -.5f),
//vec3(+.5f, +.5f, -.5f),
//vec3(-.5f, -.5f, -.5f),
//vec3(-.5f, +.5f, -.5f),
//vec3(-.5f, -.5f, +.5f),
//vec3(-.5f, +.5f, +.5f),
//vec3(+.5f, +.5f, +.5f),
//vec3(-.5f, +.5f, -.5f),
//vec3(+.5f, +.5f, -.5f),
//vec3(+.5f, +.5f, -.5f) // degen triangle
//);

// triangles
//vec3 vertex_positions[8] = vec3[](
//vec3(-.5f, -.5f, -.5f),
//vec3(+.5f, -.5f, -.5f),
//vec3(+.5f, +.5f, -.5f),
//vec3(-.5f, +.5f, -.5f),
//vec3(-.5f, +.5f, +.5f),
//vec3(+.5f, +.5f, +.5f),
//vec3(+.5f, -.5f, +.5f),
//vec3(-.5f, -.5f, +.5f)
//);

// indices
//int[] triangles = int[36](
//0, 2, 1, //face front
//0, 3, 2,
//2, 3, 4, //face top
//2, 4, 5,
//1, 2, 5, //face right
//1, 5, 6,
//0, 7, 4, //face left
//0, 4, 3,
//5, 4, 7, //face back
//5, 7, 6,
//0, 6, 7, //face bottom
//0, 1, 6
//);

#define SIZE vec3(_SIZE_XZ_, _SIZE_Y_, _SIZE_XZ_)

uniform mat4 u_mvp_matrix;

#ifdef SUBINSTANCING
layout(location = 0) in vec4 in_pos[4]; // instanced per cloud cube

void main() {
    const int vertex_count = 15;
    int sub_instance = gl_VertexID / vertex_count;
//    vec3 v = vertex_positions[triangles[gl_VertexID % vertex_count]];
    vec3 v = vertex_positions_degen[gl_VertexID % vertex_count];
    vec3 p = in_pos[sub_instance].xyz;
    vec3 vertexPos = SIZE * v + p;
    gl_Position = u_mvp_matrix * vec4(vertexPos, 1.0);
}
#else
layout(location = 0) in vec4 in_pos; // instanced per cloud cube

void main() {
    vec3 v = vertex_positions[gl_VertexID];
    vec3 p = in_pos.xyz;
    vec3 vertexPos = SIZE * v + p;
    gl_Position = u_mvp_matrix * vec4(vertexPos, 1.0);
}
#endif