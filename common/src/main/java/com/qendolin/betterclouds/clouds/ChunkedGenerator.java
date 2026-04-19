package com.qendolin.betterclouds.clouds;

import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.config.Config;
import com.qendolin.betterclouds.util.ChatUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ChunkedGenerator implements AutoCloseable {
    private final Sampler sampler;
    private double originX;
    private double originZ;
    private Buffer buffer;
    @Nullable
    private Task queuedTask;
    @Nullable
    private Task runningTask;
    @Nullable
    private Task completedTask;
    @Nullable
    private Task swappedTask;

    public ChunkedGenerator(int seed) {
        sampler = new Sampler(seed);
    }

    private static int calcBufferSize(Config options) {
        int distance = options.blockDistance();
        int size = Mth.floor(distance / options.spacing)
                + Mth.ceil(distance / options.spacing);
        if (size <= 0) {
            return 8 * 16;
        }
        return size;
    }

    private static int floorCloudChunk(double coord, int chunkSize) {
        return (int) Math.floor(coord / chunkSize);
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

    public synchronized int cloudCount() {
        if (swappedTask == null) return 0;
        return swappedTask.cloudCount();
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
            buffer.close();
            buffer = new Buffer(bufferSize, fancy, options.usePersistentBuffers);
            clear();
            return true;
        }
        return false;
    }

    public synchronized void clear() {
        queuedTask = null;
        if (runningTask != null) runningTask.cancel();
        runningTask = null;
        completedTask = null;
        swappedTask = null;
    }

    public synchronized void allocate(Config options, boolean fancy) {
        int bufferSize = calcBufferSize(options);
        if (buffer != null) {
            buffer.close();
        }
        buffer = new Buffer(bufferSize, fancy, options.usePersistentBuffers);
        clear();
    }

    public synchronized void update(Vector3d camera, long ticks, float tickDelta, Config options, float cloudiness) {
        originX = RandomPath.getPathX(Math.abs(ticks + tickDelta), Math.abs(options.travelSpeed));
        originZ = RandomPath.getPathZ(Math.abs(ticks + tickDelta), Math.abs(options.travelSpeed));

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
            boolean chunkChanged = prevChunkX != chunkX || prevChunkZ != chunkZ;
            boolean optionsChanged = !options.equals(prevTask.options);
            float prevCloudiness = prevTask.cloudiness();
            boolean cloudinessChanged = Math.ceil(cloudiness * 100) != Math.ceil(prevCloudiness * 100);

            boolean bufferCleared = buffer.swapCount() == 0 && queuedTask == null && runningTask == null && (completedTask == null || completedTask == swappedTask);

            if (optionsChanged)
                BetterCloudsStatic.getLogger().debug("Configuration changed, updating geometry");
            updateGeometry = chunkChanged || optionsChanged || cloudinessChanged || bufferCleared;
        } else {
            BetterCloudsStatic.getLogger().debug("No tasks, updating geometry");
            updateGeometry = true;
        }

        if (Debug.generatorForceUpdate) {
            Debug.generatorForceUpdate = false;
            BetterCloudsStatic.getLogger().debug("Forcibly updating geometry");
            updateGeometry = true;
        }

        if (updateGeometry) {
            queuedTask = new Task(chunkX, chunkZ, new Config(options), distance, cloudiness, buffer, sampler);
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

    public synchronized void swap() {
        if (completedTask == null) {
            BetterCloudsStatic.getLogger().warn("swap called with no completed task");
            return;
        }
        if (swappedTask == completedTask) {
            BetterCloudsStatic.getLogger().warn("swap called with swapped task");
            return;
        }

        completedTask.buffer.swap();
        swappedTask = completedTask;

        if (Debug.isProfilingEnabled()) {
            long elapsed = swappedTask.elapsedMs(Util.getMillis());
            ChatUtil.debugChatMessage("profiling.genTimes", elapsed, 1000f / elapsed);
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
        private final Buffer buffer;
        private final Sampler sampler;
        private final AtomicBoolean ran = new AtomicBoolean();
        private final AtomicBoolean cancelled = new AtomicBoolean();
        private final AtomicBoolean completed = new AtomicBoolean();
        private final List<ChunkIndex> chunks = new ArrayList<>();
        private int cloudCount;

        private long startTime;

        public Task(int chunkX, int chunkZ, Config options, float distance, float cloudiness, Buffer buffer, Sampler sampler) {
            this.id = nextId.getAndIncrement();
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.options = options;
            this.distance = distance;
            this.cloudiness = cloudiness;
            this.buffer = buffer;
            this.sampler = sampler;
        }

        public void cancel() {
            synchronized (this) {
                if (completed.get()) return;
                if (cancelled.getAndSet(true)) return;
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
            return buffer.instanceVertexCount();
        }

        public Config options() {
            return options;
        }

        public float distance() {
            return distance;
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
            double spacing = options.spacing;

            int gridMin = -Mth.floor(distance / spacing);
            int gridMax = Mth.ceil(distance / spacing);
            int gridVisibilityRadiusSquared = Mth.ceil((distance + options.sizeXZ) / spacing);
            gridVisibilityRadiusSquared = gridVisibilityRadiusSquared * gridVisibilityRadiusSquared;

            int chunkMin = roundToMultiple(gridMin, options.chunkSize);
            int chunkMax = roundToMultiple(gridMax, options.chunkSize);
            int chunkLength = chunkMax - chunkMin;
            int chunkCount = chunkLength / options.chunkSize;

            int gridOriginX = Mth.floor((chunkX * options.chunkSize) / spacing);
            int gridOriginZ = Mth.floor((chunkZ * options.chunkSize) / spacing);

            int[][][] chunkGridPoints = new int[chunkCount * chunkCount][][];
            // The outer loop generates chunks
            for (int chunkX = chunkMin; chunkX < chunkMax; chunkX += options.chunkSize) {
                for (int chunkZ = chunkMin; chunkZ < chunkMax; chunkZ += options.chunkSize) {
                    int chunkGridMinX = Math.max(chunkX, gridMin);
                    int chunkGridMinZ = Math.max(chunkZ, gridMin);
                    int chunkGridMaxX = Math.min(chunkX + options.chunkSize, gridMax);
                    int chunkGridMaxZ = Math.min(chunkZ + options.chunkSize, gridMax);
                    int chunkGridLengthX = chunkGridMaxX - chunkGridMinX;
                    int chunkGridLengthZ = chunkGridMaxZ - chunkGridMinZ;
                    int chunkIndex = (chunkX - chunkMin) / options.chunkSize + chunkCount * ((chunkZ - chunkMin) / options.chunkSize);
                    chunkGridPoints[chunkIndex] = new int[chunkGridLengthX * chunkGridLengthZ][];

                    // The outer loop generates sample points
                    for (int gridX = chunkGridMinX; gridX < chunkGridMaxX; gridX++) {
                        for (int gridZ = chunkGridMinZ; gridZ < chunkGridMaxZ; gridZ++) {
                            if (options.sparsity > 0 && Sampler.hashToFloat(11, gridX + gridOriginX, gridZ + gridOriginZ) < options.sparsity)
                                continue;
                            if (gridX * gridX + gridZ * gridZ >= gridVisibilityRadiusSquared) {
                                // The point is outside the visible range
                                continue;
                            }

                            int pointIndex = (gridX - chunkGridMinX) + chunkGridLengthX * (gridZ - chunkGridMinZ);
                            chunkGridPoints[chunkIndex][pointIndex] = new int[] { gridX, gridZ };
                        }
                    }
                }
            }

            // Shuffle
            if (options.shuffle) {
                for (int[][] gridPoints : chunkGridPoints) {
                    for (int s = 0; s < gridPoints.length; s++) {
                        int[] tmp = gridPoints[s];
                        if (tmp == null) continue;
                        int d = Sampler.hash(13, tmp[0] + gridOriginX, tmp[1] + gridOriginZ) % gridPoints.length;
                        if (d < 0) d = -d;
                        gridPoints[s] = gridPoints[d];
                        gridPoints[d] = tmp;
                    }
                }
            }

            buffer.clear();

            for (int[][] gridPoints : chunkGridPoints) {
                int chunkCloudIndex = cloudCount;
                float[] bounds = null;
                for (int[] point : gridPoints) {
                    if (point == null) continue;
                    int gridX = point[0], gridZ = point[1];

                    int sampleX = Mth.floor((gridX + gridOriginX) * spacing);
                    int sampleZ = Mth.floor((gridZ + gridOriginZ) * spacing);
                    float value = sampler.sample(sampleX, sampleZ, cloudiness, options.fuzziness, options.samplingScale);
                    if (value <= 0) continue;

                    float x = (float) (sampleX - this.chunkX * options.chunkSize + sampler.randomOffsetX(sampleX, sampleZ) * options.randomPlacement * spacing);
                    float y = options.yRange * value * value * options.pointiness * 0.3f + options.yOffset;
                    float z = (float) (sampleZ - this.chunkZ * options.chunkSize + sampler.randomOffsetZ(sampleX, sampleZ) * options.randomPlacement * spacing);

                    if (bounds == null) {
                        bounds = new float[] { x, y, z, x, y, z };
                    } else {
                        if (x < bounds[0]) bounds[0] = x;
                        if (y < bounds[1]) bounds[1] = y;
                        if (z < bounds[2]) bounds[2] = z;
                        if (x > bounds[3]) bounds[3] = x;
                        if (y > bounds[4]) bounds[4] = y;
                        if (z > bounds[5]) bounds[5] = z;
                    }

                    buffer.put(x, y, z);
                    cloudCount++;
                }

                if (chunkCloudIndex != cloudCount && bounds != null) {
                    AABB boundingBox = new AABB(bounds[0], bounds[1], bounds[2], bounds[3], bounds[4], bounds[5])
                            .move(this.chunkX * options.chunkSize, 0, this.chunkZ * options.chunkSize);
                    chunks.add(new ChunkIndex(chunkCloudIndex, cloudCount - chunkCloudIndex, boundingBox));
                }

                if (cancelled.get()) {
                    synchronized (this) {
                        notify();
                        return;
                    }
                }
            }

            completed.set(true);
        }

        private int roundToMultiple(int n, int base) {
            if (n >= 0) {
                return (n + base - 1) / base * base;
            } else {
                return (n - base + 1) / base * base;
            }
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
