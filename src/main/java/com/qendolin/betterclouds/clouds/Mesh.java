package com.qendolin.betterclouds.clouds;

public class Mesh {
    // Use TRIANGLE_STRIP
    public static final float[] FANCY_MESH = new float[]{
//     [position        ][normal          ]
//     [  x     y     z ][ x     y     z  ]
        -.5f, -.5f, -.5f, 0.0f, -1.f, 0.0f,
        +.5f, -.5f, -.5f, 0.0f, -1.f, 0.0f,
        -.5f, -.5f, +.5f, 0.0f, -1.f, 0.0f,
        +.5f, -.5f, +.5f, 0.0f, -1.f, 0.0f,
        +.5f, +.5f, +.5f, 0.0f, 0.0f, +1.f,
        +.5f, -.5f, -.5f, +1.f, 0.0f, 0.0f,
        +.5f, +.5f, -.5f, +1.f, 0.0f, 0.0f,
        -.5f, -.5f, -.5f, 0.0f, 0.0f, -1.f,
        -.5f, +.5f, -.5f, 0.0f, 0.0f, -1.f,
        -.5f, -.5f, +.5f, -1.f, 0.0f, 0.0f,
        -.5f, +.5f, +.5f, -1.f, 0.0f, 0.0f,
        +.5f, +.5f, +.5f, 0.0f, 0.0f, +1.f,
        -.5f, +.5f, -.5f, 0.0f, +1.f, 0.0f,
        +.5f, +.5f, -.5f, 0.0f, +1.f, 0.0f,
    };

    public static final int FANCY_MESH_VERTEX_SIZE = 6;
    public static final int FANCY_MESH_VERTEX_COUNT = FANCY_MESH.length / FANCY_MESH_VERTEX_SIZE;

    public static final float[] FAST_MESH = new float[]{
//     [position        ]
//     [  x     y     z ]
        -.5f, 0.0f, -.5f,
        +.5f, 0.0f, -.5f,
        -.5f, 0.0f, +.5f,
        +.5f, 0.0f, +.5f
    };

    public static final int FAST_MESH_VERTEX_SIZE = 3;
    public static final int FAST_MESH_VERTEX_COUNT = FAST_MESH.length / FAST_MESH_VERTEX_SIZE;

    // Use TRIANGLES, front faces are inside
    // TODO: Replace the quad mesh with the cube mesh
    public static final float[] CUBE_MESH = new float[]{
//     [position   ]
//     [ x   y   z ]
        -1, +1, -1, // -z
        -1, -1, -1,
        +1, -1, -1,
        -1, +1, -1,
        +1, -1, -1,
        +1, +1, -1,
        -1, +1, -1, // +y
        +1, +1, -1,
        +1, +1, +1,
        -1, +1, -1,
        +1, +1, +1,
        -1, +1, +1,
        +1, +1, +1, // +z
        +1, -1, +1,
        -1, -1, +1,
        +1, +1, +1,
        -1, -1, +1,
        -1, +1, +1,
        -1, +1, +1, // -x
        -1, -1, +1,
        -1, -1, -1,
        -1, +1, +1,
        -1, -1, -1,
        -1, +1, -1,
        +1, -1, -1, // -y
        -1, -1, -1,
        -1, -1, +1,
        +1, -1, -1,
        -1, -1, +1,
        +1, -1, +1,
        +1, +1, -1, // +x
        +1, -1, -1,
        +1, -1, +1,
        +1, +1, -1,
        +1, -1, +1,
        +1, +1, +1
    };

    public static final int CUBE_MESH_VERTEX_SIZE = 3;
    public static final int CUBE_MESH_VERTEX_COUNT = CUBE_MESH.length / CUBE_MESH_VERTEX_SIZE;
    public static final int QUAD_MESH_VERTEX_COUNT = 6;
}
