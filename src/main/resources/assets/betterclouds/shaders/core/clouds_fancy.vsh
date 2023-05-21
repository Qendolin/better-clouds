#version 330

// Geometry attributes
#define SCALE vec3(_SCALE_X_, _SCALE_Y_, _SCALE_X_)
#define NEAR_VISIBILITY_START 10. + SCALE.x/2.
#define NEAR_VISIBILITY_END 20. + SCALE.x/2.
#define FAR_VISIBILITY_EDGE _VISIBILITY_EDGE_

// Shading attributes
#define OVERRIDE_CONE_FACTOR 0.3
#define OVERRIDE_CONE_START 16.
#define OVERRIDE_NORMAL_FACTOR 1.
#define CORONA_ANGLE 2.
#define CORONA_FACTOR 0.3
#define CORONA_COLOR vec3(1.)

layout(location = 0) in vec3 in_pos;
layout(location = 1) in vec3 in_vert;
layout(location = 2) in vec3 in_normal;

flat out vec4 pass_cloudColor;
flat out vec3 pass_cloudPosition;
out vec3 pass_vert;

uniform mat4 u_modelViewProjMat;
uniform vec3 u_cloudsOrigin;
uniform float u_cloudsDistance;
uniform vec4 u_skyColor;
uniform vec4 u_skyData;
uniform vec4 u_skyColorOverride;
uniform sampler2D u_cloudNoise;
uniform vec4 u_cloudsBox;
uniform float u_time;
uniform float u_gradientPos;
uniform sampler2D u_gradient;

float balance(float value, float range) {
    return (value - (1. - range)) / range;
}

void main() {
    vec3 cloudPosition = in_pos - u_cloudsOrigin;
    pass_cloudPosition = cloudPosition;
    pass_vert = SCALE * in_vert + in_pos - u_cloudsOrigin;

    float opacity = smoothstep(NEAR_VISIBILITY_START, NEAR_VISIBILITY_END, length(cloudPosition));
    opacity *= smoothstep(u_cloudsDistance, u_cloudsDistance-FAR_VISIBILITY_EDGE,
            length(vec3(cloudPosition.x, 0., cloudPosition.z)));
    opacity *= u_skyColor.a;

    vec2 cloudDirection = normalize(cloudPosition.xz);
    float fSunCone = dot(u_skyData.xz, cloudDirection);
    fSunCone -= smoothstep(0., 1., OVERRIDE_CONE_START / length(cloudPosition));
    fSunCone = max(0.05, balance(fSunCone, OVERRIDE_CONE_FACTOR));

    //TODO: remove
    fSunCone = 0;

    vec3 celestialLookV = vec3(u_skyData.xy, 0);
    vec3 cloudLookV = normalize(cloudPosition);
    float fCorona = max(0.0, -dot(celestialLookV, cloudLookV));
    fCorona = max(0., balance(fCorona, CORONA_ANGLE/90.));
    fCorona = fCorona * fCorona * CORONA_FACTOR;
    fCorona *= u_skyData.a;

    vec3 cloudColor = mix(u_skyColor.rgb, CORONA_COLOR, fCorona);
    float fNormal = dot(in_normal, normalize(vec3(u_skyData.x, -1.4, 0.)));
    fNormal = max(0.5, balance(fNormal, OVERRIDE_NORMAL_FACTOR));

    float fColorOverride = fSunCone * fNormal * u_skyColorOverride.a * u_skyData.a;
    cloudColor = mix(cloudColor, u_skyColorOverride.rgb, fColorOverride);

    // TODO:
    vec2 gradTexel = vec2(1.0) / vec2(textureSize(u_gradient, 0));
    vec3 cloudDirection3 = normalize(cloudPosition);
    float gradPos = dot(cloudDirection3, celestialLookV);
    // (1, 0) to (0.5, 1)
    if(gradPos > 0.5) gradPos = (-2 * gradPos + 2) * 0.375;
    // (0.5, 0) to (-0.5, 1)
    else if(gradPos > -0.5) gradPos = 0.375 + (-1 * gradPos + 0.5) * 0.25;
    // (-0.5, 0) to (-1, 1)
    else gradPos = 0.625 + (-2 * gradPos - 1) * 0.375;
    cloudColor.rgb = texture(u_gradient, vec2(gradPos, u_skyData.z) * (1.0-gradTexel) + 0.5*gradTexel).rgb;

    // TODO: wind speed
    float waveScale = texture(u_cloudNoise, (cloudPosition.xz + u_cloudsBox.xy) / 4000.0 + vec2(u_time / 800f)).x;
    float smallWaves = texture(u_cloudNoise, (cloudPosition.zx + u_cloudsBox.yx) / 1000.0 + vec2(u_time / 200f)).x * 1.8 - 0.9;
    waveScale = mix(mix(waveScale, 1.0, max(smallWaves, 0.0)), 0.0, max(-smallWaves, 0.0));
//    waveScale = (waveScale - 0.25) * 2.;
    float fDynScale = 1 - smoothstep(0., u_cloudsBox.w / 4f, in_pos.y);
    float dynScale = mix(1.0, waveScale, fDynScale);

    // FIXME: This code is duplicated in the fsh
    float height = smoothstep(-u_cloudsBox.w/8., u_cloudsBox.w/2., cloudPosition.y+u_cloudsBox.z);
    vec3 gradientColor = mix(vec3(0.8, 0.8, 0.85), vec3(1), (1.0-pow(1.0-height, 2.0)));
    cloudColor *= mix(gradientColor, vec3(1.0), 1-dynScale);
    pass_cloudColor = vec4(cloudColor, opacity);

    vec3 scale = SCALE * dynScale;
    gl_Position = u_modelViewProjMat * vec4(scale * in_vert + in_pos, 1.0);
}
