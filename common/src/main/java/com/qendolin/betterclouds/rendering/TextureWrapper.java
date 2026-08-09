package com.qendolin.betterclouds.rendering;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.*;
import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.util.Producer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public final class TextureWrapper {
    private static final Object2ObjectOpenHashMap<String, TextureWrapper> cache = new Object2ObjectOpenHashMap<>();
    private final String name;
    private int id;
    private GpuTextureView view;
    private GpuSampler sampler;

    private TextureWrapper(String name, int id, GpuTextureView view, GpuSampler sampler) {
        this.name = name;
        this.id = id;
        this.view = view;
        this.sampler = sampler;
    }

    public static TextureWrapper fromBorrowedTexture(String name, int texId, int width, int height) {
        return fromBorrowedTexture(name, texId, width, height, TextureWrapper::defaultSampler);
    }

    public static TextureWrapper fromBorrowedTexture(String name, int texId, int width, int height, Producer<GpuSampler> sampler) {
        return from(name, texId, () -> RenderSystem.getDevice().createTextureView(
                new BorrowedGlTexture(texId, width, height)
        ), sampler);
    }

    public static TextureWrapper fromMcTexture(String name, Identifier mcTextureId, Producer<GpuSampler> customSampler) {
        var texture = Minecraft.getInstance().getTextureManager().getTexture(mcTextureId);
        return from(name, (int) (CloudRenderCoordinator.instance.clientTicks % 60), texture::getTextureView, customSampler);
    }

    public static TextureWrapper from(String name, int identifier, Producer<GpuTextureView> view, Producer<GpuSampler> sampler) {
        return cache.compute(name, (_, prevValue) -> {
            if (prevValue == null) {
                return new TextureWrapper(name, identifier, view.produce(), sampler.produce());
            }
            if (prevValue.id != identifier) {
                prevValue.close();

                prevValue.id = identifier;
                prevValue.view = view.produce();
                prevValue.sampler = sampler.produce();
            }
            return prevValue;
        });
    }

    public static TextureWrapper get(String name) {
        return cache.get(name);
    }

    public static void closeAllWrappers() {
        for (TextureWrapper wrap : cache.values())
            wrap.close();
        cache.clear();
    }

    public void close() {
        // don't close cache-owned sampler
        view.close();
    }

    public static GpuSampler defaultSampler() {
        return customSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.NEAREST);
    }

    public static GpuSampler customSampler(AddressMode u, AddressMode v, FilterMode fm) {
        return RenderSystem.getSamplerCache().getSampler(u, v, fm, fm, true);
    }

    public void bindTo(RenderPass pass) {
        pass.bindTexture(name, view, sampler);
    }

    public void glBind() {
        GlStateManager._bindTexture(id);
    }

    @SuppressWarnings("unused")
    public GpuTextureView view() {
        return view;
    }

    @SuppressWarnings("unused")
    public GpuSampler sampler() {
        return sampler;
    }
}
