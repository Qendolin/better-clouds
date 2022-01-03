#version 150 core

flat in vec4 pass_cloudColor;

out vec4 fragColor;

void main() {
    fragColor = pass_cloudColor;
}
