package com.qendolin.betterclouds.mixin;

import com.qendolin.betterclouds.Main;
import com.qendolin.betterclouds.clouds.Renderer4;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.qendolin.betterclouds.Main.bcPopDebugGroup;
import static com.qendolin.betterclouds.Main.bcPushDebugGroup;

@Mixin(WorldRenderer.class)
public abstract class CloudRendererMixin {

//    private Renderer3 cloudRenderer;
    private Renderer4 cloudRenderer4;
    @Shadow
    private Frustum frustum;

    private double accum;
    private int frames;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(MinecraftClient client, EntityRenderDispatcher entityRenderDispatcher, BlockEntityRenderDispatcher blockEntityRenderDispatcher, BufferBuilderStorage bufferBuilders, CallbackInfo ci) {
//        cloudRenderer = new Renderer3(client);
        cloudRenderer4 = new Renderer4(client);
    }

    @Shadow
    private int ticks;

    @Shadow private @Nullable Frustum capturedFrustum;

    @Shadow @Final private Vector3d capturedFrustumPosition;

    @Shadow @Final private MinecraftClient client;

    @Inject(at = @At("TAIL"), method = "reload(Lnet/minecraft/resource/ResourceManager;)V")
    private void onReload(ResourceManager manager, CallbackInfo ci) {
//        if(cloudRenderer != null) cloudRenderer.reload(manager);
        if(cloudRenderer4 != null) cloudRenderer4.reload(manager);
    }

    @Inject(at = @At("TAIL"), method = "setWorld")
    private void onSetWorld(ClientWorld world, CallbackInfo ci) {
//        if(cloudRenderer != null) cloudRenderer.setWorld(world);
        if(cloudRenderer4 != null) cloudRenderer4.setWorld(world);
    }

    @Inject(at = @At("HEAD"), method = "renderClouds(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FDDD)V", cancellable = true)
    private void renderClouds(MatrixStack matrices, Matrix4f projMat, float tickDelta, double camX, double camY, double camZ, CallbackInfo ci) {
//        if(cloudRenderer == null || cloudRenderer.skipRender()) return;
        if(cloudRenderer4 == null) return;

        client.getProfiler().push(Main.MODID);
        bcPushDebugGroup("Better Clouds");

        Frustum frustum = this.frustum;
        if (capturedFrustum != null) {
            frustum = capturedFrustum;
            frustum.setPosition(capturedFrustumPosition.x, this.capturedFrustumPosition.y, this.capturedFrustumPosition.z);
        }

        if(Main.DO_PROFILE) GL11.glFinish();
        long startTime = System.nanoTime();

        Vec3d cam = new Vec3d(camX, camY, camZ);

//        cloudRenderer.render(matrices, projMat, tickDelta, ticks, cam, frustum);

        matrices.push();
        if(cloudRenderer4.setup(matrices, projMat, tickDelta, ticks, cam, frustum)) {
            ci.cancel();
            cloudRenderer4.render(matrices, tickDelta, ticks, cam, frustum);
        }
        matrices.pop();

        if(Main.DO_PROFILE) {
            GL11.glFinish();
            accum += (System.nanoTime() - startTime) / 1e6;
            frames++;
            if (frames == 10000) {
                System.out.println("CPU Time Average: " + accum / frames + " mspf");
                frames = 0;
                accum = 0;
            }
        }

        client.getProfiler().pop();
        bcPopDebugGroup();
    }


    @Inject(at = @At("HEAD"), method = "close")
    private void close(CallbackInfo ci) {
//        if(cloudRenderer != null) cloudRenderer.close();
        if(cloudRenderer4 != null) cloudRenderer4.close();
    }
}
