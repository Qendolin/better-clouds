package com.qendolin.betterclouds.clouds;

import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

import java.util.List;

public class Sampler {
    /**
     * -1, 0, 1, 2     - Default with frequent clumps of clouds<br>
     * -3, -1, 0, 1, 2 - Pretty big, sparse fields of clouds and fields of clear sky, maybe too big for 32 Chunks of render distance<br>
     * -2, 0, 1, 2     - Medium heaps of clouds with fields of clear sky, no problem for 32 Chunks<br>
     * 0, 1, 2         - Many spots of small clouds with some medium holes of clear sky<br>
     *
     * <p>Run SamplerTest.java to visualize the noise function</p>
     */
    @SuppressWarnings("unchecked")
    public static final List<Integer>[] OCTAVE_OPTIONS = new List[] {
            List.of(-1, 0, 1, 2),
            List.of(-3, -1, 0, 1, 2),
            List.of(-2, 0, 1, 2),
            List.of(0, 1, 2)
    };

    private final long seed;
    private final PerlinSimplexNoise NOISE;
    private final SimplexNoise BIG_NOISE;

    public Sampler(long seed) {
        this(seed, 0);
    }

    public Sampler(long seed, int octaveOption) {
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(seed));
        this.seed = seed;
        NOISE = new PerlinSimplexNoise(random, OCTAVE_OPTIONS[octaveOption]);
        BIG_NOISE = new SimplexNoise(random);
    }

    // Jenkins hash function (seed does not have to be prime)
    public static long hash(long seed, int... values) {
        long hash = seed;
        for (int value : values) {
            hash += value;
            hash += hash << 10;
            hash ^= hash >>> 6;
        }
        hash += hash << 3;
        hash ^= hash >> 11;
        hash += hash << 15;
        return hash;
    }

    // https://stackoverflow.com/a/17479300/7448536
    // Distribution is very uniform from my testing
    public static float hashToFloat(long seed, int... values) {
        int hash = Long.hashCode(hash(seed, values));

        int ieeeMantissa = 0x007FFFFF;
        int ieeeOne = 0x3F800000;

        hash &= ieeeMantissa;
        hash |= ieeeOne;
        float f = Float.intBitsToFloat(hash);
        return f - 1;
    }

    public long getSeed() {
        return seed;
    }

    public float randomOffsetX(int x, int z, int pass) {
        return hashToFloat(seed, 'S', x, z, 'X', pass);
    }

    public float randomOffsetZ(int x, int z, int pass) {
        return hashToFloat(seed, 'S', x, z, 'Z', pass);
    }

    public float sample(int x, int z, float cloudiness, float fuzziness, float scale) {
        // TODO: A vanilla like cloud distribution is not possible with this function
        double value = NOISE.getValue(x / scale / 128f, z / scale / 128f, false);
        value = value / 2 + 0.5;
        value = (value - (1 - cloudiness)) / cloudiness;
        value *= smoothstep(-0.6 * cloudiness - 0.3, -0.6 * cloudiness, BIG_NOISE.getValue(x / 1024f, z / 1024f));

        float random = hashToFloat(x, z);
        if (random > value + (1 - fuzziness)) {
            return 0;
        }
        return (float) value;
    }

    // https://stackoverflow.com/a/50815919/7448536
    double smoothstep(double edge0, double edge1, double x) {
        // Scale, bias and saturate x to 0..1 range
        x = Mth.clamp((x - edge0) / (edge1 - edge0), 0.0f, 1.0f);
        // Evaluate polynomial
        return x * x * (3 - 2 * x);
    }
}
