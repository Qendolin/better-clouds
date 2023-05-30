package com.qendolin.betterclouds.mixin;

import com.qendolin.betterclouds.Main;
import com.qendolin.betterclouds.clouds.Debug;
import com.qendolin.betterclouds.clouds.Renderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ResourceManager;
import net.minecraft.world.dimension.DimensionTypes;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.lwjgl.opengl.GL32;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.qendolin.betterclouds.Main.glCompat;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    private Renderer cloudRenderer;
    @Shadow
    private Frustum frustum;

    private double profTimeAcc;
    private int profFrames;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(MinecraftClient client, EntityRenderDispatcher entityRenderDispatcher, BlockEntityRenderDispatcher blockEntityRenderDispatcher, BufferBuilderStorage bufferBuilders, CallbackInfo ci) {
        if(glCompat.isIncompatible()) return;
        cloudRenderer = new Renderer(client);
    }

    @Shadow
    private int ticks;

    @Shadow private @Nullable Frustum capturedFrustum;

    @Shadow @Final private Vector3d capturedFrustumPosition;

    @Shadow @Final private MinecraftClient client;

    @Shadow private @Nullable ClientWorld world;

    @Inject(at = @At("TAIL"), method = "reload(Lnet/minecraft/resource/ResourceManager;)V")
    private void onReload(ResourceManager manager, CallbackInfo ci) {
        if(glCompat.isIncompatible()) return;
        if(cloudRenderer != null) cloudRenderer.reload(manager);
    }

    @Inject(at = @At("TAIL"), method = "setWorld")
    private void onSetWorld(ClientWorld world, CallbackInfo ci) {
        if(cloudRenderer != null) cloudRenderer.setWorld(world);
    }

    @Inject(at = @At("HEAD"), method = "renderClouds(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FDDD)V", cancellable = true)
    private void renderClouds(MatrixStack matrices, Matrix4f projMat, float tickDelta, double camX, double camY, double camZ, CallbackInfo ci) {
        if(cloudRenderer == null) return;
        if(glCompat.isIncompatible()) return;
        if(world == null || !world.getDimensionEntry().matchesKey(DimensionTypes.OVERWORLD)) return;
        if(!Main.getConfig().enabled) return;

        client.getProfiler().push(Main.MODID);
        glCompat.pushDebugGroup("Better Clouds");

        Vector3d cam = new Vector3d(camX, camY, camZ);
        Frustum frustum = this.frustum;
        Vector3d frustumPos = cam;
        if (capturedFrustum != null) {
            frustum = capturedFrustum;
            frustum.setPosition(capturedFrustumPosition.x, this.capturedFrustumPosition.y, this.capturedFrustumPosition.z);
            frustumPos = capturedFrustumPosition;
        }

        if(Main.isProfilingEnabled()) GL32.glFinish();
        long startTime = System.nanoTime();

        matrices.push();
        if(cloudRenderer.prepare(matrices, projMat, tickDelta, cam)) {
            ci.cancel();
            cloudRenderer.render(tickDelta, cam, frustumPos, frustum);
        }
        matrices.pop();

        if(Main.isProfilingEnabled()) {
            GL32.glFinish();
            profTimeAcc += (System.nanoTime() - startTime) / 1e6;
            profFrames++;
            if (profFrames >= Debug.profileInterval) {
                Main.debugChatMessage(String.format("§3CPU Times§r: %.3f §7avg§r", profTimeAcc / profFrames));
                profFrames = 0;
                profTimeAcc = 0;
            }
        }

        client.getProfiler().pop();
        glCompat.popDebugGroup();
    }


    @Inject(at = @At("HEAD"), method = "close")
    private void close(CallbackInfo ci) {
        if(cloudRenderer != null) cloudRenderer.close();
    }
}