package com.qendolin.betterclouds.rendering.blaze3d;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.backend.opengl.GlConst;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import com.mojang.renderpearl.backend.opengl.GlTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.qendolin.betterclouds.compat.IrisCompat;
import com.qendolin.betterclouds.rendering.BorrowedGlTexture;
import com.qendolin.betterclouds.rendering.TextureWrapper;

import static org.lwjgl.opengl.GL33C.*;

/**
 * Render the clouds in a separate composite framebuffer to avoid precision loss in Iris' framebuffer,
 * and later this will be copied into the main framebuffer.
 * <br><br>
 * Fixes <a href="https://github.com/Qendolin/better-clouds/issues/374">#374</a>.
 */
final class IrisCloudTarget implements AutoCloseable {
    private GpuTexture color;
    private TextureWrapper colorView;
    private TextureWrapper destinationView;
    private TextureWrapper depthView;
    private Attachment destination;
    private Attachment depth;
    private int accumulationFbo;
    private int compositeFbo;
    private boolean destinationSrgb;

    /** must be called outside a render pass, after saving the caller's GL state */
    void prepare() {
        IrisCompat.instance().bindFramebuffer();
        int drawBuffer = glGetInteger(GL_DRAW_BUFFER0);
        if (drawBuffer == GL_NONE) {
            throw new IllegalStateException("Iris cloud shader has no color output 0");
        }
        Attachment nextDestination = Attachment.read(drawBuffer);
        Attachment nextDepth = Attachment.read(GL_DEPTH_ATTACHMENT);
        destinationSrgb = glIsEnabled(GL_FRAMEBUFFER_SRGB);

        if (color == null || color.getWidth(0) != nextDestination.width
                || color.getHeight(0) != nextDestination.height) {
            close();
            color = RenderSystem.getDevice().createTexture(
                    "BC accumulation", GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING,
                    GpuFormat.RGBA16_FLOAT, nextDestination.width, nextDestination.height, 1, 1);
            colorView = TextureWrapper.fromTexture("BC accumulation", color);
            accumulationFbo = GlStateManager.glGenFramebuffers();
            compositeFbo = GlStateManager.glGenFramebuffers();
        }
        if (!nextDestination.equals(destination)) {
            if (destinationView != null) destinationView.close();
            destination = nextDestination;
            destinationView = destination.borrowView("Iris cloud color");
        }
        if (!nextDepth.equals(depth)) {
            if (depthView != null) depthView.close();
            depth = nextDepth;
            depthView = depth.borrowView("Iris cloud depth");
        }

        GlStateManager._glBindFramebuffer(GL_FRAMEBUFFER, accumulationFbo);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D,
                ((GlTexture) color).glId(), 0);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depth.id, 0);
        glDrawBuffer(GL_COLOR_ATTACHMENT0);
        glReadBuffer(GL_COLOR_ATTACHMENT0);
        checkComplete();

        // An alias of output 0 avoids writing undefined values to a pack's other MRT attachments.
        GlStateManager._glBindFramebuffer(GL_FRAMEBUFFER, compositeFbo);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, destination.id, 0);
        glDrawBuffer(GL_COLOR_ATTACHMENT0);
        glReadBuffer(GL_COLOR_ATTACHMENT0);
        checkComplete();

        // Clear explicitly: Iris can redirect the framebuffer bind at render-pass creation.
        bindAccumulation();
        boolean scissor = glIsEnabled(GL_SCISSOR_TEST);
        byte[] mask = new byte[4];
        try (var stack = org.lwjgl.system.MemoryStack.stackPush()) {
            var buffer = stack.malloc(4);
            glGetBooleanv(GL_COLOR_WRITEMASK, buffer);
            buffer.get(mask);
        }
        glDisable(GL_SCISSOR_TEST);
        glColorMaski(0, true, true, true, true);
        glClearBufferfv(GL_COLOR, 0, new float[4]);
        glColorMaski(0, mask[0] != 0, mask[1] != 0, mask[2] != 0, mask[3] != 0);
        if (scissor) glEnable(GL_SCISSOR_TEST);
    }

    void bindAccumulation() {
        GlStateManager._glBindFramebuffer(GL_FRAMEBUFFER, accumulationFbo);
        GlStateManager._viewport(0, 0, color.getWidth(0), color.getHeight(0));
        glDisable(GL_FRAMEBUFFER_SRGB);
    }

    void bindComposite() {
        GlStateManager._glBindFramebuffer(GL_FRAMEBUFFER, compositeFbo);
        GlStateManager._viewport(0, 0, color.getWidth(0), color.getHeight(0));
        if (destinationSrgb) glEnable(GL_FRAMEBUFFER_SRGB);
        else glDisable(GL_FRAMEBUFFER_SRGB);
    }

    GpuTextureView colorView() { return colorView.view(); }
    GpuTextureView destinationView() { return destinationView.view(); }
    GpuTextureView depthView() { return depthView.view(); }

    private static void checkComplete() {
        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("Incomplete Better Clouds framebuffer: 0x" + Integer.toHexString(status));
        }
    }

    @Override
    public void close() {
        if (accumulationFbo != 0) GlStateManager._glDeleteFramebuffers(accumulationFbo);
        if (compositeFbo != 0) GlStateManager._glDeleteFramebuffers(compositeFbo);
        accumulationFbo = compositeFbo = 0;
        if (colorView != null) colorView.close();
        if (color != null) color.close();
        if (destinationView != null) destinationView.close();
        if (depthView != null) depthView.close();
        colorView = destinationView = depthView = null;
        color = null;
        destination = depth = null;
    }

    private record Attachment(int id, int width, int height, GpuFormat format) {
        static Attachment read(int attachment) {
            int type = glGetFramebufferAttachmentParameteri(GL_FRAMEBUFFER, attachment, GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE);
            if (type != GL_TEXTURE || glGetFramebufferAttachmentParameteri(GL_FRAMEBUFFER, attachment,
                    GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL) != 0) {
                throw new IllegalStateException("Expected a level-zero Iris texture attachment: " + attachment);
            }
            int id = glGetFramebufferAttachmentParameteri(GL_FRAMEBUFFER, attachment, GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME);
            int previous = glGetInteger(GL_TEXTURE_BINDING_2D);
            try {
                glBindTexture(GL_TEXTURE_2D, id);
                int width = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH);
                int height = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT);
                int internalFormat = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_INTERNAL_FORMAT);
                for (GpuFormat format : GpuFormat.values()) {
                    if (GlConst.toGlInternalId(format) == internalFormat) {
                        return new Attachment(id, width, height, format);
                    }
                }
                throw new IllegalStateException("Unsupported Iris attachment format: 0x" + Integer.toHexString(internalFormat));
            } finally {
                // Raw bind/restore leaves Minecraft's texture binding cache unchanged.
                glBindTexture(GL_TEXTURE_2D, previous);
            }
        }

        TextureWrapper borrowView(String label) {
            return TextureWrapper.fromTexture(label, new BorrowedGlTexture(
                    GpuTexture.USAGE_RENDER_ATTACHMENT, label, format, id, width, height));
        }
    }

    /** stores iris state gl state; try-with-resources block will automatically restore it by calling close() */
    static final class State implements AutoCloseable {
        private final int draw = glGetInteger(GL_DRAW_FRAMEBUFFER_BINDING);
        private final int read = glGetInteger(GL_READ_FRAMEBUFFER_BINDING);
        private final int[] viewport = new int[4];
        private final boolean srgb = glIsEnabled(GL_FRAMEBUFFER_SRGB);

        State() {
            glGetIntegerv(GL_VIEWPORT, viewport);
        }

        @Override
        public void close() {
            GlStateManager._glBindFramebuffer(GL_DRAW_FRAMEBUFFER, draw);
            GlStateManager._glBindFramebuffer(GL_READ_FRAMEBUFFER, read);
            GlStateManager._viewport(viewport[0], viewport[1], viewport[2], viewport[3]);
            if (srgb) glEnable(GL_FRAMEBUFFER_SRGB);
            else glDisable(GL_FRAMEBUFFER_SRGB);
        }
    }
}
