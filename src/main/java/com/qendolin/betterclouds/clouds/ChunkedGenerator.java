package com.qendolin.betterclouds.clouds;

import com.qendolin.betterclouds.Config;
import com.qendolin.betterclouds.Main;
import net.minecraft.util.Util;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ChunkedGenerator implements AutoCloseable {
    private double originX;
    private double originZ;

    private Buffer buffer;
    private final Sampler sampler = new Sampler();

    @Nullable
    private Task queuedTask;
    @Nullable
    private Task runningTask;
    @Nullable
    private Task completedTask;
    @Nullable
    private Task swappedTask;

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

    public synchronized void clear() {
        queuedTask = null;
        if(runningTask != null) runningTask.cancel();
        runningTask = null;
        completedTask = null;
        swappedTask = null;
    }

    public synchronized List<ChunkIndex> chunks() {
        if(swappedTask == null) return List.of();
        return swappedTask.chunks();
    }

    public synchronized int instanceVertexCount() {
        if(swappedTask == null) return 0;
        return swappedTask.instanceVertexCount();
    }

    public double originX() {
        return originX;
    }

    public double originZ() {
        return originZ;
    }

    public synchronized double renderOriginX(double cameraX) {
        if(swappedTask == null) return 0;
        return swappedTask.chunkX() * swappedTask.options().chunkSize - cameraX + originX;
    }

    public synchronized double renderOriginZ(double cameraZ) {
        if(swappedTask == null) return 0;
        return swappedTask.chunkZ() * swappedTask.options().chunkSize - cameraZ + originZ;
    }

    public synchronized int cloudCount() {
        if(swappedTask == null) return 0;
        return swappedTask.cloudCount();
    }

    public synchronized Config config() {
        if(swappedTask == null) return null;
        return swappedTask.options;
    }

    public synchronized boolean generating() {
        return runningTask != null;
    }

    @Override
    public void close() {
        buffer.close();
    }

    public void bind() {
        buffer.bind();
    }

    public void unbind() {
        buffer.unbind();
    }

    public synchronized boolean reallocateIfStale(Config options, boolean fancy) {
        int bufferSize = calcBufferSize(options);

        if(buffer.hasChanged(bufferSize, fancy, options.usePersistentBuffers)) {
            buffer.close();
            buffer = new Buffer(bufferSize, fancy, options.usePersistentBuffers);
            clear();
            return true;
        }
        return false;
    }

    public synchronized void allocate(Config options, boolean fancy) {
        int bufferSize = calcBufferSize(options);
        if(buffer != null) {
            buffer.close();
        }
        buffer = new Buffer(bufferSize, fancy, options.usePersistentBuffers);
        clear();
    }

    private static int calcBufferSize(Config options) {
        int distance = options.blockDistance();
        return MathHelper.floor(distance / options.spacing)
            + MathHelper.ceil(distance / options.spacing);
    }

    public synchronized void update(Vec3d camera, float timeDelta, Config options, float cloudiness) {
        originX -= timeDelta * options.travelSpeed;
        originZ = 0;
        double worldOriginX = camera.x - this.originX;
        double worldOriginZ = camera.z - this.originZ;

        int chunkX = floorCloudChunk(worldOriginX, options.chunkSize);
        int chunkZ = floorCloudChunk(worldOriginZ, options.chunkSize);

        boolean updateGeometry;
        if(queuedTask != null || runningTask != null || completedTask != null) {
            Task prevTask = queuedTask == null ? (runningTask == null ? completedTask : runningTask) : queuedTask;
            int prevChunkX = prevTask.chunkX();
            int prevChunkZ = prevTask.chunkZ();
            boolean chunkChanged = prevChunkX != chunkX || prevChunkZ != chunkZ;

            Config prevOptions = prevTask.options();
            boolean optionsChanged = options.fuzziness != prevOptions.fuzziness
                || options.chunkSize != prevOptions.chunkSize
                || options.yRange != prevOptions.yRange
                || options.sparsity != prevOptions.sparsity
                || options.spacing != prevOptions.spacing
                || options.jitter != prevOptions.jitter
                || options.distance != prevOptions.distance
                || options.samplingScale != prevOptions.samplingScale
                || options.shuffle != prevOptions.shuffle;

            float prevCloudiness = prevTask.cloudiness();
            boolean cloudinessChanged = Math.ceil(cloudiness * 100) != Math.ceil(prevCloudiness * 100);

            boolean bufferCleared = buffer.swapCount() == 0 && queuedTask == null && runningTask == null && (completedTask == null || completedTask == swappedTask);

            updateGeometry = chunkChanged || optionsChanged || cloudinessChanged || bufferCleared;
        } else {
            updateGeometry = true;
        }

        if(updateGeometry) {
            queuedTask = new Task(chunkX, chunkZ, new Config(options), cloudiness, buffer, sampler);
        }
    }

    public synchronized void generate() {
        if(queuedTask == null) {
            Main.LOGGER.warn("generate called with no queued task");
            return;
        }
        if(runningTask != null) {
            runningTask.cancel();
        }
        runningTask = queuedTask;
        queuedTask = null;

        if(runningTask.ran()) {
            Main.LOGGER.warn("Queued generator task #{} already ran", runningTask.id());
        }

        final Task boundTask = runningTask;
        CompletableFuture.runAsync(runningTask::run)
        .whenComplete((unused, throwable) -> {
            synchronized (this) {
                if(throwable != null) {
                    Main.LOGGER.error("Generator task #{} ran with error", runningTask.id(), throwable);
                }

                if(boundTask != runningTask) {
                    if(boundTask.completed()) {
                        Main.LOGGER.warn("Generator task #{} completed but task #{} was expected", boundTask.id(), runningTask.id());
                    } else if(!boundTask.cancelled() && throwable == null) {
                        Main.LOGGER.warn("Generator task #{} ran without error, completion or cancellation", boundTask.id());
                    }
                } else {
                    if(boundTask.completed()) {
                        completedTask = runningTask;
                    } else if(!boundTask.cancelled() && throwable == null) {
                        Main.LOGGER.warn("Generator task #{} ran without error, completion or cancellation", boundTask.id());
                    }
                    runningTask = null;
                }
            }
        });
    }

    public synchronized void swap() {
        if(completedTask == null) {
            Main.LOGGER.warn("swap called with no completed task");
            return;
        }
        if(swappedTask == completedTask) {
            Main.LOGGER.warn("swap called with swapped task");
            return;
        }

        completedTask.buffer.swap();
        swappedTask = completedTask;

        if(Main.isProfilingEnabled()) {
            long elapsed = swappedTask.elapsedMs(Util.getMeasuringTimeMs());
            Main.debugChatMessage(String.format("§5Gen Times§r: %d §7ms§r || %.3f §7per second§r", elapsed, 1000f/elapsed));
        }
    }

    private static class Task {

        private static final AtomicInteger nextId = new AtomicInteger(1);
        private final int id;
        private final int chunkX;
        private final int chunkZ;
        private final Config options;
        private final float cloudiness;
        private final Buffer buffer;
        private final Sampler sampler;
        private final AtomicBoolean ran = new AtomicBoolean();
        private final AtomicBoolean cancelled = new AtomicBoolean();
        private final AtomicBoolean completed = new AtomicBoolean();
        private final List<ChunkIndex> chunks = new ArrayList<>();
        private int cloudCount;

        private long startTime;

        public Task(int chunkX, int chunkZ, Config options, float cloudiness, Buffer buffer, Sampler sampler) {
            this.id = nextId.getAndIncrement();
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.options = options;
            this.cloudiness = cloudiness;
            this.buffer = buffer;
            this.sampler = sampler;
        }

        public void cancel() {
            synchronized (this) {
                if(completed.get()) return;
                if(cancelled.getAndSet(true)) return;
                Main.LOGGER.debug("Generator task #{} cancelled", id);
                try {
                    wait();
                } catch (InterruptedException e) {
                    Main.LOGGER.error("Generator task #{} interrupted after cancelled", id, e);
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
        public float cloudiness() {
            return cloudiness;
        }
        public List<ChunkIndex> chunks() {
            return chunks;
        }
        public boolean ran() {
            return ran.get();
        }
        public boolean cancelled() {
            return cancelled.get();
        }
        public long elapsedMs(long now) {
            if(startTime == 0) return 0;
            return now - startTime;
        }

        private int roundToMultiple(int n, int base) {
            if (n >= 0) {
                return (n + base - 1) / base * base;
            } else {
                return (n - base + 1) / base * base;
            }
        }

        private int hash(int prime, int... values) {
            int hash = prime;
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

        // https://stackoverflow.com/a/17479300/7448536
        // Distribution is very uniform from my testing
        private float hashToFloat(int prime, int... values) {
            int hash = hash(prime, values);

            int ieeeMantissa = 0x007FFFFF;
            int ieeeOne = 0x3F800000;

            hash &= ieeeMantissa;
            hash |= ieeeOne;
            float f = Float.intBitsToFloat(hash);
            return f - 1;
        }

        public void run() {
            synchronized (this) {
                if(ran.getAndSet(true) || cancelled.get()) return;
            }
            startTime = Util.getMeasuringTimeMs();

            int distance = options.blockDistance();
            double spacing = options.spacing;

            int gridMin = -MathHelper.floor(distance / spacing);
            int gridMax = MathHelper.ceil(distance / spacing);
            int gridVisibilityRadiusSquared = MathHelper.ceil((distance + options.sizeXZ) / spacing);
            gridVisibilityRadiusSquared = gridVisibilityRadiusSquared * gridVisibilityRadiusSquared;

            int chunkMin = roundToMultiple(gridMin, options.chunkSize);
            int chunkMax = roundToMultiple(gridMax, options.chunkSize);
            int chunkLength = chunkMax - chunkMin;
            int chunkCount = chunkLength / options.chunkSize;

            int gridOriginX = MathHelper.floor((chunkX * options.chunkSize) / spacing);
            int gridOriginZ = MathHelper.floor((chunkZ * options.chunkSize) / spacing);

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
                    int chunkIndex = (chunkX-chunkMin) / options.chunkSize + chunkCount * ((chunkZ-chunkMin) / options.chunkSize);
                    chunkGridPoints[chunkIndex] = new int[chunkGridLengthX * chunkGridLengthZ][];

                    // The outer loop generates sample points
                    for (int gridX = chunkGridMinX; gridX < chunkGridMaxX; gridX++) {
                        for (int gridZ = chunkGridMinZ; gridZ < chunkGridMaxZ; gridZ++) {
                            if(options.sparsity > 0 && hashToFloat(11, gridX + gridOriginX, gridZ + gridOriginZ) < options.sparsity)
                                continue;
                            if(gridX * gridX + gridZ * gridZ >= gridVisibilityRadiusSquared) {
                                // The point is outside the visible range
                                continue;
                            }

                            int pointIndex = (gridX-chunkGridMinX) + chunkGridLengthX * (gridZ-chunkGridMinZ);
                            chunkGridPoints[chunkIndex][pointIndex] = new int[]{gridX, gridZ};
                        }
                    }
                }
            }

            // Shuffle
            if(options.shuffle) {
                // TODO: I'm not sure if I still need this
                for (int[][] gridPoints : chunkGridPoints) {
                    for (int s = 0; s < gridPoints.length; s++) {
                        int[] tmp = gridPoints[s];
                        if(tmp == null) continue;
                        int d = hash(13, tmp[0] + gridOriginX, tmp[1] + gridOriginZ) % gridPoints.length;
                        if(d < 0) d = -d;
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
                    if(point == null) continue;
                    int gridX = point[0], gridZ = point[1];

                    int sampleX = MathHelper.floor((gridX + gridOriginX) * spacing);
                    int sampleZ = MathHelper.floor((gridZ + gridOriginZ) * spacing);
                    float value = sampler.sample(sampleX, sampleZ, cloudiness, options.fuzziness, options.samplingScale);
                    if (value <= 0) continue;

                    float x = (float) (sampleX - this.chunkX * options.chunkSize + sampler.jitterX(sampleX, sampleZ) * options.jitter * spacing);
                    // TODO: cloudPointiness value
                    float y = options.yRange * value * value;
                    float z = (float) (sampleZ - this.chunkZ * options.chunkSize + sampler.jitterZ(sampleX, sampleZ) * options.jitter * spacing);

                    if(bounds == null) {
                        bounds = new float[]{x, y, z, x, y, z};
                    } else {
                        if(x < bounds[0]) bounds[0] = x;
                        if(y < bounds[1]) bounds[1] = y;
                        if(z < bounds[2]) bounds[2] = z;
                        if(x > bounds[3]) bounds[3] = x;
                        if(y > bounds[4]) bounds[4] = y;
                        if(z > bounds[5]) bounds[5] = z;
                    }

                    buffer.put(x, y, z);
                    cloudCount++;
                }

                if(chunkCloudIndex != cloudCount && bounds != null) {
                    Box boundingBox = new Box(bounds[0], bounds[1], bounds[2], bounds[3], bounds[4], bounds[5])
                        .offset(this.chunkX * options.chunkSize, 0, this.chunkZ * options.chunkSize);
                    chunks.add(new ChunkIndex(chunkCloudIndex, cloudCount-chunkCloudIndex, boundingBox));
                }

                if(cancelled.get()) {
                    synchronized (this) {
                        notify();
                        return;
                    }
                }
            }

            completed.set(true);
        }
    }

    public static final class ChunkIndex {
        private final int start;
        private final int count;
        private final Box bounds;

        private Box cachedBounds;
        private float lastCloudsHeight;
        private float lastSizeXZ;
        private float lastSizeY;

        public ChunkIndex(int start, int count, Box bounds) {
            this.start = start;
            this.count = count;
            this.bounds = bounds;
        }

        public Box bounds(float cloudsHeight, float sizeXZ, float sizeY) {
            if(cloudsHeight == lastCloudsHeight && sizeXZ == lastSizeXZ && sizeY == lastSizeY) return cachedBounds;

            cachedBounds = bounds.offset(0, cloudsHeight, 0).expand(sizeXZ, sizeY, sizeXZ);
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
