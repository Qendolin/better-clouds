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

uniform mat4 u_modelViewProjMat;
uniform vec3 u_cloudsPosition;
uniform float u_cloudsDistance;
uniform vec4 u_skyColor;
uniform vec4 u_skyColorOverride;
uniform vec4 u_sunDirection;

float balance(float value, float range) {
    return (value - (1. - range)) / range;
}

void main() {
    vec3 cloudPosition = in_pos - u_cloudsPosition;
    float opacity = smoothstep(NEAR_VISIBILITY_START, NEAR_VISIBILITY_END, length(cloudPosition));
    opacity *= smoothstep(u_cloudsDistance, u_cloudsDistance-FAR_VISIBILITY_EDGE,
            length(vec3(cloudPosition.x, 0., cloudPosition.z)));
    opacity *= u_skyColor.a;

    vec2 cloudDirection = normalize(cloudPosition.xz);
    float fSunCone = dot(u_sunDirection.xz, cloudDirection);
    fSunCone -= smoothstep(0., 1., OVERRIDE_CONE_START / length(cloudPosition));
    fSunCone = max(0.05, balance(fSunCone, OVERRIDE_CONE_FACTOR));

    vec3 celestialLookV = u_sunDirection.xyz;
    vec3 cloudLookV = normalize(cloudPosition);
    float fCorona = abs(dot(celestialLookV, cloudLookV));
    fCorona = max(0., balance(fCorona, CORONA_ANGLE/90.));
    fCorona = fCorona * fCorona * CORONA_FACTOR;
    fCorona *= u_sunDirection.a;

    vec3 cloudColor = mix(u_skyColor.rgb, CORONA_COLOR, fCorona);
    float fNormal = dot(in_normal, normalize(vec3(u_sunDirection.x, -1.4, 0.)));
    fNormal = max(0.5, balance(fNormal, OVERRIDE_NORMAL_FACTOR));

    float fColorOverride = fSunCone * fNormal * u_skyColorOverride.a * u_sunDirection.a;

    cloudColor = mix(cloudColor, u_skyColorOverride.rgb, fColorOverride);
    pass_cloudColor = vec4(cloudColor, opacity);

    gl_Position = u_modelViewProjMat * vec4(SCALE * in_vert + in_pos, 1.0);
}
