package com.qendolin.betterclouds.clouds;

public class Mesh {
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
}
