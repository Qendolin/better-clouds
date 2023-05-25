package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.Main;
import org.lwjgl.opengl.*;

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
    public final boolean openGl32;
    public final boolean openGl33;
    public final boolean khrDebug;
    public final boolean arbTextureView;
    public final boolean arbBaseInstance;
    public final boolean arbTextureStorage;
    public final boolean arbStencilTexturing;
    public final boolean arbBufferStorage;
    public final boolean arbInstancedArrays;

    private final boolean compatible;

    public GLCompat(boolean isDev) {
        this.isDev = isDev;
        GLCapabilities caps = GL.getCapabilities();
        openGl44 = caps.OpenGL44;
        openGl43 = caps.OpenGL43;
        openGl42 = caps.OpenGL42;
        openGl33 = caps.OpenGL33;
        // OpenGL 3.2 is required to play the game
        openGl32 = caps.OpenGL32;
        khrDebug = caps.GL_KHR_debug;
        arbTextureView = caps.GL_ARB_texture_view;
        arbBaseInstance = caps.GL_ARB_base_instance;
        arbTextureStorage = caps.GL_ARB_texture_storage;
        arbStencilTexturing = caps.GL_ARB_stencil_texturing;
        arbBufferStorage = caps.GL_ARB_buffer_storage;
        arbInstancedArrays = caps.GL_ARB_instanced_arrays;

        compatible = openGl32 &&
            (openGl33 || (arbInstancedArrays)) &&
            (openGl42 || (arbBaseInstance && arbTextureStorage)) &&
            (openGl43 || (arbTextureView && arbStencilTexturing));

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

    public void objectLabel(int type, int name, String label) {
        if(!isDev) return;
        String typeString = switch (type) {
            case GL43.GL_TEXTURE -> "tex";
            case GL43.GL_BUFFER -> "buf";
            case GL43.GL_VERTEX_ARRAY -> "va";
            case GL43.GL_FRAMEBUFFER -> "fb";
            case GL43.GL_SHADER -> "sh";
            case GL43.GL_PROGRAM -> "shp";
            case GL43.GL_QUERY -> "qry";
            default -> "unk";
        };
        if(openGl43) {
            GL43.glObjectLabel(type, name, Main.MODID + ":" + label + ":" + typeString);
        } else if(khrDebug) {
            KHRDebug.glObjectLabel(type, name, Main.MODID + ":" + label + ":" + typeString);
        }
    }

    public void pushDebugGroup(String name) {
        if(!isDev) return;
        if(openGl43) {
            GL43.glPushDebugGroup(GL43.GL_DEBUG_SOURCE_APPLICATION, 1337, name+"\0");
        } else if(khrDebug) {
            KHRDebug.glPushDebugGroup(GL43.GL_DEBUG_SOURCE_APPLICATION, 1337, name+"\0");
        }
    }

    public void popDebugGroup() {
        if(!isDev) return;
        if(openGl43) {
            GL43.glPopDebugGroup();
        } else if(khrDebug) {
            KHRDebug.glPopDebugGroup();
        }
    }

    public void debugMessage(String message) {
        if(!isDev) return;
        if(openGl43) {
            GL43.glDebugMessageInsert(GL43.GL_DEBUG_SOURCE_APPLICATION, GL43.GL_DEBUG_TYPE_OTHER, 0, GL43.GL_DEBUG_SEVERITY_NOTIFICATION, message+"\0");
        } else if(khrDebug) {
            KHRDebug.glDebugMessageInsert(KHRDebug.GL_DEBUG_SOURCE_APPLICATION, KHRDebug.GL_DEBUG_TYPE_OTHER, 0, KHRDebug.GL_DEBUG_SEVERITY_NOTIFICATION, message+"\0");
        }
    }

    public void enableDebugOutputSynchronous() {
        if(!isDev) return;
        if(openGl43) {
            GL43.glEnable(GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS);
        } else if(khrDebug) {
            GL32.glEnable(KHRDebug.GL_DEBUG_OUTPUT_SYNCHRONOUS);
        }
    }

    public void textureView(int texture, int target, int origtexture, int internalformat, int minlevel, int numlevels, int minlayer, int numlayers) {
        if(openGl43) {
            GL43.glTextureView(texture, target, origtexture, internalformat, minlevel, numlevels, minlayer, numlayers);
        } else if(arbTextureView) {
            ARBTextureView.glTextureView(texture, target, origtexture, internalformat, minlevel, numlevels, minlayer, numlayers);
        }
    }

    public void drawArraysInstancedBaseInstance(int mode, int first, int count, int primcount, int baseinstance) {
        if(openGl42) {
            GL42.glDrawArraysInstancedBaseInstance(mode, first, count, primcount, baseinstance);
        } else if(arbBaseInstance) {
            ARBBaseInstance.glDrawArraysInstancedBaseInstance(mode, first, count, primcount, baseinstance);
        }
    }

    public void texStorage2D(int target, int levels, int internalformat, int width, int height) {
        if(openGl42) {
            GL42.glTexStorage2D(target, levels, internalformat, width, height);
        } else if(arbTextureStorage) {
            ARBTextureStorage.glTexStorage2D(target, levels, internalformat, width, height);
        }
    }

    public void bufferStorage(int target, long size, int flags) {
        if(openGl44) {
            GL44.glBufferStorage(target, size, flags);
        } else if(arbBufferStorage) {
            ARBBufferStorage.glBufferStorage(target, size, flags);
        }
    }

    public void vertexAttribDivisor(int index, int divisor) {
        if(openGl33) {
            GL33.glVertexAttribDivisor(index, divisor);
        } else if(arbInstancedArrays) {
            ARBInstancedArrays.glVertexAttribDivisorARB(index, divisor);
        }
    }
}
