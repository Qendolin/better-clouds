package com.qendolin.betterclouds.compat;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GLX;
import com.qendolin.betterclouds.Main;
import net.minecraft.client.util.Untracker;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import java.util.ArrayList;
import java.util.List;

public class GLCompat {
    public final int GL_VERTEX_ARRAY;
    public final int GL_BUFFER;
    public final int GL_PROGRAM;
    public final int GL_TEXTURE;
    public final int GL_FRAMEBUFFER;
    public final int GL_SHADER;
    public final int GL_QUERY;
    public final int GL_DEPTH_STENCIL_TEXTURE_MODE;
    public final int GL_MAP_PERSISTENT_BIT;
    public final int GL_MAP_COHERENT_BIT;

    private final boolean isDev;

    public final boolean openGl44;
    public final boolean openGl43;
    public final boolean openGl42;
    public final boolean openGl40;
    public final boolean openGl32;
    public final boolean openGl33;

    public final int openGlMax;

    public final boolean khrDebug;
    public final boolean amdDebugOutput;
    public final boolean arbDebugOutput;
    public final boolean extDebugLabel;
    public final boolean extDebugMarker;
    public final boolean arbTextureView;
    public final boolean arbBaseInstance;
    public final boolean arbTextureStorage;
    public final boolean extTextureStorage;
    public final boolean arbDirectStateAccess;
    public final boolean extDirectStateAccess;
    public final boolean arbStencilTexturing;
    public final boolean arbBufferStorage;
    public final boolean arbInstancedArrays;
    public final boolean nvCopyDepthToColor;
    public final boolean arbDrawBuffersBlend;

    public final boolean arbSeparateShaderObjects;
    public final boolean arbConservativeDepth;
    public final boolean arbShaderImageLoadStore;
    public final boolean extShaderImageLoadStore;
    public final boolean arbExplicitAttribLocation;

    public final ImmutableList<String> supportedCheckedExtensions;

    public final boolean glObjectLabel;
    public final boolean glPushDebugGroup;
    public final boolean glPopDebugGroup;
    public final boolean glDebugMessageInsert;
    public final boolean glTextureView;
    public final boolean glDrawArraysInstancedBaseInstance;
    public final boolean glTexStorage2D;
    public final boolean glBufferStorage;
    public final boolean glVertexAttribDivisor;
    public final boolean glBlendFunci;

    public final ImmutableList<String> supportedCheckedFunctions;

    public final boolean useBaseInstanceFallback;
    public final boolean useStencilTextureFallback;
    public final boolean useDepthWriteFallback;
    public final boolean useTexStorageFallback;

    public final ImmutableList<String> usedFallbacks;

    private final boolean compatible;
    private final boolean partiallyIncompatible;

    public GLCompat(boolean isDev) {
        this.isDev = isDev;
        GLCapabilities caps = GL.getCapabilities();
        openGl44 = caps.OpenGL44;
        openGl43 = caps.OpenGL43;
        openGl42 = caps.OpenGL42;
        openGl40 = caps.OpenGL40;
        openGl33 = caps.OpenGL33;
        // OpenGL 3.2 is required to play the game
        openGl32 = caps.OpenGL32;

        if (caps.OpenGL46) openGlMax = 46;
        else if (caps.OpenGL45) openGlMax = 45;
        else if (caps.OpenGL44) openGlMax = 44;
        else if (caps.OpenGL43) openGlMax = 43;
        else if (caps.OpenGL42) openGlMax = 42;
        else if (caps.OpenGL41) openGlMax = 41;
        else if (caps.OpenGL40) openGlMax = 40;
        else if (caps.OpenGL33) openGlMax = 33;
        else if (caps.OpenGL32) openGlMax = 32;
        else openGlMax = 0;

        khrDebug = caps.GL_KHR_debug;
        amdDebugOutput = caps.GL_AMD_debug_output;
        arbDebugOutput = caps.GL_ARB_debug_output;
        extDebugLabel = caps.GL_EXT_debug_label;
        extDebugMarker = caps.GL_EXT_debug_marker;
        arbTextureView = caps.GL_ARB_texture_view;
        arbBaseInstance = caps.GL_ARB_base_instance;
        arbTextureStorage = caps.GL_ARB_texture_storage;
        extTextureStorage = caps.GL_EXT_texture_storage;
        arbDirectStateAccess = caps.GL_ARB_direct_state_access;
        extDirectStateAccess = caps.GL_EXT_direct_state_access;
        arbStencilTexturing = caps.GL_ARB_stencil_texturing;
        arbBufferStorage = caps.GL_ARB_buffer_storage;
        arbInstancedArrays = caps.GL_ARB_instanced_arrays;
        nvCopyDepthToColor = caps.GL_NV_copy_depth_to_color;
        arbDrawBuffersBlend = caps.GL_ARB_draw_buffers_blend;

        // glsl related
        arbSeparateShaderObjects = caps.GL_ARB_separate_shader_objects;
        arbConservativeDepth = caps.GL_ARB_conservative_depth;
        arbShaderImageLoadStore = caps.GL_ARB_shader_image_load_store;
        extShaderImageLoadStore = caps.GL_EXT_shader_image_load_store;
        arbExplicitAttribLocation = caps.GL_ARB_explicit_attrib_location;

        List<String> supportedExtensions = new ArrayList<>();
        if (khrDebug) supportedExtensions.add("GL_KHR_debug");
        if (amdDebugOutput) supportedExtensions.add("GL_AMD_debug_output");
        if (arbDebugOutput) supportedExtensions.add("GL_ARB_debug_output");
        if (extDebugLabel) supportedExtensions.add("GL_EXT_debug_label");
        if (extDebugMarker) supportedExtensions.add("GL_EXT_debug_marker");
        if (arbTextureView) supportedExtensions.add("GL_ARB_texture_view");
        if (arbBaseInstance) supportedExtensions.add("GL_ARB_base_instance");
        if (arbTextureStorage) supportedExtensions.add("GL_ARB_texture_storage");
        if (extTextureStorage) supportedExtensions.add("GL_EXT_texture_storage");
        if (arbDirectStateAccess) supportedExtensions.add("GL_ARB_direct_state_access");
        if (extDirectStateAccess) supportedExtensions.add("GL_EXT_direct_state_access");
        if (arbStencilTexturing) supportedExtensions.add("GL_ARB_stencil_texturing");
        if (arbBufferStorage) supportedExtensions.add("GL_ARB_buffer_storage");
        if (arbInstancedArrays) supportedExtensions.add("GL_ARB_instanced_arrays");
        if (nvCopyDepthToColor) supportedExtensions.add("GL_NV_copy_depth_to_color");
        if (arbSeparateShaderObjects) supportedExtensions.add("GL_ARB_separate_shader_objects");
        if (arbConservativeDepth) supportedExtensions.add("GL_ARB_conservative_depth");
        if (arbShaderImageLoadStore) supportedExtensions.add("GL_ARB_shader_image_load_store");
        if (extShaderImageLoadStore) supportedExtensions.add("GL_EXT_shader_image_load_store");
        if (arbExplicitAttribLocation) supportedExtensions.add("GL_ARB_explicit_attrib_location");
        if (arbDrawBuffersBlend) supportedExtensions.add("GL_ARB_draw_buffers_blend");
        supportedCheckedExtensions = ImmutableList.copyOf(supportedExtensions);

        glObjectLabel = caps.glObjectLabel != MemoryUtil.NULL;
        glPushDebugGroup = caps.glPushDebugGroup != MemoryUtil.NULL;
        glPopDebugGroup = caps.glPopDebugGroup != MemoryUtil.NULL;
        glDebugMessageInsert = caps.glDebugMessageInsert != MemoryUtil.NULL;
        glTextureView = caps.glTextureView != MemoryUtil.NULL;
        glDrawArraysInstancedBaseInstance = caps.glDrawArraysInstancedBaseInstance != MemoryUtil.NULL;
        glTexStorage2D = caps.glTexStorage2D != MemoryUtil.NULL;
        glBufferStorage = caps.glBufferStorage != MemoryUtil.NULL;
        glVertexAttribDivisor = caps.glVertexAttribDivisor != MemoryUtil.NULL;
        glBlendFunci = caps.glBlendFunci != MemoryUtil.NULL;

        List<String> supportedFunctions = new ArrayList<>();
        if (glObjectLabel) supportedFunctions.add("glObjectLabel");
        if (glPushDebugGroup) supportedFunctions.add("glPushDebugGroup");
        if (glPopDebugGroup) supportedFunctions.add("glPopDebugGroup");
        if (glDebugMessageInsert) supportedFunctions.add("glDebugMessageInsert");
        if (glTextureView) supportedFunctions.add("glTextureView");
        if (glDrawArraysInstancedBaseInstance) supportedFunctions.add("glDrawArraysInstancedBaseInstance");
        if (glTexStorage2D) supportedFunctions.add("glTexStorage2D");
        if (glBufferStorage) supportedFunctions.add("glBufferStorage");
        if (glVertexAttribDivisor) supportedFunctions.add("glVertexAttribDivisor");
        if (glBlendFunci) supportedFunctions.add("glBlendFunci");
        supportedCheckedFunctions = ImmutableList.copyOf(supportedFunctions);

        boolean supportsBaseInstance = glDrawArraysInstancedBaseInstance || arbBaseInstance;
        boolean supportsTextureStorage = glTexStorage2D || arbTextureStorage || extTextureStorage;
        boolean supportsTextureView = supportsTextureStorage && (glTextureView || arbTextureView);
        boolean supportsStencilTexturing = arbStencilTexturing || openGl43;

        //noinspection UnnecessaryLocalVariable
        boolean canReadStencil = supportsStencilTexturing;

        compatible = openGl32 &&
            (openGl33 || (glVertexAttribDivisor || arbInstancedArrays)) &&
            (supportsStencilTexturing || (openGl40 || glBlendFunci || arbDrawBuffersBlend));

        useBaseInstanceFallback = !supportsBaseInstance;
        useStencilTextureFallback = !canReadStencil;
        useTexStorageFallback = !supportsTextureStorage;
        useDepthWriteFallback = !supportsTextureView && canReadStencil;

        List<String> usedFallbacks = new ArrayList<>();
        if (useBaseInstanceFallback) usedFallbacks.add("base_instance");
        if (useStencilTextureFallback) usedFallbacks.add("stencil_texture");
        if (useTexStorageFallback) usedFallbacks.add("texture_storage");
        if (useDepthWriteFallback) usedFallbacks.add("depth_view_write");
        this.usedFallbacks = ImmutableList.copyOf(usedFallbacks);

        partiallyIncompatible = useBaseInstanceFallback || useStencilTextureFallback || useDepthWriteFallback || useTexStorageFallback;

        GL_VERTEX_ARRAY = GL32.GL_VERTEX_ARRAY;
        GL_BUFFER = KHRDebug.GL_BUFFER;
        GL_PROGRAM = KHRDebug.GL_PROGRAM;
        GL_TEXTURE = GL32.GL_TEXTURE;
        GL_FRAMEBUFFER = GL32.GL_FRAMEBUFFER;
        GL_SHADER = KHRDebug.GL_SHADER;
        GL_QUERY = KHRDebug.GL_QUERY;

        GL_DEPTH_STENCIL_TEXTURE_MODE = ARBStencilTexturing.GL_DEPTH_STENCIL_TEXTURE_MODE;

        GL_MAP_PERSISTENT_BIT = ARBBufferStorage.GL_MAP_PERSISTENT_BIT;
        GL_MAP_COHERENT_BIT = ARBBufferStorage.GL_MAP_COHERENT_BIT;
    }

    public boolean isIncompatible() {
        return !compatible;
    }

    public boolean isPartiallyIncompatible() {
        return partiallyIncompatible;
    }

    public void objectLabelDev(int type, int name, String label) {
        if (!isDev) return;
        objectLabel(type, name, label);
    }

    public void objectLabel(int type, int name, String label) {
        String typeString = switch (type) {
            case GL43.GL_TEXTURE -> "tex";
            case GL43.GL_BUFFER -> "buf";
            case GL43.GL_VERTEX_ARRAY -> "va";
            case GL43.GL_FRAMEBUFFER -> "fb";
            case GL43.GL_SHADER -> "sh";
            case GL43.GL_PROGRAM -> "shp";
            case GL43.GL_QUERY -> "qry";
            case GL43.GL_PROGRAM_PIPELINE -> "spp";
            case GL43.GL_TRANSFORM_FEEDBACK -> "tff";
            case GL43.GL_SAMPLER -> "ts";
            case GL43.GL_RENDERBUFFER -> "rb";
            default -> "unk";
        };
        String fullLabel = Main.MODID + ":" + label + ":" + typeString;
        if (glObjectLabel) {
            GL43.glObjectLabel(type, name, fullLabel);
        } else if (khrDebug) {
            KHRDebug.glObjectLabel(type, name, fullLabel);
        } else if (extDebugLabel) {
            int extType = switch (type) {
                case GL43.GL_TEXTURE -> GL43.GL_TEXTURE;
                case GL43.GL_BUFFER -> EXTDebugLabel.GL_BUFFER_OBJECT_EXT;
                case GL43.GL_VERTEX_ARRAY -> EXTDebugLabel.GL_VERTEX_ARRAY_OBJECT_EXT;
                case GL43.GL_FRAMEBUFFER -> GL43.GL_FRAMEBUFFER;
                case GL43.GL_SHADER -> EXTDebugLabel.GL_SHADER_OBJECT_EXT;
                case GL43.GL_PROGRAM -> EXTDebugLabel.GL_PROGRAM_OBJECT_EXT;
                case GL43.GL_QUERY -> EXTDebugLabel.GL_QUERY_OBJECT_EXT;
                case GL43.GL_PROGRAM_PIPELINE -> EXTDebugLabel.GL_PROGRAM_PIPELINE_OBJECT_EXT;
                case GL43.GL_TRANSFORM_FEEDBACK -> GL43.GL_TRANSFORM_FEEDBACK;
                case GL43.GL_SAMPLER -> GL43.GL_SAMPLER;
                case GL43.GL_RENDERBUFFER -> GL43.GL_RENDERBUFFER;
                default -> type;
            };
            EXTDebugLabel.glLabelObjectEXT(extType, name, fullLabel);
        }
    }

    public void pushDebugGroupDev(String name) {
        if (!isDev) return;
        pushDebugGroup(name);
    }

    public void pushDebugGroup(String name) {
        if (glPushDebugGroup) {
            GL43.glPushDebugGroup(GL43.GL_DEBUG_SOURCE_APPLICATION, 1337, name + "\0");
        } else if (khrDebug) {
            KHRDebug.glPushDebugGroup(GL43.GL_DEBUG_SOURCE_APPLICATION, 1337, name + "\0");
        } else if (extDebugMarker) {
            EXTDebugMarker.glPushGroupMarkerEXT(name + "\0");
        }
    }

    public void popDebugGroupDev() {
        if (!isDev) return;
        popDebugGroup();
    }

    public void popDebugGroup() {
        if (glPopDebugGroup) {
            GL43.glPopDebugGroup();
        } else if (khrDebug) {
            KHRDebug.glPopDebugGroup();
        } else if (extDebugMarker) {
            EXTDebugMarker.glPopGroupMarkerEXT();
        }
    }

    public void debugMessageDev(String message) {
        if (!isDev) return;
        debugMessage(message);
    }

    public void debugMessage(String message) {
        if (glDebugMessageInsert) {
            GL43.glDebugMessageInsert(GL43.GL_DEBUG_SOURCE_APPLICATION, GL43.GL_DEBUG_TYPE_OTHER, 0, GL43.GL_DEBUG_SEVERITY_NOTIFICATION, message + "\0");
        } else if (khrDebug) {
            KHRDebug.glDebugMessageInsert(KHRDebug.GL_DEBUG_SOURCE_APPLICATION, KHRDebug.GL_DEBUG_TYPE_OTHER, 0, KHRDebug.GL_DEBUG_SEVERITY_NOTIFICATION, message + "\0");
        } else if (extDebugMarker) {
            EXTDebugMarker.glInsertEventMarkerEXT(message + "\0");
        }
    }

    public void enableDebugOutputSynchronousDev() {
        if (!isDev) return;
        enableDebugOutputSynchronous();
    }

    public void enableDebugOutputSynchronous() {
        if (openGl43) {
            GL43.glEnable(ARBDebugOutput.GL_DEBUG_OUTPUT_SYNCHRONOUS_ARB);
        } else if (arbDebugOutput) {
            GL32.glEnable(ARBDebugOutput.GL_DEBUG_OUTPUT_SYNCHRONOUS_ARB);
        } else if (khrDebug) {
            GL32.glEnable(KHRDebug.GL_DEBUG_OUTPUT_SYNCHRONOUS);
        }
    }

    public void debugMessageCallbackDev(GLDebugMessageCallbackI callback) {
        if (!isDev) return;
        debugMessageCallback(callback);
    }

    public void debugMessageCallback(GLDebugMessageCallbackI callback) {
        if (openGl43) {
            GL43.glDebugMessageCallback(GLX.make(GLDebugMessageCallback.create(callback), Untracker::untrack), 0);
        } else if (arbDebugOutput) {
            ARBDebugOutput.glDebugMessageCallbackARB(GLX.make(GLDebugMessageARBCallback.create(callback::invoke), Untracker::untrack), 0);
        } else if (khrDebug) {
            KHRDebug.glDebugMessageCallback(GLX.make(GLDebugMessageCallback.create(callback), Untracker::untrack), 0);
        }
    }

    public void debugMessageControlDev(int source, int type, int severity, int[] ids, boolean enabled) {
        if (!isDev) return;
        debugMessageControl(source, type, severity, ids, enabled);
    }

    public void debugMessageControl(int source, int type, int severity, int[] ids, boolean enabled) {
        if (openGl43) {
            GL43.glDebugMessageControl(source, type, severity, ids, enabled);
        } else if (arbDebugOutput) {
            ARBDebugOutput.glDebugMessageControlARB(source, type, severity, ids, enabled);
        } else if (khrDebug) {
            KHRDebug.glDebugMessageControl(source, type, severity, ids, enabled);
        }
    }

    public void textureView(int texture, int target, int origtexture, int internalformat, int minlevel, int numlevels, int minlayer, int numlayers) {
        if (glTextureView) {
            GL43.glTextureView(texture, target, origtexture, internalformat, minlevel, numlevels, minlayer, numlayers);
        } else if (arbTextureView) {
            ARBTextureView.glTextureView(texture, target, origtexture, internalformat, minlevel, numlevels, minlayer, numlayers);
        }
    }

    public void drawArraysInstancedBaseInstanceFallback(int mode, int first, int count, int primcount, int baseinstance) {
        if (useBaseInstanceFallback) {
            GL32.glDrawArraysInstanced(mode, first, count, primcount);
        } else {
            drawArraysInstancedBaseInstance(mode, first, count, primcount, baseinstance);
        }
    }

    public void drawArraysInstancedBaseInstance(int mode, int first, int count, int primcount, int baseinstance) {
        if (glDrawArraysInstancedBaseInstance) {
            GL42.glDrawArraysInstancedBaseInstance(mode, first, count, primcount, baseinstance);
        } else if (arbBaseInstance) {
            ARBBaseInstance.glDrawArraysInstancedBaseInstance(mode, first, count, primcount, baseinstance);
        }
    }

    public void texStorage2DFallback(int target, int levels, int internalformat, int width, int height, int format, int type) {
        if (useTexStorageFallback) {
            GL32.glTexImage2D(target, 0, internalformat, width, height, 0, format, type, 0);
            GL32.glTexParameteri(target, GL32.GL_TEXTURE_MAX_LEVEL, levels - 1);
        } else {
            texStorage2D(target, levels, internalformat, width, height);
        }
    }

    public void texStorage2D(int target, int levels, int internalformat, int width, int height) {
        if (glTexStorage2D) {
            GL42.glTexStorage2D(target, levels, internalformat, width, height);
        } else if (arbTextureStorage) {
            ARBTextureStorage.glTexStorage2D(target, levels, internalformat, width, height);
        } else if (extTextureStorage) {
            EXTTextureStorage.glTexStorage2DEXT(target, levels, internalformat, width, height);
        }
    }

    public void bufferStorage(int target, long size, int flags) {
        if (glBufferStorage) {
            GL44.glBufferStorage(target, size, flags);
        } else if (arbBufferStorage) {
            ARBBufferStorage.glBufferStorage(target, size, flags);
        }
    }

    public void vertexAttribDivisor(int index, int divisor) {
        if (glVertexAttribDivisor) {
            GL33.glVertexAttribDivisor(index, divisor);
        } else if (arbInstancedArrays) {
            ARBInstancedArrays.glVertexAttribDivisorARB(index, divisor);
        }
    }

    public void blendFunci(int buf, int sfactor, int dfactor) {
        if (glBlendFunci) {
            GL40.glBlendFunci(buf, sfactor, dfactor);
        } else if (arbDrawBuffersBlend) {
            ARBDrawBuffersBlend.glBlendFunciARB(buf, sfactor, dfactor);
        }
    }
}
