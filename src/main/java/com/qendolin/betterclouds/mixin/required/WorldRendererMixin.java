package com.qendolin.betterclouds.mixin.required;

import com.qendolin.betterclouds.BetterClouds;
import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.clouds.Debug;
import com.qendolin.betterclouds.clouds.Renderer;
import com.qendolin.betterclouds.config.ConfigManager;
import com.qendolin.betterclouds.duck.WorldRendererDuck;
import com.qendolin.betterclouds.renderdoc.RenderDoc;
import com.qendolin.betterclouds.telemetry.IssueReportManager;
import com.qendolin.betterclouds.util.RenderHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ResourceManager;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=1.21.3 {
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
//?} else {
/*import net.minecraft.client.util.math.MatrixStack;
*///?}

//? if >=1.21.5 {
import com.mojang.blaze3d.systems.RenderSystem;
//?}

//? if >=1.21.6 {
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.util.ObjectAllocator;
//?}

import static com.qendolin.betterclouds.compat.GLCompat.glCompat;
import static com.qendolin.betterclouds.compat.ProfilerWrapper.getProfiler;

@Mixin(value = WorldRenderer.class, priority = 900)
public abstract class WorldRendererMixin implements WorldRendererDuck {

    @Unique
    private final Vector3d tempVector = new Vector3d();

    @Unique
    private Renderer cloudRenderer;
    @Shadow
    private Frustum frustum;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(MinecraftClient client, EntityRenderDispatcher entityRenderDispatcher, BlockEntityRenderDispatcher blockEntityRenderDispatcher, BufferBuilderStorage bufferBuilders, CallbackInfo ci) {
        if (glCompat.isIncompatible()) return;
        cloudRenderer = new Renderer(client);
    }

    @Shadow
    private @Nullable Frustum capturedFrustum;

    @Shadow
    private @Nullable ClientWorld world;

    @Shadow
    private int ticks;

    //? if >=1.21.3 {
    @Shadow
    public abstract Frustum getCapturedFrustum();

    @Shadow
    @Final
    private DefaultFramebufferSet framebufferSet;
    //?} else {
    /*@Shadow
    @Final
    private Vector3d capturedFrustumPosition;
    *///?}


    @Shadow @Final private MinecraftClient client;

    @Override
    public Renderer betterclouds$getRenderer() {
        return cloudRenderer;
    }

    @Inject(at = @At("TAIL"), method = "reload(Lnet/minecraft/resource/ResourceManager;)V")
    private void onReload(ResourceManager manager, CallbackInfo ci) {
        if (!BetterClouds.isInitialized()) return;
        if (glCompat.isIncompatible()) return;
        try {
            if (cloudRenderer != null)
                cloudRenderer.reload(manager);
        } catch (Throwable e) {
            if(!IssueReportManager.handle(e, "An error occurred while reloading resources: " + e.getMessage()))
                throw e;
        }
    }

    @Inject(at = @At("TAIL"), method = "setWorld")
    private void onSetWorld(ClientWorld world, CallbackInfo ci) {
        if (cloudRenderer != null) cloudRenderer.setWorld(world);
    }

    @Unique
    private Vector3d getCapturedFrustumPosition() {
        //? if >=1.21.3 {
        return new Vector3d(getCapturedFrustum().x, getCapturedFrustum().y, getCapturedFrustum().z);
        //?} else {
        /*return new Vector3d(capturedFrustumPosition);
        *///?}
    }

    //? if >=1.21.6 {
    @Inject(at = @At("HEAD"), method = "render")
    private void captureViewAndProjectionMatrix(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, Matrix4f positionMatrix, Matrix4f projectionMatrix, GpuBufferSlice fog, Vector4f fogColor, boolean shouldRenderSky, CallbackInfo ci) {
        RenderHelper.setProjectionMatrix(projectionMatrix);
        RenderHelper.setViewMatrix(positionMatrix);
    }
    //?}

    //? if >=1.21.5 {
    @Inject(at = @At("HEAD"), method = "renderClouds", cancellable = true)
    private void renderClouds(FrameGraphBuilder frameGraphBuilder, CloudRenderMode _mode, Vec3d cameraPos, float _ticks, int _color, float _cloudHeight, CallbackInfo ci) {
        double camX = cameraPos.x, camY = cameraPos.y, camZ = cameraPos.z;
        float tickDelta = MathHelper.fractionalPart(_ticks);
        Matrix4f viewMat = RenderHelper.getViewMatrix();
        Matrix4f projMat = RenderHelper.getProjectionMatrix();
    //?} elif >=1.21.3 {
    /*@Inject(at = @At("HEAD"), method = "renderClouds", cancellable = true)
    private void renderClouds(FrameGraphBuilder frameGraphBuilder, Matrix4f viewMat, Matrix4f projMat, CloudRenderMode _mode, Vec3d cameraPos, float _ticks, int _color, float _cloudHeight, CallbackInfo ci) {
        double camX = cameraPos.x, camY = cameraPos.y, camZ = cameraPos.z;
        float tickDelta = MathHelper.fractionalPart(_ticks);
    *///?} elif >=1.20.6 {
    /*@Inject(at = @At("HEAD"), method = "renderClouds(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;FDDD)V", cancellable = true)
    private void renderClouds(MatrixStack matrices, Matrix4f viewMat, Matrix4f projMat, float tickDelta, double camX, double camY, double camZ, CallbackInfo ci) {
    *///?} else {
    /*@Inject(at = @At("HEAD"), method = "renderClouds(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FDDD)V", cancellable = true)
    private void renderClouds(MatrixStack matrices, Matrix4f projMat, float tickDelta, double camX, double camY, double camZ, CallbackInfo ci) {
        Matrix4f viewMat = matrices.peek().getPositionMatrix();
    *///?}
        if (cloudRenderer == null) return;
        if (glCompat.isIncompatible()) return;
        if (world == null) return;
        if (!ConfigManager.instance().enabledDimensions.contains(world.getDimensionEntry().getKey().orElse(null))) return;
        if (!BetterClouds.isEnabled()) return;

        getProfiler().push(BetterCloudsStatic.MODID);
        glCompat.pushDebugGroupDev("Better Clouds");

        Vector3d cam = tempVector.set(camX, camY, camZ);
        Frustum frustum = this.frustum;
        Vector3d frustumPos = cam;
        if (capturedFrustum != null) {
            frustumPos = getCapturedFrustumPosition();
            frustum = capturedFrustum;
            frustum.setPosition(frustumPos.x, frustumPos.y, frustumPos.z);
        }

        int ticks = this.ticks;
        if (Debug.animationPause >= 0) {
            if (Debug.animationPause == 0) Debug.animationPause = ticks;
            else ticks = Debug.animationPause;
            tickDelta = 0;
        }

        try {
            Renderer.PrepareResult prepareResult = cloudRenderer.prepare(viewMat, projMat, ticks, tickDelta, cam);
            if (RenderDoc.isFrameCapturing())
                glCompat.debugMessage("renderer prepare returned " + prepareResult.name());

            if (prepareResult != Renderer.PrepareResult.FALLBACK)
                ci.cancel();

            // Note to self: do not use return
            if (prepareResult == Renderer.PrepareResult.RENDER) {
                //? if >=1.21.3 {
                var renderPass = frameGraphBuilder.createPass("clouds");
                if (framebufferSet.cloudsFramebuffer != null) {
                    framebufferSet.cloudsFramebuffer = renderPass.transfer(framebufferSet.cloudsFramebuffer);
                } else {
                    framebufferSet.mainFramebuffer = renderPass.transfer(framebufferSet.mainFramebuffer);
                }

                final var fticks = ticks;
                final var ftickDelta = tickDelta;
                final var fcam = cam;
                final var ffrustumPos = frustumPos;
                final var ffrustum = frustum;
                renderPass.setRenderer(() -> {
                    try {
                        getProfiler().push("clouds");
                        glCompat.pushDebugGroupDev("Better Clouds");
                        cloudRenderer.render(fticks, ftickDelta, fcam, ffrustumPos, ffrustum);
                        getProfiler().pop();
                        glCompat.popDebugGroupDev();
                    } catch (Throwable e) {
                        if(!IssueReportManager.handle(e, "An error occurred while rendering: " + e.getMessage()))
                            throw e;
                    }
                });
                //?} else {
                /*cloudRenderer.render(ticks, tickDelta, cam, frustumPos, frustum);
                 *///?}
            }
        } catch (Throwable e) {
            if(!IssueReportManager.handle(e, "An error occurred while rendering: " + e.getMessage()))
                throw e;
        }

        getProfiler().pop();
        glCompat.popDebugGroupDev();
    }


    @Inject(at = @At("HEAD"), method = "close")
    private void close(CallbackInfo ci) {
        if (cloudRenderer != null) cloudRenderer.close();
    }
}
