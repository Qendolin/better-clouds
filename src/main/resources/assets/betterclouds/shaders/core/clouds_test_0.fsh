#version 460

uniform sampler2D u_depth;
uniform vec4 u_clipPlanes;

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

//float linearize_depth(float d, float n, float f)
//{
//    float ndc = d * 2.0 - 1.0;
//    return (2.0 * n * f) / (f + n - ndc * (f - n));
//}

//float linearize_depth(float hyp, float n, float f)
//{
//    return dot(vec2(hyp * 2 - 1, 1), vec2((f-n)/f, -(f+n)/f));
//}

//float linearize_depth(float hyp, float n, float f)
//{
//    return dot(vec2(hyp * 2 - 1, 1), vec2((f-n)/(2*f*n), -(f+n)/(2*f*n)));
//}

float linearize_depth(float hyp, float a, float b)
{
    return (hyp * 2 - 1) * a + b;
}

//float hyperbolize_depth(float d, float n, float f)
//{
//    return ((n - d) * f) / ((n - f) * d);
//}

//float hyperbolize_depth(float lin, float n, float f)
//{
//    return dot(vec2(lin, 1), vec2((2*f*n)/(f-n), (f+n)/(f-n))) * 0.5 + 0.5;
//}

//float hyperbolize_depth(float lin, float n, float f)
//{
//    return dot(vec2(lin, 1), vec2((2*f*n)/(f-n), (f+n)/(f-n))) * 0.5 + 0.5;
//}

float hyperbolize_depth(float lin, float a, float b)
{
    return (lin * a + b) * 0.5 + 0.5;
}

float remap_depth(float d, float n1, float f1, float n2, float f2)
{
    return ((n2*f1 - n2*f1*d + n2*n1*d - n1*f1) * f2) / ((n2 - f2) * n1 * f1);
}


void main() {
    float depth = texelFetch(u_depth, ivec2(gl_FragCoord.xy), 0).r;
//    if(depth == 1.0 || depth == 0.0) {
//        gl_FragDepth = depth;
//        return;
//    }
    gl_FragDepth = hyperbolize_depth(linearize_depth(depth, u_clipPlanes.x, u_clipPlanes.y), u_clipPlanes.z, u_clipPlanes.w);
//    gl_FragDepth = remap_depth(depth, u_clipPlanes.x, u_clipPlanes.y, u_clipPlanes.z, u_clipPlanes.w);
}