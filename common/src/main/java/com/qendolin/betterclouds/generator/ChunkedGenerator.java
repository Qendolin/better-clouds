package com.qendolin.betterclouds.generator;

import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.config.Config;
import com.qendolin.betterclouds.config.ConfigManager;
import com.qendolin.betterclouds.rendering.opengl.Debug;
import com.qendolin.betterclouds.rendering.opengl.internal.Buffer;
import com.qendolin.betterclouds.util.ChatUtil;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ChunkedGenerator implements AutoCloseable {
    private static int cacheHit = 0;
    private static int cacheMiss = 0;
    private final long seed;
    private Sampler sampler;
    private DummyCache pointCache;
    private double originX, originZ;
    private long lastCloudTicks;
    private int lastRendererTicks;
    private float lastTickIncrement = 1;
    private boolean queueCacheClear = false;

    private Buffer buffer;
    @Nullable
    private Task queuedTask;
    @Nullable
    private Task runningTask;
    @Nullable
    private Task completedTask;
    @Nullable
    private Task swappedTask;

    public ChunkedGenerator(long seed) {
        sampler = new Sampler(seed);
        this.seed = seed;

        Config options = ConfigManager.instance();
        int gridWidth = (int) (options.blockDistance() / options.spacing / options.chunkSize * 2);
        // default capacity: number of chunks in the grid + some extra for when camera position changes
        pointCache = options.useSamplerCaching ? new ChunkCache(gridWidth * gridWidth + gridWidth * 2) : new DummyCache();
    }

    private static int calcBufferSize(Config options) {
        int distance = options.blockDistance();
        int size = Mth.floor(distance / options.spacing) + Mth.ceil(distance / options.spacing);
        return size > 0 ? size : 8 * 16;
    }

    private static int floorCloudChunk(double coord, int chunkSize) {
        return (int) Math.floor(coord / chunkSize);
    }

    public static long cacheHash(int x, int z) {
        return ((long) x << 32) | (z & 0xffffffffL);
    }

    public synchronized boolean canGenerate() {
        return queuedTask != null;
    }

    public synchronized boolean canSwap() {
        return completedTask != null && completedTask != swappedTask;
    }

    public synchronized boolean canRender() {
        return completedTask != null;
    }

    public synchronized List<ChunkIndex> chunks() {
        if (swappedTask == null) return List.of();
        return swappedTask.chunks();
    }

    public Buffer buffer() {
        return buffer;
    }

    public synchronized int instanceVertexCount() {
        if (swappedTask == null) return 0;
        return swappedTask.instanceVertexCount();
    }

    public double originX() {
        return originX;
    }

    public double originZ() {
        return originZ;
    }

    public synchronized double renderOriginX(double cameraX) {
        if (swappedTask == null) return 0;
        return swappedTask.chunkX() * swappedTask.options().chunkSize - cameraX + originX;
    }

    public synchronized double renderOriginZ(double cameraZ) {
        if (swappedTask == null) return 0;
        return swappedTask.chunkZ() * swappedTask.options().chunkSize - cameraZ + originZ;
    }

    public synchronized Config config() {
        if (swappedTask == null) return null;
        return swappedTask.options;
    }

    public synchronized boolean generating() {
        return runningTask != null;
    }

    @Override
    public void close() {
        clear();
        if (buffer != null) buffer.close();
    }

    public void bind() {
        buffer.bind();
    }

    public void unbind() {
        buffer.unbind();
    }

    @SuppressWarnings("UnusedReturnValue")
    public synchronized boolean reallocateIfStale(Config options, boolean fancy) {
        int bufferSize = calcBufferSize(options);

        if (buffer.hasChanged(bufferSize, fancy, options.usePersistentBuffers)) {
            clear();
            buffer.close();
            buffer = new Buffer(bufferSize, fancy, options.usePersistentBuffers);
            return true;
        }
        return false;
    }

    public synchronized void clear() {
        if (queuedTask != null) queuedTask.cancel();
        if (swappedTask != null) swappedTask.cancel();
        if (completedTask != null) completedTask.cancel();
        if (runningTask != null) runningTask.cancel();
        queuedTask = null;
        runningTask = null;
        completedTask = null;
        swappedTask = null;
    }

    public synchronized void allocate(Config options, boolean fancy) {
        clear();
        int bufferSize = calcBufferSize(options);
        if (buffer != null) buffer.close();
        buffer = new Buffer(bufferSize, fancy, options.usePersistentBuffers);
    }

    public synchronized void update(Vector3d camera, long cloudTicks, int rendererTicks, float tickDelta, Config options, float cloudiness) {
        originX = RandomPath.getPathX(cloudTicks, tickDelta * lastTickIncrement, options.travelSpeed);
        originZ = RandomPath.getPathZ(cloudTicks, tickDelta * lastTickIncrement, options.travelSpeed);

        if (rendererTicks != lastRendererTicks) {
            lastTickIncrement = cloudTicks - lastCloudTicks;
            lastCloudTicks = cloudTicks;
            lastRendererTicks = rendererTicks;
        }

        double worldOriginX = camera.x - this.originX;
        double worldOriginZ = camera.z - this.originZ;

        int chunkX = floorCloudChunk(worldOriginX, options.chunkSize);
        int chunkZ = floorCloudChunk(worldOriginZ, options.chunkSize);

        float distance = options.blockDistance();

        boolean updateGeometry;
        if (queuedTask != null || runningTask != null || completedTask != null) {
            Task prevTask = queuedTask == null ? (runningTask == null ? completedTask : runningTask) : queuedTask;
            int prevChunkX = prevTask.chunkX();
            int prevChunkZ = prevTask.chunkZ();
            boolean chunkChanged = Math.abs(prevChunkX - chunkX) + Math.abs(prevChunkZ - chunkZ) > 4;
            boolean optionsChanged = !options.equals(prevTask.options);
            float prevCloudiness = prevTask.cloudiness();
            boolean cloudinessChanged = Math.ceil(cloudiness * 100) != Math.ceil(prevCloudiness * 100);

            boolean bufferCleared = buffer.swapCount() == 0 && queuedTask == null && runningTask == null && (completedTask == null || completedTask == swappedTask);

            if (optionsChanged || cloudinessChanged) {
                BetterCloudsStatic.getLogger().info((optionsChanged ? "Configuration" : "Cloudiness") + " changed, updating geometry");
                queueCacheClear = true;
            }
            updateGeometry = chunkChanged || optionsChanged || cloudinessChanged || bufferCleared;
        } else {
            BetterCloudsStatic.getLogger().debug("No tasks, updating geometry");
            updateGeometry = true;
        }

        if (Debug.generatorChangeCacheSize >= 30) {
            pointCache = new ChunkCache(Debug.generatorChangeCacheSize);
            BetterCloudsStatic.getLogger().debug("Changing cache size and invalidating cache");
            Debug.generatorForceUpdate = true;
            Debug.generatorChangeCacheSize = 0;
        }

        if (Debug.generatorForceUpdate) {
            Debug.generatorForceUpdate = false;
            BetterCloudsStatic.getLogger().debug("Forcibly updating geometry");
            updateGeometry = true;
        }

        if (updateGeometry) {
            queuedTask = new Task(chunkX, chunkZ, new Config(options), distance, cloudiness, this);
        }
    }

    public synchronized void generate() {
        if (queuedTask == null) {
            BetterCloudsStatic.getLogger().warn("generate called with no queued task");
            return;
        }

        if (runningTask != null) {
            runningTask.cancel();
        }

        runningTask = queuedTask;
        queuedTask = null;

        if (queueCacheClear) {
            queueCacheClear = false;
            refresh();
        }

        if (runningTask.ran()) {
            BetterCloudsStatic.getLogger().warn("Queued generator task #{} already ran", runningTask.id());
        }

        final Task boundTask = runningTask;
        CompletableFuture.runAsync(runningTask::run)
                .whenComplete((_, throwable) -> {
                    synchronized (this) {
                        if (throwable != null) {
                            BetterCloudsStatic.getLogger().error("Generator task #{} ran with error", runningTask.id(), throwable);
                        }

                        if (boundTask != runningTask) {
                            if (boundTask.completed()) {
                                BetterCloudsStatic.getLogger().warn("Generator task #{} completed but task #{} was expected", boundTask.id(), runningTask.id());
                            } else if (!boundTask.cancelled() && throwable == null) {
                                BetterCloudsStatic.getLogger().warn("Generator task #{} ran without error, completion or cancellation", boundTask.id());
                            }
                        } else {
                            if (boundTask.completed()) {
                                completedTask = runningTask;
                            } else if (!boundTask.cancelled() && throwable == null) {
                                BetterCloudsStatic.getLogger().warn("Generator task #{} ran without error, completion or cancellation", boundTask.id());
                            }
                            runningTask = null;
                        }
                    }
                });
    }

    private void refresh() {
        pointCache.clear();
        sampler = new Sampler(seed);
    }

    public synchronized void swap() {
        if (completedTask == null) {
            BetterCloudsStatic.getLogger().warn("swap called with no completed task");
            return;
        }
        if (swappedTask == completedTask) {
            BetterCloudsStatic.getLogger().warn("swap called with swapped task");
            return;
        }

        completedTask.generator.buffer.swap();
        swappedTask = completedTask;

        if (Debug.isProfilingEnabled()) {
            long elapsed = swappedTask.elapsedMs(Util.getMillis());
            ChatUtil.debugChatMessage(
                    "profiling.genTimes",
                    elapsed, 1000f / elapsed,
                    cacheHit, cacheMiss + cacheHit,
                    pointCache.capacity(),
                    (float) cacheHit / (cacheMiss + cacheHit) * 100
            );
        }
    }

    private static class Task {
        private static final AtomicInteger nextId = new AtomicInteger(1);

        private final int id;
        private final int chunkX;
        private final int chunkZ;
        private final Config options;
        private final float distance;
        private final float cloudiness;
        private final ChunkedGenerator generator;
        private final AtomicBoolean ran = new AtomicBoolean();
        private final AtomicBoolean cancelled = new AtomicBoolean();
        private final AtomicBoolean completed = new AtomicBoolean();
        private final List<ChunkIndex> chunks = new ArrayList<>();
        private int cloudCount;

        private long startTime;

        public Task(int chunkX, int chunkZ, Config options, float distance, float cloudiness, ChunkedGenerator generator) {
            this.id = nextId.getAndIncrement();
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.options = options;
            this.distance = distance;
            this.cloudiness = cloudiness;
            this.generator = generator;
        }

        public void cancel() {
            synchronized (this) {
                if (completed.get()) return;
                if (cancelled.getAndSet(true)) {
                    notify();
                    return;
                }
                BetterCloudsStatic.getLogger().debug("Generator task #{} cancelled", id);
                try {
                    wait();
                } catch (InterruptedException e) {
                    BetterCloudsStatic.getLogger().error("Generator task #{} interrupted after cancelled", id, e);
                }
            }
        }

        public int id() {
            return id;
        }

        public boolean completed() {
            return completed.get();
        }

        public int cloudCount() {
            return cloudCount;
        }

        public int chunkX() {
            return chunkX;
        }

        public int chunkZ() {
            return chunkZ;
        }

        public int instanceVertexCount() {
            return generator.buffer.instanceVertexCount();
        }

        public Config options() {
            return options;
        }

        public float cloudiness() {
            return cloudiness;
        }

        public List<ChunkIndex> chunks() {
            return chunks;
        }

        public boolean ran() {
            return ran.get();
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        public boolean cancelled() {
            return cancelled.get();
        }

        public long elapsedMs(long now) {
            if (startTime == 0) return 0;
            return now - startTime;
        }

        public void run() {
            synchronized (this) {
                if (ran.getAndSet(true) || cancelled.get()) return;
            }
            startTime = Util.getMillis();

            int distance = options.blockDistance();
            float spacing = options.spacing;

            // relative sample-grid range centered around this task's origin chunk
            int gridMin = -Mth.floor(distance / spacing);
            int gridMax = Mth.ceil(distance / spacing);

            // relative sample-grid chunks generated for this task
            int chunkMin = roundToMultiple(gridMin, options.chunkSize);
            int chunkMax = roundToMultiple(gridMax, options.chunkSize);

            // global/world sample-grid origin of this task's origin chunk
            int gridOriginX = Mth.floor((chunkX * options.chunkSize) / spacing);
            int gridOriginZ = Mth.floor((chunkZ * options.chunkSize) / spacing);

            generator.buffer.clear();
            cacheHit = 0;
            cacheMiss = 0;

            // The outer loop generates chunks
            for (int chunkX = chunkMin; chunkX < chunkMax; chunkX += options.chunkSize) {
                for (int chunkZ = chunkMin; chunkZ < chunkMax; chunkZ += options.chunkSize) {
                    int chunkCloudIndex = cloudCount;

                    int globalChunkX = chunkX + gridOriginX;
                    int globalChunkZ = chunkZ + gridOriginZ;
                    long cacheKey = cacheHash(
                            Math.floorDiv(globalChunkX, options.chunkSize),
                            Math.floorDiv(globalChunkZ, options.chunkSize)
                    );

                    SamplePoints samplePoints = generator.pointCache.get(cacheKey);
                    if (samplePoints == null) {
                        cacheMiss++;
                        samplePoints = genSamplePoints(
                                chunkX, chunkZ,
                                gridMin, gridMax,
                                gridOriginX, gridOriginZ,
                                spacing
                        );
                        generator.pointCache.put(cacheKey, samplePoints);
                    } else {
                        cacheHit++;
                    }

                    for (AABB point : samplePoints.points()) {
                        generator.buffer.put(
                                (float) (point.minX - this.chunkX * options.chunkSize),
                                (float) point.minY,
                                (float) (point.minZ - this.chunkZ * options.chunkSize)
                        );
                    }
                    cloudCount += samplePoints.points().size();

                    if (chunkCloudIndex != cloudCount && samplePoints.bounds() != null) {
                        chunks.add(new ChunkIndex(chunkCloudIndex, cloudCount - chunkCloudIndex, samplePoints.bounds()));
                    }

                    if (cancelled.get()) {
                        synchronized (this) {
                            notify();
                            return;
                        }
                    }
                }
            }

            generator.pointCache.swap();
            completed.set(true);
        }

        private @NonNull SamplePoints genSamplePoints(
                int chunkX, int chunkZ,
                int gridMin, int gridMax,
                int gridOriginX, int gridOriginZ,
                float spacing
        ) {
            // Relative sample-grid bounds for this chunk, clipped to the task range
            int chunkGridMinX = Math.max(chunkX, gridMin);
            int chunkGridMinZ = Math.max(chunkZ, gridMin);
            int chunkGridMaxX = Math.min(chunkX + options.chunkSize, gridMax);
            int chunkGridMaxZ = Math.min(chunkZ + options.chunkSize, gridMax);

            // global/world sample-grid chunk position for a stable cache key:
            // chunkX + gridOriginX, chunkZ + gridOriginZ.

            AABB bounds = null;

            ObjectArrayList<AABB> points = new ObjectArrayList<>(100);

            for (int gridX = chunkGridMinX; gridX < chunkGridMaxX; gridX++) {
                for (int gridZ = chunkGridMinZ; gridZ < chunkGridMaxZ; gridZ++) {
                    // global/world sample-grid coordinates
                    int globalGridX = gridX + gridOriginX;
                    int globalGridZ = gridZ + gridOriginZ;

                    if (options.sparsity > 0 && Sampler.hashToFloat(generator.sampler.getSeed(), 'G', globalGridX, globalGridZ) < options.sparsity)
                        continue;

                    // global/world block coordinates sampled from the cloud noise field
                    int sampleX = Mth.floor(globalGridX * spacing);
                    int sampleZ = Mth.floor(globalGridZ * spacing);

                    float value = generator.sampler.sample(sampleX, sampleZ, cloudiness, options.fuzziness, options.samplingScale);
                    if (value <= 0) continue;

                    for (int pass = 0; pass <= 1; pass++) {
                        float cloudHeight = value * value * options.yRange;

                        // the second pass is used to "fill the cloud void" as described in https://github.com/Qendolin/better-clouds/issues/262
                        // so the cube is placed below the normal cloud y range, like this:
                        if (pass == 1) cloudHeight *= -0.3f;

                        // global/world block coordinates for cached sample points
                        float x = sampleX + generator.sampler.randomOffsetX(sampleX, sampleZ, pass) * options.randomPlacement * spacing;
                        float y = cloudHeight + options.yOffset;
                        float z = sampleZ + generator.sampler.randomOffsetZ(sampleX, sampleZ, pass) * options.randomPlacement * spacing;

                        AABB pointAABB = new AABB(x, y, z, x, y, z);
                        bounds = bounds != null ? bounds.minmax(pointAABB) : pointAABB;
                        points.add(pointAABB);

                        if (value < 1 - options.bottomSparsity)
                            break;
                    }
                }
            }

            return new SamplePoints(bounds, points);
        }

        private int roundToMultiple(int n, int base) {
            return Math.floorDiv(n, base) * base;
        }
    }

    private record SamplePoints(AABB bounds, ObjectArrayList<AABB> points) {
    }

    private static class DummyCache {
        public DummyCache() {
        }

        public SamplePoints get(long key) {
            return defaultReturnValue();
        }

        public void put(long key, SamplePoints value) {
        }

        public void swap() {
        }

        public void clear() {
        }

        public SamplePoints defaultReturnValue() {
            return null;
        }

        public int capacity() {
            return 0;
        }
    }

    /**
     * LRU-like cache with two maps so new entries don't erase old entries that would have been future cache hits
     */
    private static class ChunkCache extends DummyCache {
        private final int capacity;
        private Long2ObjectLinkedOpenHashMap<SamplePoints> readMap, writeMap;

        public ChunkCache(int capacity) {
            this.capacity = capacity;
            this.readMap = new Long2ObjectLinkedOpenHashMap<>(capacity, 0.5f);
            this.writeMap = new Long2ObjectLinkedOpenHashMap<>(capacity, 0.5f);
        }

        public SamplePoints get(long key) {
            SamplePoints value = readMap.remove(key);
            if (value != defaultReturnValue())
                put(key, value);
            else if (BetterCloudsStatic.IS_DEV && writeMap.containsKey(key))
                BetterCloudsStatic.getLogger().warn("Same position accessed twice? %d, %d", (int) (key >> 32), (int) key);
            return value;
        }

        public void put(long key, SamplePoints value) {
            writeMap.putAndMoveToFirst(key, value);
            if (writeMap.size() > capacity) writeMap.removeLast();
        }

        public void swap() {
            Long2ObjectLinkedOpenHashMap<SamplePoints> prevReadMap = readMap;
            readMap = writeMap;
            writeMap = prevReadMap;
            writeMap.clear();
        }

        public void clear() {
            readMap.clear();
            writeMap.clear();
        }

        public SamplePoints defaultReturnValue() {
            return readMap.defaultReturnValue();
        }

        public int capacity() {
            return capacity;
        }
    }

    public static final class ChunkIndex {
        private final int start;
        private final int count;
        private final AABB bounds;

        private AABB cachedBounds;
        private float lastCloudsHeight;
        private float lastSizeXZ;
        private float lastSizeY;

        public ChunkIndex(int start, int count, AABB bounds) {
            this.start = start;
            this.count = count;
            this.bounds = bounds;
        }

        public AABB bounds(float cloudsHeight, float sizeXZ, float sizeY) {
            if (cloudsHeight == lastCloudsHeight && sizeXZ == lastSizeXZ && sizeY == lastSizeY) return cachedBounds;

            cachedBounds = bounds.move(0, cloudsHeight, 0).inflate(sizeXZ, sizeY, sizeXZ);
            lastCloudsHeight = cloudsHeight;
            lastSizeXZ = sizeXZ;
            lastSizeY = sizeY;
            return cachedBounds;
        }

        public int start() {
            return start;
        }

        public int count() {
            return count;
        }
    }
}
