package com.qendolin.betterclouds.clouds;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.math.noise.OctaveSimplexNoiseSampler;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;

import java.util.List;

public class Sampler {
    private final ChunkRandom RANDOM = new ChunkRandom(new CheckedRandom(1337));
    // -1, 0, 1, 2     - Default with frequent clumps of clouds
    // -3, -1, 0, 1, 2 - Pretty big, sparse fields of clouds and fields of clear sky, maybe to big for
    //                   32 Chunks of render distance
    // -2, 0, 1, 2     - Medium heaps of clouds with fields of clear sky, no problem for 32 Chunks
    // 0, 1, 2         - Many spots of small clouds with some medium holes of clear sky
    private final List<Integer> OCTAVES = ImmutableList.of(-1, 0, 1, 2);
    private final OctaveSimplexNoiseSampler NOISE = new OctaveSimplexNoiseSampler(RANDOM, OCTAVES);
    private final SimplexNoiseSampler BIG_NOISE = new SimplexNoiseSampler(RANDOM);

    public float randomOffsetX(int x, int z) {
        return hashToFloat(x, z, 'X');
    }

    // https://stackoverflow.com/a/17479300/7448536
    // Distribution is very uniform from my testing
    private float hashToFloat(int... values) {
        int hash = hash(values);

        int ieeeMantissa = 0x007FFFFF;
        int ieeeOne = 0x3F800000;

        hash &= ieeeMantissa;
        hash |= ieeeOne;
        float f = Float.intBitsToFloat(hash);
        return f - 1;
    }

    // Jenkins hash function
    private int hash(int... values) {
        int hash = 0;
        for (int value : values) {
            hash += value;
            hash += hash << 10;
            hash ^= hash >> 6;
        }
        hash += hash << 3;
        hash ^= hash >> 11;
        hash += hash << 15;
        return hash;
    }

    public float randomOffsetZ(int x, int z) {
        return hashToFloat(x, z, 'Z');
    }

    public float sample(int x, int z, float cloudiness, float fuzziness, float scale) {
        double value = NOISE.sample(x / scale / 128f, z / scale / 128f, false);
        value = value / 2 + 0.5;
        value = (value - (1 - cloudiness)) / cloudiness;
        value *= smoothstep(-0.6 * cloudiness - 0.3, -0.6 * cloudiness, BIG_NOISE.sample(x / 1024f, z / 1024f));

        float random = hashToFloat(x, z);
        if (random > value + (1 - fuzziness)) {
            return 0;
        }
        return (float) value;
    }

    // https://stackoverflow.com/a/50815919/7448536
    double smoothstep(double edge0, double edge1, double x) {
        // Scale, bias and saturate x to 0..1 range
        x = Math.min(Math.max((x - edge0) / (edge1 - edge0), 0.0f), 1.0f);
        // Evaluate polynomial
        return x * x * (3 - 2 * x);
    }
}
