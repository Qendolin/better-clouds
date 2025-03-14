package com.qendolin.betterclouds.mixin;

import com.qendolin.betterclouds.Main;
import com.qendolin.betterclouds.clouds.Debug;
import com.qendolin.betterclouds.clouds.Renderer;
import com.qendolin.betterclouds.compat.Telemetry;
import com.qendolin.betterclouds.renderdoc.RenderDoc;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ResourceManager;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.lwjgl.opengl.GL32;
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

import static com.qendolin.betterclouds.Main.glCompat;
import static com.qendolin.betterclouds.compat.ProfilerWrapper.getProfiler;

@Mixin(value = WorldRenderer.class, priority = 900)
public abstract class WorldRendererMixin {

    @Unique
    private final Vector3d tempVector = new Vector3d();

    @Unique
    private Renderer cloudRenderer;
    @Shadow
    private Frustum frustum;

    @Unique
    private double profTimeAcc;
    @Unique
    private int profFrames;

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
    @Shadow public abstract Frustum getCapturedFrustum();

    @Shadow @Final private DefaultFramebufferSet framebufferSet;
    //?} else {
    /*@Shadow
    @Final
    private Vector3d capturedFrustumPosition;
    *///?}

    @Inject(at = @At("TAIL"), method = "reload(Lnet/minecraft/resource/ResourceManager;)V")
    private void onReload(ResourceManager manager, CallbackInfo ci) {
        if (!Main.initialized()) return;
        if (glCompat.isIncompatible()) return;
        try {
            if (cloudRenderer != null) cloudRenderer.reload(manager);
        } catch (Exception e) {
            Telemetry.INSTANCE.sendUnhandledException(e);
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

    //? if >=1.21.3 {
    @Inject(at = @At("HEAD"), method = "renderClouds", cancellable = true)
    private void renderClouds(FrameGraphBuilder frameGraphBuilder, Matrix4f viewMat, Matrix4f projMat, CloudRenderMode renderMode, Vec3d cameraPos, float _ticks, int _color, float _cloudHeight, CallbackInfo ci) {
        double camX = cameraPos.x, camY = cameraPos.y, camZ = cameraPos.z;
        float tickDelta = MathHelper.fractionalPart(_ticks);
    //?} elif >=1.20.6 {
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
        if (!Main.getConfig().enabledDimensions.contains(world.getDimensionEntry().getKey().orElse(null))) return;
        if (!Main.getConfig().enabled) return;

        getProfiler().push(Main.MODID);
        glCompat.pushDebugGroupDev("Better Clouds");

        Vector3d cam = tempVector.set(camX, camY, camZ);
        Frustum frustum = this.frustum;
        Vector3d frustumPos = cam;
        if (capturedFrustum != null) {
            frustumPos = getCapturedFrustumPosition();
            frustum = capturedFrustum;
            frustum.setPosition(frustumPos.x, frustumPos.y, frustumPos.z);
        }

        if (Main.isProfilingEnabled()) GL32.glFinish();
        long startTime = System.nanoTime();

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
            if (prepareResult == Renderer.PrepareResult.RENDER) {
                ci.cancel();

                //? if >=1.21.3 {
                RenderPass renderPass = frameGraphBuilder.createPass("clouds");
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
                    glCompat.pushDebugGroupDev("Better Clouds");
                    cloudRenderer.render(fticks, ftickDelta, fcam, ffrustumPos, ffrustum);
                    glCompat.popDebugGroupDev();
                });
                //?} else {
                /*cloudRenderer.render(ticks, tickDelta, cam, frustumPos, frustum);
                *///?}

            } else if (prepareResult == Renderer.PrepareResult.NO_RENDER) {
                ci.cancel();
            }
        } catch (Exception e) {
            Telemetry.INSTANCE.sendUnhandledException(e);
            throw e;
        }

        if (Main.isProfilingEnabled()) {
            GL32.glFinish();
            profTimeAcc += (System.nanoTime() - startTime) / 1e6;
            profFrames++;
            if (profFrames >= Debug.profileInterval) {
                Main.debugChatMessage("profiling.cpuTimes", profTimeAcc / profFrames);
                profFrames = 0;
                profTimeAcc = 0;
            }
        }

        getProfiler().pop();
        glCompat.popDebugGroupDev();
    }


    @Inject(at = @At("HEAD"), method = "close")
    private void close(CallbackInfo ci) {
        if (cloudRenderer != null) cloudRenderer.close();
    }
}
