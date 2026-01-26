#version 440 core

#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shader_draw_parameters : require
#extension GL_ARB_shading_language_420pack : enable

// tri-strip
vec3 vertex_positions[14] = vec3[](
vec3(-.5f, -.5f, -.5f),
vec3(+.5f, -.5f, -.5f),
vec3(-.5f, -.5f, +.5f),
vec3(+.5f, -.5f, +.5f),
vec3(+.5f, +.5f, +.5f),
vec3(+.5f, -.5f, -.5f),
vec3(+.5f, +.5f, -.5f),
vec3(-.5f, -.5f, -.5f),
vec3(-.5f, +.5f, -.5f),
vec3(-.5f, -.5f, +.5f),
vec3(-.5f, +.5f, +.5f),
vec3(+.5f, +.5f, +.5f),
vec3(-.5f, +.5f, -.5f),
vec3(+.5f, +.5f, -.5f)
);

// tri-fan
vec3 vert_pxpypz[8] = vec3[] (
// center
vec3(+0.5, +0.5, +0.5),
// start
vec3(-0.5, +0.5, +0.5),
// +z
vec3(-0.5, -0.5, +0.5),
vec3(+0.5, -0.5, +0.5),
// +x
vec3(+0.5, -0.5, -0.5),
vec3(+0.5, +0.5, -0.5),
// +y
vec3(-0.5, +0.5, -0.5),
vec3(-0.5, +0.5, +0.5)
);

vec3 vert_nxpypz[8] = vec3[] (
// center
vec3(-0.5, +0.5, +0.5),
// start
vec3(-0.5, +0.5, -0.5),
// -x
vec3(-0.5, -0.5, -0.5),
vec3(-0.5, -0.5, +0.5),
// +z
vec3(+0.5, -0.5, +0.5),
vec3(+0.5, +0.5, +0.5),
// +y
vec3(+0.5, +0.5, -0.5),
vec3(-0.5, +0.5, -0.5)
);

vec3 vert_pxpynz[8] = vec3[] (
// center
vec3(+0.5, +0.5, -0.5),
// start
vec3(+0.5, +0.5, +0.5),
// +x
vec3(+0.5, -0.5, +0.5),
vec3(+0.5, -0.5, -0.5),
// -z
vec3(-0.5, -0.5, -0.5),
vec3(-0.5, +0.5, -0.5),
// +y
vec3(-0.5, +0.5, +0.5),
vec3(+0.5, +0.5, +0.5)
);

vec3 vert_nxpynz[8] = vec3[] (
// center
vec3(-0.5, +0.5, -0.5),
// start
vec3(+0.5, +0.5, -0.5),
// -z
vec3(+0.5, -0.5, -0.5),
vec3(-0.5, -0.5, -0.5),
// -x
vec3(-0.5, -0.5, +0.5),
vec3(-0.5, +0.5, +0.5),
// +y
vec3(+0.5, +0.5, +0.5),
vec3(+0.5, +0.5, -0.5)
);

vec3 vert_pxnypz[8] = vec3[] (
// center
vec3(+0.5, -0.5, +0.5),
// start
vec3(-0.5, -0.5, +0.5),
// -y
vec3(-0.5, -0.5, -0.5),
vec3(+0.5, -0.5, -0.5),
// +x
vec3(+0.5, +0.5, -0.5),
vec3(+0.5, +0.5, +0.5),
// +z
vec3(-0.5, +0.5, +0.5),
vec3(-0.5, -0.5, +0.5)
);

vec3 vert_nxnypz[8] = vec3[] (
// center
vec3(-0.5, -0.5, +0.5),
// start
vec3(-0.5, -0.5, -0.5),
// -y
vec3(+0.5, -0.5, -0.5),
vec3(+0.5, -0.5, +0.5),
// +z
vec3(+0.5, +0.5, +0.5),
vec3(-0.5, +0.5, +0.5),
// -x
vec3(-0.5, +0.5, -0.5),
vec3(-0.5, -0.5, -0.5)
);

vec3 vert_pxnynz[8] = vec3[] (
// center
vec3(+0.5, -0.5, -0.5),
// start
vec3(+0.5, -0.5, +0.5),
// -y
vec3(-0.5, -0.5, +0.5),
vec3(-0.5, -0.5, -0.5),
// -z
vec3(-0.5, +0.5, -0.5),
vec3(+0.5, +0.5, -0.5),
// +x
vec3(+0.5, +0.5, +0.5),
vec3(+0.5, -0.5, +0.5)
);

vec3 vert_nxnynz[8] = vec3[] (
// center
vec3(-0.5, -0.5, -0.5),
// start
vec3(+0.5, -0.5, -0.5),
// -y
vec3(+0.5, -0.5, +0.5),
vec3(-0.5, -0.5, +0.5),
// -x
vec3(-0.5, +0.5, +0.5),
vec3(-0.5, +0.5, -0.5),
// -z
vec3(+0.5, +0.5, -0.5),
vec3(+0.5, -0.5, -0.5)
);

const float random_offsets_xy[] = float[](
285.0, 708.0, 286.0, 132.0, 969.0, 815.0, 616.0, 885.0, 708.0, 53.0, 781.0, 528.0, 651.0, 774.0, 614.0, 466.0, 122.0, 786.0, 1010.0, 829.0, 452.0, 970.0, 558.0, 674.0, 939.0, 863.0, 834.0, 235.0, 816.0, 497.0, 867.0, 69.0, 36.0, 839.0, 857.0, 235.0, 474.0, 262.0, 854.0, 102.0, 179.0, 557.0, 683.0, 895.0, 146.0, 173.0, 905.0, 25.0, 614.0, 188.0, 763.0, 599.0, 762.0, 884.0, 407.0, 998.0, 194.0, 514.0, 774.0, 447.0, 137.0, 834.0, 155.0, 131.0, 846.0, 729.0, 758.0, 922.0, 954.0, 639.0, 728.0, 336.0, 1008.0, 964.0, 557.0, 783.0, 987.0, 619.0, 322.0, 575.0, 935.0, 141.0, 302.0, 717.0, 128.0, 994.0, 302.0, 608.0, 975.0, 946.0, 410.0, 633.0, 885.0, 1018.0, 333.0, 796.0, 377.0, 820.0, 664.0, 873.0, 962.0, 916.0, 930.0, 441.0, 597.0, 561.0, 533.0, 652.0, 857.0, 641.0, 166.0, 938.0, 14.0, 943.0, 67.0, 466.0, 584.0, 1006.0, 155.0, 600.0, 702.0, 21.0, 756.0, 458.0, 855.0, 681.0, 17.0, 689.0, 943.0, 525.0, 116.0, 932.0, 888.0, 882.0, 271.0, 884.0, 16.0, 171.0, 995.0, 222.0, 725.0, 758.0, 469.0, 251.0, 835.0, 175.0, 524.0, 448.0, 514.0, 817.0, 534.0, 126.0, 68.0, 966.0, 769.0, 539.0, 417.0, 925.0, 894.0, 661.0, 817.0, 943.0, 797.0, 158.0, 95.0, 0.0, 922.0, 932.0, 408.0, 250.0, 564.0, 743.0, 364.0, 690.0, 496.0, 547.0, 441.0, 540.0, 9.0, 549.0, 14.0, 833.0, 305.0, 439.0, 902.0, 324.0, 168.0, 525.0, 120.0, 265.0, 252.0, 438.0, 506.0, 979.0, 751.0, 475.0, 169.0, 523.0, 219.0, 645.0, 152.0, 888.0, 501.0, 975.0, 793.0, 931.0, 534.0, 487.0, 582.0, 338.0, 183.0, 123.0, 808.0, 179.0, 147.0, 695.0, 380.0, 89.0, 733.0, 550.0, 160.0, 5.0, 642.0, 428.0, 893.0, 309.0, 418.0, 692.0, 544.0, 299.0, 189.0, 710.0, 634.0, 905.0, 320.0, 814.0, 927.0, 745.0, 23.0, 91.0, 180.0, 592.0, 545.0, 281.0, 137.0, 736.0, 4.0, 518.0, 413.0, 757.0, 688.0, 363.0, 287.0, 308.0, 173.0, 987.0, 842.0, 757.0, 115.0, 307.0, 209.0, 835.0, 42.0, 92.0, 371.0, 900.0, 581.0, 557.0, 20.0, 471.0, 275.0, 641.0, 230.0, 331.0, 65.0, 1018.0, 212.0, 226.0, 779.0, 723.0, 24.0, 435.0, 720.0, 340.0, 621.0, 790.0, 53.0, 462.0, 359.0, 1006.0, 941.0, 744.0, 637.0, 466.0, 506.0, 208.0, 148.0, 690.0, 838.0, 874.0, 993.0, 113.0, 858.0, 764.0, 867.0, 462.0, 196.0, 725.0, 910.0, 432.0, 405.0, 336.0, 495.0, 904.0, 812.0, 40.0, 330.0, 103.0, 598.0, 161.0, 967.0, 817.0, 23.0, 882.0, 236.0, 156.0, 158.0, 262.0, 816.0, 582.0, 600.0, 290.0, 756.0, 821.0, 492.0, 341.0, 729.0, 285.0, 321.0, 353.0, 973.0, 868.0, 741.0, 56.0, 262.0, 254.0, 377.0, 573.0, 806.0, 748.0, 954.0, 969.0, 791.0, 635.0, 696.0, 647.0, 567.0, 860.0, 469.0, 865.0, 557.0, 172.0, 492.0, 419.0, 985.0, 66.0, 813.0, 94.0, 24.0, 632.0, 537.0, 922.0, 831.0, 650.0, 419.0, 467.0, 980.0, 457.0, 388.0, 789.0, 265.0, 920.0, 377.0, 460.0, 853.0, 487.0, 1021.0, 584.0, 837.0, 891.0, 559.0, 736.0, 331.0, 811.0, 23.0, 282.0, 1.0, 507.0, 9.0, 729.0, 63.0, 388.0, 463.0, 758.0, 674.0, 503.0, 722.0, 793.0, 196.0, 808.0, 723.0, 613.0, 263.0, 820.0, 69.0, 649.0, 483.0, 450.0, 395.0, 282.0, 92.0, 75.0, 189.0, 81.0, 799.0, 154.0, 506.0, 350.0, 274.0, 825.0, 845.0, 431.0, 228.0, 133.0, 296.0, 0.0, 69.0, 782.0, 899.0, 714.0, 224.0, 220.0, 662.0, 358.0, 845.0, 360.0, 502.0, 10.0, 509.0, 572.0, 50.0, 196.0, 183.0, 214.0, 632.0, 204.0, 665.0, 858.0, 884.0, 939.0, 272.0, 153.0, 396.0, 927.0, 611.0, 142.0, 915.0, 849.0, 988.0, 997.0, 808.0, 920.0, 563.0, 656.0, 962.0, 379.0, 839.0, 241.0, 156.0, 403.0, 757.0, 408.0, 309.0, 798.0, 214.0, 977.0, 193.0, 1000.0, 917.0, 549.0, 940.0, 723.0, 343.0, 314.0, 209.0, 602.0, 441.0, 507.0, 400.0, 352.0, 50.0, 747.0, 965.0, 129.0, 1011.0, 145.0, 390.0, 887.0, 930.0, 77.0, 775.0, 350.0
);

// tri-strip, degen
//vec3 vertex_positions_degen[15] = vec3[](
//vec3(-.5f, -.5f, -.5f),
//vec3(+.5f, -.5f, -.5f),
//vec3(-.5f, -.5f, +.5f),
//vec3(+.5f, -.5f, +.5f),
//vec3(+.5f, +.5f, +.5f),
//vec3(+.5f, -.5f, -.5f),
//vec3(+.5f, +.5f, -.5f),
//vec3(-.5f, -.5f, -.5f),
//vec3(-.5f, +.5f, -.5f),
//vec3(-.5f, -.5f, +.5f),
//vec3(-.5f, +.5f, +.5f),
//vec3(+.5f, +.5f, +.5f),
//vec3(-.5f, +.5f, -.5f),
//vec3(+.5f, +.5f, -.5f),
//vec3(+.5f, +.5f, -.5f) // degen triangle
//);

// triangles
//vec3 vertex_positions[8] = vec3[](
//vec3(-.5f, -.5f, -.5f),
//vec3(+.5f, -.5f, -.5f),
//vec3(+.5f, +.5f, -.5f),
//vec3(-.5f, +.5f, -.5f),
//vec3(-.5f, +.5f, +.5f),
//vec3(+.5f, +.5f, +.5f),
//vec3(+.5f, -.5f, +.5f),
//vec3(-.5f, -.5f, +.5f)
//);

// indices
//int[] triangles = int[36](
//0, 2, 1, //face front
//0, 3, 2,
//2, 3, 4, //face top
//2, 4, 5,
//1, 2, 5, //face right
//1, 5, 6,
//0, 7, 4, //face left
//0, 4, 3,
//5, 4, 7, //face back
//5, 7, 6,
//0, 6, 7, //face bottom
//0, 1, 6
//);

uint deinterleaveBits(uint x) {
    x &= 0x55555555u;
    x = (x | (x >> 1)) & 0x33333333u;
    x = (x | (x >> 2)) & 0x0F0F0F0Fu;
    x = (x | (x >> 4)) & 0x00FF00FFu;
    x = (x | (x >> 8)) & 0x0000FFFFu;
    return x;
}

uvec2 decodeMorton(uint index) {
    uint x = deinterleaveBits(index);
    uint y = deinterleaveBits(index >> 1);
    return uvec2(x, y);
}

#define SIZE vec3(_SIZE_XZ_, _SIZE_Y_, _SIZE_XZ_)
#define REGIONS _REGIONS_

layout(binding = 6) uniform sampler2DArray u_height;

uniform mat4 u_mvp_matrix;
uniform vec3 u_miscellaneous;
uniform vec3 u_camera_pos;
uniform float u_spacing;
uniform vec3 u_circle;

layout(std430, binding = 0) buffer RegionOffsets
{
    ivec2 ssbo_region_offsets[REGIONS * REGIONS];
    int ssbo_region_map[REGIONS * REGIONS];
};

/*
layout(location = 0) in float in_pos[4]; // instanced per cloud cube

void main() {
    const int vertex_count = 15;
    int sub_instance = gl_VertexID / vertex_count;
//    vec3 v = vertex_positions[triangles[gl_VertexID % vertex_count]];
    vec3 v = vertex_positions_degen[gl_VertexID % vertex_count];
    vec3 p = in_pos[sub_instance].xyz;
    vec3 vertexPos = SIZEb * v + p;
    gl_Position = u_mvp_matrix * vec4(vertexPos, 1.0);
}
/*/
//layout(location = 0) in float in_pos;// instanced per cloud cube

void main() {
    //    uint morton_index = uint(gl_BaseInstanceARB + gl_InstanceID - u_temporary);
    uint instance = uint(gl_BaseInstanceARB + gl_InstanceID);
    uint morton_index = instance % (128u * 128u);

    uvec2 morton_pos = decodeMorton(morton_index);

    //    int ri = int(morton_pos.x) % 16 + 16 * (int(morton_pos.y % 16));
    //    vec3 r = vec3(random_offsets_x[ri], random_offsets_y[ri], 0.0) / 256.0;
    vec3 r = vec3(random_offsets_xy[(instance%256) * 2], 0.0, random_offsets_xy[(instance%256) * 2 + 1]) / 1024.0;
    r = (r - 0.5) * u_spacing;

    //    vec3 p = vec3(instance % 128, in_pos, int(instance / 128)) * vec3(u_spacing, 64, u_spacing) + u_miscellaneous;
    //    vec3 region_origin = u_miscellaneous;
    vec3 region_offset = vec3(0.0);
    // FIXME: This is wrong I think
    int region = int(instance / (128u * 128u));
//    region = ssbo_region_map[region];

    //    int region = u_regions[int(instance / (128u * 128u))];
    //    int region = u_region;
    //    region_origin.x = region / REGIONS - REGIONS/2;
    region_offset.xz = vec2(ssbo_region_offsets[region].xy);
    region_offset.y = 0;
    //    region_origin.z = region % REGIONS - REGIONS/2;
    region_offset *= u_spacing * 128;
    //    float y = in_pos;
//            float y = float(morton_index) / (128. * 128.);

//    region = ssbo_region_map[region];

    float y = texelFetch(u_height, ivec3(int(morton_pos.x), int(morton_pos.y), region), 0).r;
    //    float y = texture(u_height, vec3((float(morton_pos.x) + 0.5) / 128.0, (float(morton_pos.y) + 0.5) / 128.0, region), 0).r;
//    y += 0.01;
//    float y = 0.5;
    vec3 p = vec3(morton_pos.x, y, morton_pos.y) * vec3(u_spacing, 64, u_spacing) + region_offset;
//    p += r;
    p.y = 0.0;

    vec3 d = u_camera_pos - p;
    ivec3 corner = ivec3(d.x <= 0.0 ? -1:1, d.y <= 0.0 ? -1:1, d.z <= 0.0 ? -1:1);
    vec3 v = vec3(0.0);
    if (corner == ivec3(1, 1, 1)) v = vert_pxpypz[gl_VertexID];
    else if (corner == ivec3(1, 1, -1)) v = vert_pxpynz[gl_VertexID];
    else if (corner == ivec3(1, -1, 1)) v = vert_pxnypz[gl_VertexID];
    else if (corner == ivec3(1, -1, -1)) v = vert_pxnynz[gl_VertexID];
    else if (corner == ivec3(-1, 1, 1)) v = vert_nxpypz[gl_VertexID];
    else if (corner == ivec3(-1, 1, -1)) v = vert_nxpynz[gl_VertexID];
    else if (corner == ivec3(-1, -1, 1)) v = vert_nxnypz[gl_VertexID];
    else if (corner == ivec3(-1, -1, -1)) v = vert_nxnynz[gl_VertexID];

//    vec3 v = vertex_positions[gl_VertexID];


    vec3 vertexPos = SIZE * v + p;
    gl_Position = u_mvp_matrix * vec4(vertexPos, 1.0);

    bool isOutsideRadius = length(u_camera_pos.xz - p.xz) > u_circle.z;
    isOutsideRadius = false;
    if (y == 0.0 || isOutsideRadius) {
        gl_Position = vec4(0.0, 0.0, 0.0, -1.0);
    }
}
//*/