package com.qendolin.betterclouds.clouds;

import com.qendolin.betterclouds.config.Config;
import com.qendolin.betterclouds.config.ConfigManager;
import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
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

    public static final float REGION_SIZE = 2048;
    public static final float BASE_FUZZINESS = 0.9f;

    public static int cacheHit = 0;
    public static int cacheMiss = 0;

    private final long seed;

    private final SimplexNoise REGION_NOISE;
    private final SimplexNoise COVERAGE_NOISE;
    private final List<PerlinSimplexNoise> DETAIL_NOISES;

    private final SamplerLRUCache cache;

    public Sampler(long seed) {
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(seed));
        WorldgenRandom regionRandom = new WorldgenRandom(new LegacyRandomSource(random.nextInt()));
        this.seed = seed;

        Config options = ConfigManager.instance();
        cache = options.useSamplerCaching ? new SamplerLRUCache() : null;

        REGION_NOISE = new SimplexNoise(regionRandom);
        COVERAGE_NOISE = new SimplexNoise(random);
        DETAIL_NOISES = options.noisePreset().octaves
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

    public static long cacheHash(int x, int z) {
        return ((long) x << 32) | (z & 0xffffffffL);
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
        if (cache == null) return sampleWithoutCache(x, z, cloudiness, fuzziness, scale);

        long cacheHash = cacheHash(x, z);
        float cachedValue = cache.get(cacheHash);
        if (!Double.isNaN(cachedValue)) {
            cacheHit++;
            return cachedValue;
        }

        float value = sampleWithoutCache(x, z, cloudiness, fuzziness, scale);
        cache.put(cacheHash, value);
        cacheMiss++;
        return value;
    }

    private float sampleWithoutCache(int x, int z, float cloudiness, float fuzziness, float scale) {
        // TODO: A vanilla like cloud distribution is not possible with this function
        double regionNoise = (REGION_NOISE.getValue(x / REGION_SIZE, z / REGION_SIZE) * 0.5 + 0.5) * DETAIL_NOISES.size();
        int noiseInd = (int) regionNoise;
        PerlinSimplexNoise noise1 = DETAIL_NOISES.get(noiseInd), noise2 = DETAIL_NOISES.get((noiseInd + 1) % DETAIL_NOISES.size());

        double value = Mth.lerp(
                Math.pow(Mth.clamp(regionNoise - noiseInd, 0, 1), 5),
                noise1.getValue(x / scale / 128f, z / scale / 128f, false),
                noise2.getValue(x / scale / 128f, z / scale / 128f, false)
        );
        value = value / 2 + 0.5;
        value = (value - (1 - cloudiness)) / cloudiness;
        value *= 0.2 + 0.7 * smoothstep(-0.6 * cloudiness - 0.3, -0.6 * cloudiness, COVERAGE_NOISE.getValue(x / 1024f, z / 1024f));

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

    public static class SamplerLRUCache {
        private static final int CLOUD_DIST_BLOCKS_TO_POINT_COUNT = 244;

        private final int capacity;
        private final Long2FloatLinkedOpenHashMap map;

        public SamplerLRUCache() {
            this(ConfigManager.instance().blockDistance() * CLOUD_DIST_BLOCKS_TO_POINT_COUNT);
        }

        public SamplerLRUCache(int capacity) {
            this.capacity = capacity;
            this.map = new Long2FloatLinkedOpenHashMap(Math.min(capacity, 1000), 0.5f);     // cap initial capacity to prevent lag spikes
            this.map.defaultReturnValue(Float.NaN);
        }

        public float get(long key) {
            return map.getAndMoveToFirst(key);
        }

        public void put(long key, float value) {
            map.putAndMoveToFirst(key, value);
            if (map.size() > capacity) map.removeLastFloat();
        }

        public boolean contains(long key) {
            return map.containsKey(key);
        }
    }
}
