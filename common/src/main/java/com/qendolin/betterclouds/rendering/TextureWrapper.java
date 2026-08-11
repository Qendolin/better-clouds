package com.qendolin.betterclouds.rendering;

import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.*;
import com.qendolin.betterclouds.util.Producer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

/**
 * Unifies Minecraft textures, OpenGL texture ids, DH texture wrappers,
 * and Voxy texture wrappers under a single object that provides
 * <code>GpuTextureView</code>s and <code>GpuSampler</code>s for
 * the Blaze3D renderer
 *
 */
public class TextureWrapper {
    private static final Object2ObjectOpenHashMap<String, TextureWrapper> cache = new Object2ObjectOpenHashMap<>();
    private final String name;
    private int id;
    private GpuTextureView view;
    private GpuSampler sampler;
    private boolean borrowed;

    private TextureWrapper(String name, int id, GpuTextureView view, GpuSampler sampler) {
        this.name = name;
        this.id = id;
        this.view = view;
        this.sampler = sampler;
    }

    public static TextureWrapper fromBorrowedTexture(String name, int texId, int width, int height) {
        return fromBorrowedTexture(name, texId, width, height, TextureWrapper::defaultSampler).asBorrowed();
    }

    public static TextureWrapper fromBorrowedTexture(String name, int texId, int width, int height, Producer<GpuSampler> sampler) {
        return from(name, texId, () -> RenderSystem.getDevice().createTextureView(
                new BorrowedGlTexture(texId, width, height)
        ), sampler).asBorrowed();
    }

    public static TextureWrapper fromMcTexture(String name, Identifier mcTextureId, Producer<GpuSampler> customSampler) {
        var view = Minecraft.getInstance().getTextureManager().getTexture(mcTextureId).getTextureView();
        return new TextureWrapper(name, 0 /* not cached as it does not go through #from */, view, customSampler.produce()).asBorrowed();
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
                prevValue.borrowed = false;
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
        if (view != null && !borrowed)
            view.close();
    }

    public static GpuSampler defaultSampler() {
        return customSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.NEAREST);
    }

    public static GpuSampler customSampler(AddressMode u, AddressMode v, FilterMode fm) {
        return RenderSystem.getSamplerCache().getSampler(u, v, fm, fm, false);
    }

    public void bindTo(RenderPass pass) {
        pass.bindTexture(name, view, sampler);
    }

    @SuppressWarnings("unused")
    public GpuTextureView view() {
        return view;
    }

    @SuppressWarnings("unused")
    public GpuSampler sampler() {
        return sampler;
    }

    public TextureWrapper asBorrowed() {
        this.borrowed = true;
        return this;
    }
}
