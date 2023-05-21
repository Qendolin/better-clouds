#version 330

// Geometry attributes
#define SCALE vec3(_SCALE_X_, _SCALE_Y_, _SCALE_X_)
#define NEAR_VISIBILITY_START 10. + SCALE.x/2.
#define NEAR_VISIBILITY_END 20. + SCALE.x/2.
#define FAR_VISIBILITY_EDGE _VISIBILITY_EDGE_

// Shading attributes
#define OVERRIDE_CONE_FACTOR 0.3
#define OVERRIDE_CONE_START 16.

layout(location = 0) in vec3 in_pos;
layout(location = 1) in vec3 in_vert;

flat out vec4 pass_cloudColor;

uniform mat4 u_modelViewProjMat;
uniform vec3 u_cloudsOrigin;
uniform vec4 u_skyColor;
uniform vec4 u_skyColorOverride;
uniform vec4 u_skyData;

float balance(float value, float range) {
    return (value - (1. - range)) / range;
}

void main() {
    vec3 cloudPosition = in_pos - u_cloudsOrigin;
    vec2 cloudDirection = normalize(cloudPosition.xz);
    float fSunCone = dot(u_skyData.xz, cloudDirection);
    fSunCone -= smoothstep(0., 1., OVERRIDE_CONE_START / length(cloudPosition));
    fSunCone = max(0.05, balance(fSunCone, OVERRIDE_CONE_FACTOR));

    float fColorOverride = fSunCone * u_skyColorOverride.a * u_skyData.a;

    pass_cloudColor.rgb = mix(u_skyColor.rgb, u_skyColorOverride.rgb, fColorOverride);
    // flat clouds require a higher alpha because there are half as many faces?
    pass_cloudColor.a = u_skyColor.a*1.5;

    gl_Position = u_modelViewProjMat * vec4(SCALE * in_vert + in_pos, 1.0);
}
