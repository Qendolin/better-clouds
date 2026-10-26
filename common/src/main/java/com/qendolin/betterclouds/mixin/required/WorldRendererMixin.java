package com.qendolin.betterclouds.mixin.required;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.qendolin.betterclouds.config.ConfigManager;
import com.qendolin.betterclouds.rendering.CloudRenderCoordinator;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.renderer.CloudRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.oit.OitRenderPassProvider;
import net.minecraft.client.renderer.oit.OitStage;
import net.minecraft.client.renderer.state.OptionsRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(value = LevelRenderer.class, priority = 900)
public abstract class WorldRendererMixin {
    @Shadow
    @Final
    private OptionsRenderState optionsRenderState;

    @Unique
    private boolean betterclouds$replaceClouds;
    @Unique
    private Consumer<RenderPass> betterclouds$renderClouds;
    @Unique
    private Vec3 betterclouds$lastCameraPos;

    @Inject(at = @At("HEAD"), method = "render")
    private void captureFrustumAndCheckState(GraphicsResourceAllocator resourceAllocator, boolean renderOutline, CameraRenderState cameraState, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, boolean hasPostEffects, CallbackInfo ci) {
        betterclouds$replaceClouds = false;
        betterclouds$renderClouds = null;
        CloudRenderCoordinator.instance.captureFrustum(cameraState);
        CloudRenderCoordinator.instance.checkState(optionsRenderState);
    }

    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;execute(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder$Inspector;)V"))
    private void prepareClouds(GraphicsResourceAllocator allocator, boolean renderOutline,
                               CameraRenderState camera, GpuBufferSlice terrainFog, Vector4f fogColor,
                               boolean renderSky, boolean hasPostEffects, CallbackInfo ci) {
        if (optionsRenderState.cloudStatus != CloudStatus.OFF) {
            betterclouds$lastCameraPos = camera.pos;
            betterclouds$replaceClouds = CloudRenderCoordinator.instance.renderClouds(
                    render -> betterclouds$renderClouds = render, camera.pos, camera.cameraEntityPartialTicks);
        }
    }

    /**
     * after opaque, before transparency
     * fixes <a href="https://github.com/Qendolin/better-clouds/issues/404">#404</a><br>
     * also before debug rendering (which clears the depth buffer to become always on top)
     * fixes <a href="https://github.com/Qendolin/better-clouds/issues/394">#394</a><br>
     * only runs if player is below cloud level
     */
    @Inject(method = "executeSolid", at = @At("TAIL"))
    private void renderCloudsBeforeTransparent(ChunkSectionsToRender chunkSectionsToRender, FeatureRenderDispatcher.PreparedFrame featureFrame, RenderPass renderPass, CallbackInfo ci) {
        if (betterclouds$lastCameraPos.y > CloudRenderCoordinator.instance.renderer.getCloudHeight()) return;
        Consumer<RenderPass> render = betterclouds$renderClouds;
        betterclouds$renderClouds = null;
        if (render != null) render.accept(renderPass);
    }

    /**
     * after transparency, before debug, only runs if player is above cloud level<br>
     * todo: implement oit
     */
    @Inject(method = "executeOutline", at = @At("HEAD"))
    private void renderCloudsAfterTransparent(FeatureRenderDispatcher.PreparedFrame featureFrame, CallbackInfo ci) {
        Consumer<RenderPass> render = betterclouds$renderClouds;
        betterclouds$renderClouds = null;
        if (render != null) render.accept(null);
    }

    @WrapWithCondition(method = "executeClassicTransparency", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/CloudRenderer;render(Lnet/minecraft/client/CloudStatus;Lcom/mojang/renderpearl/api/commands/RenderPass;)V"))
    private boolean renderVanillaClouds(CloudRenderer renderer, CloudStatus status, RenderPass pass) {
        return !betterclouds$replaceClouds;
    }

    @WrapWithCondition(method = "executeOit", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/CloudRenderer;renderOit(Lnet/minecraft/client/CloudStatus;Lnet/minecraft/client/renderer/oit/OitStage;Lcom/mojang/renderpearl/api/textures/GpuTextureView;Lnet/minecraft/client/renderer/oit/OitRenderPassProvider$Parameters;)V"))
    private boolean renderVanillaOitClouds(CloudRenderer renderer, CloudStatus status, OitStage stage,
                                           GpuTextureView depth, OitRenderPassProvider.Parameters parameters) {
        return !betterclouds$replaceClouds;
    }


    @Inject(at = @At("HEAD"), method = "close")
    private void close(CallbackInfo ci) {
        CloudRenderCoordinator.instance.close();
    }
}
