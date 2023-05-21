#version 440

#define DITHER_FUNC (pixel.y)%2, (pixel.x)%2

in vec2 pass_uv;

// shader outputs
layout (location = 0) out vec4 fragColor;

// This is for Iris compatability
layout (location=1) out vec4 gBuffer1;
layout (location=2) out vec4 gBuffer2;
layout (location=3) out vec4 gBuffer3;
layout (location=4) out vec4 gBuffer4;

// color accumulation buffer
uniform sampler2D u_accum;

// revealage threshold buffer
uniform sampler2D u_reveal;

uniform vec3 u_gamma;

const float EPSILON = 0.00001f;

// TODO: do I need this?
// calculate floating point numbers equality accurately
bool isApproximatelyEqual(float a, float b)
{
    return abs(a - b) <= (abs(a) < abs(b) ? abs(b) : abs(a)) * EPSILON;
}

// get the max value between three values
float max3(vec3 v)
{
    return max(max(v.x, v.y), v.z);
}

void main()
{
    ivec2 texel = ivec2(gl_FragCoord.xy);

    // fragment revealage
    float revealage = texelFetch(u_reveal, texel, 0).r;

    // save the blending and color texture fetch cost if there is not a transparent fragment
    if (revealage > (1.0 - EPSILON))
        discard;

    // fragment color
    vec4 accumulation = texelFetch(u_accum, texel, 0);

    // TODO: do I need this?
    // suppress overflow
    if (isinf(max3(abs(accumulation.rgb))))
        accumulation.rgb = vec3(accumulation.a);

    // prevent floating point precision bug
    vec3 average_color = accumulation.rgb / max(accumulation.a, EPSILON);

    // blend pixels
    fragColor = vec4(average_color, 1.0 - revealage);

    fragColor.rgb *= u_gamma.x;
    fragColor.a *= u_gamma.z;
    if(u_gamma.y < 0) {
        fragColor.rgb = pow(fragColor.rgb, vec3(-u_gamma.y));
    } else {
        fragColor.rgb = pow(fragColor.rgb, vec3(1./u_gamma.y));
    }

    gBuffer1 = vec4(0.);
    gBuffer2 = vec4(0.);
    gBuffer3 = vec4(0.);
    gBuffer4 = vec4(0.);
}