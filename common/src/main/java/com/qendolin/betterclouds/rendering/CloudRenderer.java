package com.qendolin.betterclouds.rendering;

import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.compat.ArsNouveauCompat;
import com.qendolin.betterclouds.config.ConfigManager;
import com.qendolin.betterclouds.generator.ChunkedGenerator;
import com.qendolin.betterclouds.mixin.duck.BiomeManagerDuck;
import com.qendolin.betterclouds.rendering.opengl.Debug;
import com.qendolin.betterclouds.util.ChatUtil;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public abstract class CloudRenderer implements AutoCloseable {
    protected final Minecraft client;
    protected ChunkedGenerator generator;
    protected ClientLevel level;
    protected PerfTimer timer;
    protected float cloudHeight;
    protected boolean closed = false;

    public CloudRenderer(Minecraft client) {
        this.client = client;
        if (GraphicsCompat.isOpenGL)
            this.timer = new PerfTimer();
    }

    public long dayTime() {
        long time = level.getDefaultClockTime();
        if (level.dimensionType().hasFixedTime() && time == 0)
            return 6000;        // noon
        return time % 24000;
    }

    public void reload(ResourceManager resourceManager) {
    }

    public @NonNull PrepareResult checkAndPrepare(Matrix4f viewMat, Matrix4f projMat, int rendererTicks, float tickDelta, Vector3d cam) {
        if (!Objects.equals(level, client.level))
            setLevel(client.level);
        if (closed || level == null)
            return PrepareResult.FALLBACK;

        // Rendering clouds when underwater was making them very visible in unloaded chunks
        if (client.gameRenderer.mainCamera().getFluidInCamera() != FogType.NONE)
            return PrepareResult.NO_RENDER;

        // This doesn't make the Skyweave block work, but it prevents larger issues
        if (ArsNouveauCompat.isSkyTextureCloudsRendering())
            return PrepareResult.NO_RENDER;

        cloudHeight = level.environmentAttributes().getValue(EnvironmentAttributes.CLOUD_HEIGHT, new Vec3(cam.x, cam.y, cam.z)) + ConfigManager.instance().yOffset;
        return prepare(viewMat, projMat, rendererTicks, tickDelta, cam);
    }

    public void checkAndRender(int ticks, float tickDelta, Vector3d cam, Vector3d frustumPos, Frustum frustum) {
        if (level == null)
            return;

        startTiming();
        render(ticks, tickDelta, cam, frustumPos, frustum);
        stopTiming();
    }

    protected @NonNull
    abstract PrepareResult prepare(Matrix4f viewMat, Matrix4f projMat, int rendererTicks, float tickDelta, Vector3d cam);

    protected abstract void render(int ticks, float tickDelta, Vector3d cam, Vector3d frustumPos, Frustum frustum);

    protected abstract void onClose();

    @Override
    public void close() {
        if (closed) {
            BetterCloudsStatic.getLogger().warn(getClass().getSimpleName() + " is already closed, skipping close");
            return;
        }
        try {
            onClose();
            if (timer != null)
                timer.close();
            closed = true;
        } catch (Exception e) {
            BetterCloudsStatic.getLogger().error("Error while closing " + getClass().getSimpleName(), e);
        }
    }

    public long getWorldSeed() {
        if (level == null) {
            BetterCloudsStatic.getLogger().warn("No level when getWorldSeed was called?");
            return 0;
        }
        long seed = ((BiomeManagerDuck) level.getBiomeManager()).better_clouds$biomeSeed();
        BetterCloudsStatic.getLogger().debug("Biome seed: " + seed);
        return seed;
    }

    // Used to be called isFancyMode
    public boolean useCubeClouds() {
        return Minecraft.getInstance().options.getCloudStatus() == CloudStatus.FANCY && ConfigManager.instance().sizeY > 0;
    }

    public PerfTimer timer() {
        return timer;
    }

    protected void stopTiming() {
        if (!Debug.isProfilingEnabled() || timer == null) return;
        timer.stop();

        if (timer.frames() >= Debug.profileInterval) {
            PerfTimer.Stats gpu = PerfTimer.Stats.of(timer.gpu());
            PerfTimer.Stats cpu = PerfTimer.Stats.of(timer.cpu());
            BetterCloudsStatic.getLogger().info("GPU Times (msec):\n" + gpu);
            BetterCloudsStatic.getLogger().info("CPU Times (msec):\n" + cpu);
            ChatUtil.debugChatMessage("profiling.gpuTimes", gpu.formatted());
            ChatUtil.debugChatMessage("profiling.cpuTimes", cpu.formatted());
            timer.reset();
        }
    }

    protected void startTiming() {
        if (!Debug.isProfilingEnabled()) return;
        if (timer == null)
            reloadTimer();
        if (timer != null)
            timer.start();
    }

    public void reloadTimer() {
        deleteTimer();
        if (!Debug.isProfilingEnabled()) return;
        timer = new PerfTimer();
    }

    public void deleteTimer() {
        if (timer != null) timer.close();
        timer = null;
    }

    public void setLevel(ClientLevel level) {
        this.level = level;
    }
}
