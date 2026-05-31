package com.qendolin.betterclouds.rendering;

import com.qendolin.betterclouds.rendering.opengl.Debug;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.jspecify.annotations.NonNull;

public abstract class CloudRenderer implements AutoCloseable {
    protected final Minecraft client;
    protected ClientLevel level;
    protected PerfTimer timer = new PerfTimer();

    public CloudRenderer(Minecraft client) {
        this.client = client;
    }

    public void setLevel(ClientLevel level) {
        this.level = level;
    }

    public abstract void reload(ResourceManager resourceManager);

    @NonNull
    public abstract PrepareResult prepare(Matrix4f viewMat, Matrix4f projMat, int ticks, float tickDelta, Vector3d cam);

    public abstract void render(int ticks, float tickDelta, Vector3d cam, Vector3d frustumPos, Frustum frustum);

    @Override
    public void close() {
        timer.close();
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
}
