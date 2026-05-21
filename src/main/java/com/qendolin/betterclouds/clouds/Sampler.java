package com.qendolin.betterclouds.clouds;

import com.qendolin.betterclouds.config.Config;
import com.qendolin.betterclouds.config.ConfigManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.OctaveSimplexNoiseSampler;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;

import java.util.List;

public class Sampler {
    @SuppressWarnings("unchecked")
    public static final List<Integer>[] OCTAVE_OPTIONS = new List[]{
            List.of(-1, 0, 1, 2),
            List.of(-3, -1, 0, 1, 2),
            List.of(-2, 0, 1, 2),
            List.of(0, 1, 2)
    };

    public static final float REGION_SIZE = 2048;
    public static final float BASE_FUZZINESS = 0.9f;

    private final long seed;
    private final SimplexNoiseSampler regionNoise;
    private final SimplexNoiseSampler coverageNoise;
    private final List<OctaveSimplexNoiseSampler> detailNoises;

    public Sampler(long seed) {
        ChunkRandom random = new ChunkRandom(new CheckedRandom(seed));
        ChunkRandom regionRandom = new ChunkRandom(new CheckedRandom(random.nextLong()));
        this.seed = seed;

        Config options = ConfigManager.instance();

        regionNoise = new SimplexNoiseSampler(regionRandom);
        coverageNoise = new SimplexNoiseSampler(random);
        detailNoises = options.noisePreset().octaves.stream()
                .map(octave -> new OctaveSimplexNoiseSampler(random, octave))
                .toList();
    }

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
        if (detailNoises.isEmpty()) return 0;
        double value;
        if (detailNoises.size() > 1) {
            double region = (regionNoise.sample(x / REGION_SIZE, z / REGION_SIZE) * 0.5 + 0.5) * detailNoises.size();
            int noiseIndex = Math.floorMod((int) region, detailNoises.size());
            OctaveSimplexNoiseSampler noise1 = detailNoises.get(noiseIndex);
            OctaveSimplexNoiseSampler noise2 = detailNoises.get((noiseIndex + 1) % detailNoises.size());

            value = MathHelper.lerp(
                    Math.pow(MathHelper.clamp(region - noiseIndex, 0, 1), 5),
                    noise1.sample(x / scale / 128f, z / scale / 128f, false),
                    noise2.sample(x / scale / 128f, z / scale / 128f, false)
            );
        }
        else {
            value = detailNoises.get(0).sample(x / scale / 128f, z / scale / 128f, false);
        }
        value = value / 2 + 0.5;
        value = (value - (1 - cloudiness)) / cloudiness;
        value *= smoothstep(-0.6 * cloudiness - 0.3, -0.6 * cloudiness, coverageNoise.sample(x / 1024f, z / 1024f));

        float random = hashToFloat(seed, 'B', x, z);
        if (random > value + (BASE_FUZZINESS - fuzziness)) value = 0;
        return (float) value;
    }

    double smoothstep(double edge0, double edge1, double x) {
        x = MathHelper.clamp((x - edge0) / (edge1 - edge0), 0.0f, 1.0f);
        return x * x * (3 - 2 * x);
    }

}
