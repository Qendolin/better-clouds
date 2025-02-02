#version 330 core

#extension GL_ARB_separate_shader_objects : enable

#define DISTANT_HORIZONS _DISTANT_HORIZONS_

const float dither_matrix[16] = float[](
    0.0, 0.5, 0.125, 0.625,
    0.75, 0.25, 0.875, 0.375,
    0.0625, 0.5625, 0.03125, 0.53125,
    0.8125, 0.4375, 0.78125, 0.40625
);


flat in float pass_opacity;
in vec3 pass_color;

layout (location=0) out vec3 out_color;
layout (location=1) out float out_one;

uniform sampler2D u_depth_texture;

#if DISTANT_HORIZONS
uniform sampler2D u_dh_depth_texture;
in float pass_dh_depth;
#endif

void main() {
    // initialize out variables
    out_color = vec3(0.0);
    out_one = 0.0;

    float depth = texelFetch(u_depth_texture, ivec2(gl_FragCoord.xy), 0).r;
    if(min(gl_FragCoord.z, 1.0) > depth) discard;

#if DISTANT_HORIZONS
    // pass_dh_depth is always 0 if the depth texture cloud not be set.
    // This is a "safety" check to prevent reading from an unbound texture
    if(pass_dh_depth != 0) {
        depth = texelFetch(u_dh_depth_texture, ivec2(gl_FragCoord.xy), 0).r;
        if(pass_dh_depth > depth) discard;
    }
#endif

    int x = int(gl_FragCoord.x) % 4;
    int y = int(gl_FragCoord.y) % 4;
    int index = x + y * 4;

    if(pass_opacity <= dither_matrix[index]) {
        // TODO: Maybe there is a way to achieve the same without using discard, to enable the early z test
        // Just 'return' doesn't work because the color is still written, just as (0,0,0) and beaks near visibility
        discard;
    }

    out_color.r = 1.0;
    out_color.gb = pass_color.gb;
    out_one = 1.0 / 255.0;
}