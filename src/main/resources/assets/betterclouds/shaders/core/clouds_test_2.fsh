#version 460

layout(early_fragment_tests) in;

in vec3 pass_dir;
in vec2 pass_ndc;

out vec4 frag_color;

uniform sampler2D u_color;
uniform usampler2D u_accum;
uniform usampler2D u_depth;
uniform sampler2D u_lightTexture;
uniform sampler2D u_noise2Texture;
uniform vec4 u_skyData;
uniform vec4 u_effectColor;
uniform vec3 u_gamma;
uniform mat4 u_inverseMat;

const float pi = 3.14159265359;
const float e = 2.71828182846;

void main() {
    vec3 cloudData = texelFetch(u_color, ivec2(gl_FragCoord), 0).rgb;
    if(cloudData == vec3(0.0)) return;
//    frag_color.rgb = cloudColor;

    float coverage = float(texelFetch(u_accum, ivec2(gl_FragCoord), 0).r);
    // This is the "correct" formula
//    frag_color.a = 1. - pow((1.-u_effectColor.a), coverage);
    frag_color.a = pow(coverage, 1.5) / (1./(u_effectColor.a)+pow(coverage, 1.5)-1);
//    frag_color.a = 1 + (u_effectColor.a - 1) / (pow(coverage, 0.5));

    vec3 sunDir = vec3(u_skyData.xy, 0);
    vec3 fragDir = normalize(pass_dir);
    vec3 xzDir = normalize(vec3(pass_dir.x, 0, pass_dir.z));

    vec3 xzProj = fragDir - sunDir * dot(fragDir, sunDir);
//    float projAngle = acos(dot(xzProj, cross(sunDir, vec3(0, 0, 1))));
    float projAngle = acos(dot(normalize(xzProj), vec3(0, 0, 1)));

//    float dotXY = dot(normalize(vec3(pass_dir.xy, 0)), sunDir);
    float dotXY = dot(normalize(pass_dir.xy), normalize(sunDir.xy));
//    float dotYZ = dot(normalize(vec3(0, pass_dir.yz)), sunDir);
    float dotYZ = dot(normalize(pass_dir.yz), normalize(sunDir.yz));
    float dotX = dot(fragDir, vec3(1, 0, 0));
    float dotZ = dot(fragDir, vec3(0, 0, 1));

    float cotx = tan(fragDir.x / fragDir.y);
    float cotz = tan(fragDir.z / fragDir.y);

//    float cox = abs(dotXY) - abs(dot(normalize(vec3(pass_dir.xy, 0)), vec3(1,0,0)));
//    float coz = abs(dotYZ) - abs(dot(normalize(vec3(0, pass_dir.yz)), vec3(0,0,1)));

//    float lightUVx = dot(normalize(pass_dir), sunDir);
//    float lightUVx = (fragDir.y * sunDir.y) + max(abs(fragDir.x - sunDir.x), abs(fragDir.z - sunDir.z));
    // if sunDir.z is always 0, this can be optimized
    float sphere = dot(sunDir, normalize(pass_dir));
//    float square = max(dot(normalize(vec3(pass_dir.xy, 0)), sunDir), dot(normalize(vec3(0, pass_dir.yz)), sunDir));
//    float square = abs(pass_dir.x) > abs(pass_dir.z) ? dotXY : dotYZ;
    float square = abs(pass_dir.x) > abs(pass_dir.z) ? dotXY : dotYZ;
    float rsquare = (1-length(max(abs(vec2(1-abs(dotXY), 1-abs(dotYZ))), vec2(0)))) * sign(sunDir.y * fragDir.y);
//    float superellipse = pow(pow(abs(dot(normalize(vec3(pass_dir.xy, 0)), sunDir)), 10) + pow(abs(dot(normalize(vec3(0, pass_dir.yz)), sunDir)), 10), 1./10.);
//    float superellipse = pow(pow(abs(dotXY), 4) + pow(abs(dotYZ), 4), 1./4.) * sign(sunDir.y * fragDir.y);
//    float superellipse = pow(pow(abs(fragDir.x), 8) + pow(abs(fragDir.z), 8), 1./8.) * sign(sunDir.y * fragDir.y);
//    float superellipse = pow(pow(abs(cotx), 4) + pow(abs(cotz), 4), 1./4.) * sign(sunDir.y * fragDir.y);
//    float superellipse = (1.0 + (1./3.) * (pow(sin(2*projAngle + pi/2.), 2.0))) * (1.-(sunDir.y * -fragDir.y)) - 1.0;
//    float superellipse = (1.0 + (1./3.) * (pow(sin(2*projAngle + pi/2.), 2.0))) * (1.-dot(sunDir, -fragDir)) - 1.0;
    float superellipse = ((1.0 + (1./3.) * (pow(sin(2*projAngle + pi/2.), 2.0))) * (1.-abs(dot(sunDir, fragDir))) - 1.0) * sign(dot(sunDir, -fragDir));
//    float superellipse = -((1.0 + (1./8.) * (pow(sin(2*projAngle), 2.0))) * length(xzProj) - 1.0) * sign(sunDir.y * fragDir.y);
//    float superellipse = 1./(pow(1-0.5*pow(sin(2*projAngle), 2), 1./4.)) - length(xzProj);
//    float awdad = 0.5;
//    superellipse = smoothstep(awdad-0.01, awdad, superellipse) * smoothstep(awdad+0.01, awdad, superellipse);
    float lightUVx = mix(sphere, superellipse, smoothstep(0.75, 1.0, abs(sphere)));
//    float lightUVx = -abs(pow((dotXY+dotYZ)/(2*max(dotXY,dotYZ)), 4.));
//    float lightUVx = pow(abs(1-pow(fragDir.y * sunDir.y, 4)), 1./4.);
//    float lightUVx = length(max(abs(vec2(1-dotXY, 1-dotYZ)) - vec2(0.0), vec2(0)));
//    float lightUVx = superellipse;
//    frag_color.a = 1;
    frag_color.rgb = vec3(-lightUVx);
//    if(-lightUVx > 1.0) frag_color.rgb = vec3(0.);
//    frag_color.rgb = vec3(abs(dot(normalize(vec3(pass_dir.xy, 0)), vec3(1,0,0))) - abs(dotXY), abs(dot(normalize(vec3(0, pass_dir.yz)), vec3(0,0,1))) - abs(dotYZ), 0.) * 0.5 + 0.5;
//    frag_color.rgb = vec3(superellipse);
//    return;

    //    (2*x-1)^3/2  + 0.5
//    lightUVx = mix(lightUVx, pow(2.*lightUVx - 1, 3.) / 2 + 0.5, pow(coverage/100., 5.));

    // (1, 0) to (0.5, 1)
    if(lightUVx > 0.5) lightUVx = (-2 * lightUVx + 2) * 0.375;
    // (0.5, 0) to (-0.5, 1)
    else if(lightUVx > -0.5) lightUVx = 0.375 + (-1 * lightUVx + 0.5) * 0.25;
    // (-0.5, 0) to (-1, 1)
    else lightUVx = 0.625 + (-2 * lightUVx - 1) * 0.375;

    vec2 lightUV = vec2(lightUVx, u_skyData.z);

    // Center texels
    lightUV -= (lightUV - 0.5) / textureSize(u_lightTexture, 0);
    frag_color.rgb = texture(u_lightTexture, lightUV).rgb;

//    frag_color.rgb *= mix(cloudColor.rgb, vec3(1.0), frag_color.a);
//    frag_color.rgb *= mix(mix(0.5, 1.0, pow(cloudColor.r, 0.5)), 1., 1.-pow(0.5, (coverage-1.)/50.));
//    frag_color.rgb *= mix(mix(0.5, 1.0, pow(cloudColor.g, 0.5)), 1., 1.-pow(0.5, (coverage-1.)/50.));
//    frag_color.rgb *= 0.85+(0.15/pow(2., coverage/10.));


//    float depth = texelFetch(u_depth, ivec2(gl_FragCoord), 0).b;
//    float depth = cloudColor.b;
//    vec4 homogenousPos = u_inverseMat * vec4(pass_ndc, depth, 1.0) * 2. - 1.;
//    homogenousPos /= homogenousPos.w;
//
//    float colorNoise = texture(u_noise2Texture, homogenousPos.xz / 512.).r;
//    frag_color.rgb = mix(frag_color.rgb, vec3(146,182,240) / 255., pow(colorNoise, 5.));

    float colorLumi = dot(frag_color.rgb, vec3(0.2126, 0.7152, 0.072)) + 0.001;
    vec3 colorChroma = frag_color.rgb / colorLumi;

    vec3 dataWeights = vec3(1, 3, 3);
//    vec3 dataOffsets = vec3(0, , 2);
//    float colorVariance = pow(1. - length(cloudData.rgb * dataWeights.rgb) / length(dataWeights), 6.);
    float colorVariance = length(vec3(1., 1. - pow(1. - cloudData.g, 3.) * 0.75, cloudData.b * 0.75 + 0.25) * dataWeights.rgb) / length(dataWeights);
//    float colorVariance = 1. - pow(1. - length(cloudData.rgb * dataWeights.rgb) / length(dataWeights), 3.);
//    frag_color.rgb = mix(frag_color.rgb, vec3(130, 145, 184) / 255., colorVariance);
//    frag_color.rgb *= mix(vec3(1.0), vec3(130, 145, 184) / 255., min(colorVariance*2., 1.0)) + mix(vec3(1.0), vec3(255, 270, 309) / 255., max(colorVariance*2.-1., 0.0));
//    frag_color.rgb = vec3(length(cloudColor.rgb));
//    colorLumi *= colorVariance * 0.3 + 0.75;
    colorLumi = colorVariance * 0.35 * (0.3 + 0.7 * colorLumi) + 0.75 * colorLumi;
//    colorLumi = colorVariance * 0.3 + 0.75 * colorLumi;
//    float skyLumi = dot(pow(u_effectColor.rgb, vec3(u_gamma.y)), vec3(0.2126, 0.7152, 0.072));
//    float skyLumi = pow(u_effectColor.r, u_gamma.y);
    colorLumi *= u_effectColor.r;
    colorChroma = mix(vec3(1.0), colorChroma, u_effectColor.r);

    frag_color.rgb = colorChroma * colorLumi;
//    float skyColorError = length(frag_color-u_effectColor)/length(vec3(1.));
//    frag_color.rgb *= u_effectColor.rgb;
//    frag_color.rgb = mix(frag_color.rgb, u_effectColor.rgb, skyColorError);
//    frag_color.a = mix(frag_color.a, 1, skyColorError);

    frag_color.rgb *= u_gamma.x;
    frag_color.a *= u_gamma.z;
    if(u_gamma.y < 0) {
        frag_color.rgb = pow(frag_color.rgb, vec3(-u_gamma.y));
    } else {
        frag_color.rgb = pow(frag_color.rgb, vec3(1./u_gamma.y));
    }

//    frag_color.rgb = vec3(colorVariance)
//    frag_color.rgb = vec3(cloudColor.b);
//    frag_color.a = 1.0;

//    const float s = 0.2;
//    frag_color.rgb = mix(frag_color.rgb, cloudColor.rgb, smoothstep(0.0, s, lightUVx) * (1. - smoothstep(1.0-s, 1.0, lightUVx)));
//    frag_color.rgb = cloudColor.rgb;

//    frag_color.rgb = vec3(lightUV.x);

//    frag_color.rgb /= 2.0;
}