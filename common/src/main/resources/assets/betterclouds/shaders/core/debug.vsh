#version 330 core

layout(location = 0) in vec3 in_position;
layout(location = 1) in vec4 in_color;

uniform mat4 u_model_view_matrix;
uniform mat4 u_projection_matrix;

out vec4 pass_color;

void main() {
    gl_Position = u_projection_matrix * u_model_view_matrix * vec4(in_position, 1.0);

    pass_color = in_color;
}
