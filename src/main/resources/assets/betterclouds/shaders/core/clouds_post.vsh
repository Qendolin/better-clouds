#version 330

layout(location = 0) in vec2 in_vert;

out vec2 pass_uv;

void main() {
    gl_Position = vec4(in_vert, 0.0, 1.0);
    pass_uv = in_vert * 0.5 + 0.5;
}
