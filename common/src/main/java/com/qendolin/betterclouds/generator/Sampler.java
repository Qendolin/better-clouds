package com.qendolin.betterclouds.generator;

import com.qendolin.betterclouds.config.Config;
import com.qendolin.betterclouds.config.ConfigManager;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

import java.util.Arrays;
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

    public static final float REGION_SIZE = 2048;
    public static final float BASE_FUZZINESS = 0.9f;

    private final long seed;

    private final SimplexNoise regionNoise;
    private final SimplexNoise coverageNoise;
    private final List<PerlinSimplexNoise> detailNoises;

    public Sampler(long seed) {
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(seed));
        WorldgenRandom regionRandom = new WorldgenRandom(new LegacyRandomSource(random.nextInt()));
        this.seed = seed;

        Config options = ConfigManager.instance();

        regionNoise = new SimplexNoise(regionRandom);
        coverageNoise = new SimplexNoise(random);
        detailNoises = options.noisePreset().octaves
                .stream().map(octave -> new PerlinSimplexNoise(random, octave)).toList();
    }

    /**
     * For testing only
     */
    public Sampler() {
        this.seed = 1337;
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(seed));
        WorldgenRandom regionRandom = new WorldgenRandom(new LegacyRandomSource(random.nextInt()));

        Config options = new Config();
        options.noisePreset().octaves = Arrays.asList(
                Arrays.asList(-1, 0, 1, 2),
                Arrays.asList(-3, -1, 0, 1, 2),
                Arrays.asList(-2, 0, 1, 2),
                Arrays.asList(0, 1, 2)
        );

        regionNoise = new SimplexNoise(regionRandom);
        coverageNoise = new SimplexNoise(random);
        detailNoises = options.noisePreset().octaves
                .stream().map(octave -> new PerlinSimplexNoise(random, octave)).toList();
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
        double value;

        // TODO: A vanilla like cloud distribution is not possible with this function
        if (detailNoises.size() > 1) {
            double regionNoiseValue = (regionNoise.getValue(x / REGION_SIZE, z / REGION_SIZE) * 0.5 + 0.5) * detailNoises.size();
            int noiseInd = (int) regionNoiseValue;
            PerlinSimplexNoise noise1 = detailNoises.get(noiseInd), noise2 = detailNoises.get((noiseInd + 1) % detailNoises.size());

            value = Mth.lerp(
                    Math.pow(Mth.clamp(regionNoiseValue - noiseInd, 0, 1), 5),
                    noise1.getValue(x / scale / 128f, z / scale / 128f, false),
                    noise2.getValue(x / scale / 128f, z / scale / 128f, false)
            );
        } else {
            value = detailNoises.getFirst().getValue(x / scale / 128f, z / scale / 128f, false);
        }

        value = value / 2 + 0.5;
        value = (value - (1 - cloudiness)) / cloudiness;
        value *= smoothstep(-0.6 * cloudiness - 0.5, -0.6 * cloudiness, coverageNoise.getValue(x / 1024f, z / 1024f));

        float random = hashToFloat(seed, 'B', x, z);
        if (random > value + (BASE_FUZZINESS - fuzziness)) value = 0;
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
