#version 330 core

flat in vec4 pass_cloudColor;

layout (location=0) out vec4 fragColor;

// This is for Iris compatability
layout (location=1) out vec4 gBuffer1;
layout (location=2) out vec4 gBuffer2;
layout (location=3) out vec4 gBuffer3;
layout (location=4) out vec4 gBuffer4;


void main() {
    fragColor = pass_cloudColor;
    fragColor.rgb *= 1.0;
    fragColor.a *= 1.0;
    gBuffer1 = vec4(0.);
    gBuffer2 = vec4(0.);
    gBuffer3 = vec4(0.);
    gBuffer4 = vec4(0.);
}
