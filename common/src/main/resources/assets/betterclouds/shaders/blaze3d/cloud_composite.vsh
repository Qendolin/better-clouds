#version 330 core
#extension GL_ARB_separate_shader_objects : require

void main() {
    // Fullscreen triangle; no vertex buffer is needed.
    vec2 p = vec2(float((gl_VertexIndex << 1) & 2), float(gl_VertexIndex & 2));
    gl_Position = vec4(p * 2.0 - 1.0, 0.0, 1.0);
}
