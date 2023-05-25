#version 460

#define REMAP_DEPTH _REMAP_DEPTH_

uniform sampler2D u_depth;
#if REMAP_DEPTH
uniform vec4 u_depthCoeffs;
#endif

layout (depth_any) out float gl_FragDepth;

// NDC
// [0, 1] ↦ [-1, 1]
//
// ⎛2 -1⎞
// ⎝0  1⎠
//
// NDC'
// [-1, 1] ↦ [0, 1]
//
// ⎛½ ½⎞
// ⎝0 1⎠
//
// Perspective
// [n, f] ↦ [-1, 1]
//
// ⎛2fn/(f−n) (f+n)/(f−n)⎞
// ⎝0         1          ⎠
//
// Perspective'
// [-1, 1] ↦ [n, f]
//
// ⎛(f-n)/2fn -(f+n)/2fn⎞
// ⎝0         1         ⎠
//
//
// hyp   ... hyperbolic depth in [-1, 1] (NDC)
// hypₙ ... hyperbolic depth in [0, 1] (normalized)
// lin   ... linear depth in [near, far]
// P     ... perspective projection matrix from lin to hyp
// NDC   ... NDC matrix (x * 2 - 1) from normalized to NDC
//
// hyp = lin * P
// hypₙ = lin * NDC' * P
// lin = hyp * P'
// lin = hypₙ * P' * NDC
//
//
//
// NDC' * Perspective:
// [n, f] ↦ [0, 1]
//
// ⎛fn/(f-n) f/(f-n)⎞
// ⎝0        1      ⎠
//
//
// Perspective' * NDC
// [0, 1] ↦ [n, f]
//
// ⎛(f-n)/fn -(f+fn+n)/fn⎞
// ⎝0        1           ⎠

float linearize_depth(float hyp, float a, float b)
{
    return (hyp * 2 - 1) * a + b;
}

float hyperbolize_depth(float lin, float a, float b)
{
    return (lin * a + b) * 0.5 + 0.5;
}

float remap_depth(float d, float x, float y, float z, float w)
{
    return d*x*z - 0.5*x*z + 0.5*y*z + 0.5*w + 0.5;
}

void main() {
    float depth = texelFetch(u_depth, ivec2(gl_FragCoord.xy), 0).r;
#if REMAP_DEPTH
//    gl_FragDepth = hyperbolize_depth(linearize_depth(depth, u_depthCoeffs.x, u_depthCoeffs.y), u_depthCoeffs.z, u_depthCoeffs.w);
    gl_FragDepth = remap_depth(depth, u_depthCoeffs.x, u_depthCoeffs.y, u_depthCoeffs.z, u_depthCoeffs.w);
#else
    gl_FragDepth = depth;
#endif
}