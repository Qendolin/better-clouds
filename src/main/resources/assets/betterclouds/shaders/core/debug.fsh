#version 330 core

in vec4 pass_color;

uniform vec4 u_color_modulator;

out vec4 out_color;

void main() {
    vec4 color = pass_color;
    if (color.a == 0.0) {
        discard;
    }
    out_color = color * u_color_modulator;
}
