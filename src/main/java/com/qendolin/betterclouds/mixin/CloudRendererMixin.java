package com.qendolin.betterclouds.mixin;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.qendolin.betterclouds.Config;
import com.qendolin.betterclouds.Main;
import com.qendolin.betterclouds.clouds.Generator;
import com.qendolin.betterclouds.clouds.Shader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.Map;

import static org.lwjgl.opengl.GL32.*;

@Mixin(WorldRenderer.class)
public abstract class CloudRendererMixin {
    @Shadow
    private ClientWorld world;
    @Shadow
    @Final
    private MinecraftClient client;
    @Shadow
    private int ticks;

    @Shadow private @Nullable CloudRenderMode lastCloudsRenderMode;
    private Shader cloudShader;
    private Generator cloudGenerator;
    private boolean firstGenerate = false;
    private float lastRaininess = -1;

    @Inject(at = @At("TAIL"), method = "reload(Lnet/minecraft/resource/ResourceManager;)V")
    public void reload(ResourceManager manager, CallbackInfo ci) {
        reloadShader(manager);
        reloadGenerator();
    }

    private void reloadGenerator() {
        if(cloudGenerator != null) cloudGenerator.close();
        cloudGenerator = new Generator();
        cloudGenerator.allocate(Main.CONFIG, isFancyMode());
        firstGenerate = false;
    }

    private void reloadShader(ResourceManager manager) {
        if(cloudShader != null) cloudShader.close();
        Config options = Main.CONFIG;
        Map<String, Float> defs = ImmutableMap.ofEntries(
                Map.entry(Shader.DEF_SIZE_X_KEY, options.sizeX),
                Map.entry(Shader.DEF_SIZE_Y_KEY, options.sizeY),
                Map.entry(Shader.DEF_FADE_EDGE_KEY, (float) options.fadeEdge)
        );

        try {
            cloudShader = new Shader(manager, isFancyMode(), defs);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Inject(at = @At("HEAD"), method = "renderClouds(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/math/Matrix4f;FDDD)V", cancellable = true)
    private void renderClouds(MatrixStack matrices, Matrix4f projMat, float tickDelta, double camX, double camY, double camZ, CallbackInfo ci) {
        ci.cancel();

        assert RenderSystem.isOnRenderThread();
        client.getProfiler().push("render_setup");
        // When the shader could not be loaded
        if (!cloudShader.isComplete()) return;

        matrices.push();
        matrices.translate(-camX, -camY, -camZ);
        DimensionEffects effects = world.getDimensionEffects();

        if(Main.CONFIG.hasChanged || lastCloudsRenderMode != client.options.getCloudRenderMode()) {
            lastCloudsRenderMode = client.options.getCloudRenderMode();
            reloadShader(client.getResourceManager());
            cloudGenerator.reallocate(Main.CONFIG, isFancyMode());
            Main.CONFIG.hasChanged = false;
            firstGenerate = false;
        }

        cloudShader.bind();
        cloudGenerator.bind();
        boolean cloudGeometryStale = cloudGenerator.update((float) camX, (float) camZ,  ticks + tickDelta, Main.CONFIG);
        Vec3f cloudPosition = new Vec3f(cloudGenerator.originX(), effects.getCloudsHeight(), cloudGenerator.originZ());
        matrices.translate(cloudPosition.getX(), cloudPosition.getY(), cloudPosition.getZ());

        Matrix4f mvpMat = projMat.copy();
        mvpMat.multiply(matrices.peek().getPositionMatrix());
        cloudShader.uModelViewProjMat.setMat4(mvpMat);
        cloudShader.uCloudsPosition.setVec3((float)camX-cloudPosition.getX(), (float)camY-cloudPosition.getY(), (float)camZ-cloudPosition.getZ());
        cloudShader.uCloudsDistance.setFloat(Main.CONFIG.blockDistance() - Main.CONFIG.chunkSize/2f);

        float skyAngle = world.getSkyAngleRadians(tickDelta);
        float raininess = Math.max(0.6f*world.getRainGradient(tickDelta), world.getThunderGradient(tickDelta));
        cloudShader.uSunDirection.setVec4((float) -Math.sin(skyAngle), (float) Math.cos(skyAngle), 0, 1-0.75f*raininess);

        Vec3d skyColor = world.getCloudsColor(tickDelta);
        cloudShader.uSkyColor.setVec4((float) skyColor.x, (float) skyColor.y, (float) skyColor.z, Main.CONFIG.opacity);

        float[] skyColorOverride = effects.getFogColorOverride(world.getSkyAngle(tickDelta), tickDelta);
        if (skyColorOverride != null) {
            cloudShader.uSkyColorOverride.setVec4(skyColorOverride[0], skyColorOverride[1], skyColorOverride[2], skyColorOverride[3]);
        } else {
            cloudShader.uSkyColorOverride.setVec4(0, 0, 0, 0);
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        if (!isFancyMode()) {
            RenderSystem.disableCull();
        }

        if(cloudGeometryStale || !firstGenerate || Math.abs(raininess - lastRaininess) > 0.05) {
            firstGenerate = true;
            lastRaininess = raininess;
            client.getProfiler().swap("generate_clouds");
            float cloudiness = raininess * 0.3f + 0.5f;
            cloudGenerator.generate(Main.CONFIG, cloudiness);
            client.getProfiler().swap("render_setup");
        }

        if(cloudGenerator.finished()) {
            client.getProfiler().swap("swap");
            cloudGenerator.swap();
            client.getProfiler().swap("render_setup");
        }

        client.getProfiler().swap("draw");
        glDrawArraysInstanced(GL_TRIANGLE_STRIP, 0, cloudGenerator.baseMeshVertexCount(), cloudGenerator.count());
        client.getProfiler().swap("render_teardown");

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        if (!isFancyMode()) {
            RenderSystem.enableCull();
        }
        cloudShader.unbind();
        cloudGenerator.unbind();

        matrices.pop();
        client.getProfiler().pop();
    }

    private boolean isFancyMode() {
        return client.options.getCloudRenderMode() == CloudRenderMode.FANCY;
    }

    @Inject(at = @At("HEAD"), method = "close")
    private void close(CallbackInfo ci) {
        cloudShader.close();
        cloudGenerator.close();
    }
}
