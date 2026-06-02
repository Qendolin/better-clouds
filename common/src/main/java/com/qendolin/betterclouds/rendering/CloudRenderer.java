package com.qendolin.betterclouds.rendering;

import com.qendolin.betterclouds.BetterCloudsStatic;
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
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.jspecify.annotations.NonNull;

public abstract class CloudRenderer implements AutoCloseable {
    protected final Minecraft client;
    protected ClientLevel level;
    protected PerfTimer timer;
    protected float cloudHeight;

    protected boolean closed = false;

    public CloudRenderer(Minecraft client) {
        this.client = client;
        if (GraphicsCompat.isOpenGL)
            this.timer = new PerfTimer();
    }

    public void setLevel(ClientLevel level) {
        this.level = level;
    }

    public void reload(ResourceManager resourceManager) {
    }

    @NonNull
    public abstract PrepareResult prepare(Matrix4f viewMat, Matrix4f projMat, int rendererTicks, float tickDelta, Vector3d cam);

    public abstract void render(int ticks, float tickDelta, Vector3d cam, Vector3d frustumPos, Frustum frustum);

    public abstract ChunkedGenerator generator();

    @Override
    public void close() {
        closed = true;
        if (timer != null)
            timer.close();
    }

    public long getWorldSeed() {
        if (level == null) return 0;
        return ((BiomeManagerDuck) level.getBiomeManager()).better_clouds$biomeSeed();
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

    public void updateCloudHeight(Vector3d cam) {
        cloudHeight = level.environmentAttributes().getValue(EnvironmentAttributes.CLOUD_HEIGHT, new Vec3(cam.x, cam.y, cam.z)) + ConfigManager.instance().yOffset;
    }

    public void onConfigSave() {

    }
}
