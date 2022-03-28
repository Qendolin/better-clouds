package com.qendolin.betterclouds.clouds;

import com.qendolin.betterclouds.Config;
import net.minecraft.util.math.MathHelper;

import java.util.concurrent.CompletableFuture;

public class Generator implements AutoCloseable {
    private Buffer buffer;
    private final Sampler sampler = new Sampler();

    private int drawCloudCount;
    private int writeCloudCount;
    private int chunkX;
    private int chunkZ;
    private float originX;
    private float originZ;

    private boolean generating = false;
    private boolean finished = false;

    public int count() {
        return drawCloudCount;
    }

    public float originX() {
        return originX;
    }

    public float originZ() {
        return originZ;
    }

    public boolean finished() {
        synchronized (this) {
            return finished;
        }
    }

    public int baseMeshVertexCount() {
        return this.buffer.baseMeshVertexCount();
    }

    public void generate(Config options, float cloudiness, boolean forceSync) {
        synchronized (this) {
            if(generating) return;
            if(options.async && !forceSync) {
                final Config optionsSnap = new Config(options);
                final int chunkXSnap = this.chunkX;
                final int chunkZSnap = this.chunkZ;
                generating = true;
                CompletableFuture.runAsync(() -> generateSync(optionsSnap, chunkXSnap, chunkZSnap, cloudiness))
                    .whenComplete((unused, throwable) -> {
                        synchronized (this) {
                            finished = true;
                            generating = false;
                        }
                    });
            } else {
                generateSync(options, this.chunkX, this.chunkZ, cloudiness);
                finished = true;
            }
        }
    }

    public void swap() {
        synchronized (this) {
            if (!finished) {
                return;
            }
            finished = false;
            drawCloudCount = writeCloudCount;
            this.buffer.swap();
        }
    }

    private void generateSync(Config options, int chunkX, int chunkZ, float cloudiness) {
        writeCloudCount = 0;
        buffer.clear();

        int distance = options.blockDistance();
        float spacing = options.spacing;

        int halfGridPointsC = MathHelper.ceil(distance / spacing);
        int halfGridPointsF = MathHelper.floor(distance / spacing);

        int originX = chunkX * options.chunkSize;
        int originZ = chunkZ * options.chunkSize;
        float alignedOriginX = MathHelper.floor(originX / spacing) * spacing;
        float alignedOriginZ = MathHelper.floor(originZ / spacing) * spacing;

        // TODO: Remove debug stuff here
//        List<Float> heights = new ArrayList<>();
//        float sum = 0;
//        float min = Float.MAX_VALUE;
//        float max = -Float.MAX_VALUE;

        for (int gridX = -halfGridPointsF; gridX < halfGridPointsC; gridX++) {
            for (int gridZ = -halfGridPointsF; gridZ < halfGridPointsC; gridZ++) {
                int sampleX = MathHelper.floor(gridX * spacing + alignedOriginX);
                int sampleZ = MathHelper.floor(gridZ * spacing + alignedOriginZ);
                float value = sampler.sample(sampleX, sampleZ, cloudiness, options.fuzziness);
                if (value <= 0) continue;

                // TODO: cloudPointiness vlaue
                float x = sampleX + sampler.jitterX(sampleX, sampleZ) * options.jitter * spacing;
                float y = options.spreadY * value * value;
                float z = sampleZ + sampler.jitterZ(sampleX, sampleZ) * options.jitter * spacing;
//                heights.add(value * value);
//                sum += value * value;
//                min = Math.min(min, value * value);
//                max = Math.max(max, value * value);

                buffer.put(x, y, z);
                writeCloudCount++;
            }
        }

        // FIXME: mean / avg / max is 0.01, 0.12, o.68 but should be 0.5, 0.5, 1.0
        // If someone knows, please tell
//        heights.sort(Float::compareTo);
//        Main.LOGGER.info("Avg: {} Min: {} Max: {} Mean: {}", sum/heights.size(), min, max, heights.get(heights.size()/2));
    }

    public boolean reallocate(Config options, boolean fancy) {
        int bufferSize = calcBufferSize(options);

        if(buffer.hasChanged(bufferSize, fancy, options.usePersistentBuffers)) {
            buffer.close();
            buffer = new Buffer(bufferSize, fancy, options.usePersistentBuffers);
            return true;
        }
        return false;
    }

    public void allocate(Config options, boolean fancy) {
        int bufferSize = calcBufferSize(options);
        if(buffer != null) {
            buffer.close();
        }
        buffer = new Buffer(bufferSize, fancy, options.usePersistentBuffers);
    }

    private static int calcBufferSize(Config options) {
        int distance = options.blockDistance();
        return MathHelper.floor(distance / options.spacing)
                + MathHelper.ceil(distance / options.spacing);
    }

    public boolean update(float cameraX, float cameraZ, float ticksTotal, Config options) {
        originX = -ticksTotal * options.windSpeed;
        originZ = 0;
        // The origin is at the player
        float worldOriginX = cameraX - this.originX;
        float worldOriginZ = cameraZ - this.originZ;

        int chunkX = floorCloudChunk(worldOriginX, options.chunkSize);
        int chunkZ = floorCloudChunk(worldOriginZ, options.chunkSize);

        if(chunkX != this.chunkX || chunkZ != this.chunkZ) {
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            return true;
        }
        return false;
    }

    private static int floorCloudChunk(float coord, int chunkSize) {
        return (int) coord / chunkSize;
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
}
