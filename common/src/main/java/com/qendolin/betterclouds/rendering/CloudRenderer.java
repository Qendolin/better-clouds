package com.qendolin.betterclouds.rendering;

import com.qendolin.betterclouds.config.ConfigManager;
import com.qendolin.betterclouds.mixin.duck.BiomeManagerDuck;
import com.qendolin.betterclouds.rendering.opengl.Debug;
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
    protected PerfTimer timer = new PerfTimer();
    protected float cloudHeight;

    public CloudRenderer(Minecraft client) {
        this.client = client;
    }

    public void setLevel(ClientLevel level) {
        this.level = level;
    }

    public void reload(ResourceManager resourceManager) {
    }

    @NonNull
    public abstract PrepareResult prepare(Matrix4f viewMat, Matrix4f projMat, int ticks, float tickDelta, Vector3d cam);

    public abstract void render(int ticks, float tickDelta, Vector3d cam, Vector3d frustumPos, Frustum frustum);

    @Override
    public void close() {
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
        cloudHeight = level.environmentAttributes().getValue(EnvironmentAttributes.CLOUD_HEIGHT, new Vec3(cam.x, cam.y, cam.z));
    }
}
