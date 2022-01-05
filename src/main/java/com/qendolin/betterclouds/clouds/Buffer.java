package com.qendolin.betterclouds.clouds;

import com.qendolin.betterclouds.Main;
import com.qendolin.betterclouds.mixin.BufferRendererAccessor;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL32.*;

public class Buffer implements AutoCloseable {
    private final boolean supportsBufferStorage = GL.getCapabilities().GL_ARB_buffer_storage || GL.getCapabilities().OpenGL44;

    private final boolean usePersistent;
    private final int size;
    private final boolean fancy;

    private final int vaoId;
    private int drawBufferId;
    private int writeBufferId;
    private final int meshId;
    private final int baseMeshVertexCount;

    private FloatBuffer drawBuffer;
    private FloatBuffer writeBuffer;

    public Buffer(int size, boolean fancy, boolean usePersistent) {
        this.usePersistent = usePersistent && supportsBufferStorage;
        this.size = size;
        this.fancy = fancy;

        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        meshId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, meshId);
        float[] mesh = fancy ? Mesh.FANCY_MESH : Mesh.FAST_MESH;
        baseMeshVertexCount = fancy ? Mesh.FANCY_MESH_VERTEX_COUNT : Mesh.FAST_MESH_VERTEX_COUNT;
        glBufferData(GL_ARRAY_BUFFER, mesh, GL_STATIC_DRAW);

        if(fancy) {
            glEnableVertexAttribArray(1);
            glEnableVertexAttribArray(2);
            glVertexAttribPointer(1, 3, GL_FLOAT, false, Mesh.FANCY_MESH_VERTEX_SIZE*Float.BYTES, 0);
            glVertexAttribPointer(2, 3, GL_FLOAT, false, Mesh.FANCY_MESH_VERTEX_SIZE*Float.BYTES, 3*Float.BYTES);
        } else {
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
        }

        writeBufferId = glGenBuffers();
        drawBufferId = glGenBuffers();
        long vboSize = (long) size * size * 3 * Float.BYTES;
        if(usePersistent) {
            drawBufferId = glGenBuffers();

            int flags;
            if(GL.getCapabilities().OpenGL44) {
                flags = GL_MAP_WRITE_BIT | GL44.GL_MAP_PERSISTENT_BIT | GL44.GL_MAP_COHERENT_BIT;
            } else {
                flags = GL_MAP_WRITE_BIT | ARBBufferStorage.GL_MAP_PERSISTENT_BIT | ARBBufferStorage.GL_MAP_COHERENT_BIT;
            }
            glBindBuffer(GL_ARRAY_BUFFER, writeBufferId);
            glBufferStorage(GL_ARRAY_BUFFER, vboSize, flags);
            writeBuffer = glMapBufferRange(GL_ARRAY_BUFFER, 0, vboSize, flags).asFloatBuffer();

            glBindBuffer(GL_ARRAY_BUFFER, drawBufferId);
            glBufferStorage(GL_ARRAY_BUFFER, vboSize, flags);
            drawBuffer = glMapBufferRange(GL_ARRAY_BUFFER, 0, vboSize, flags).asFloatBuffer();
        } else {
            writeBuffer = MemoryUtil.memAllocFloat((int) (vboSize / Float.BYTES));

            glBindBuffer(GL_ARRAY_BUFFER, drawBufferId);
            glBufferData(GL_ARRAY_BUFFER, vboSize, GL_DYNAMIC_DRAW);
        }

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        if(GL.getCapabilities().OpenGL33) {
            GL33.glVertexAttribDivisor(0, 1);
        } else if(GL.getCapabilities().GL_ARB_instanced_arrays) {
            ARBInstancedArrays.glVertexAttribDivisorARB(0, 1);
        } else {
            Main.LOGGER.fatal("No glVertexAttribDivisor support");
        }

        restoreVao();
        restoreVbo();
    }

    public boolean hasChanged(int size, boolean fancy, boolean persistent) {
        return size != this.size || fancy != this.fancy || (supportsBufferStorage && persistent) != this.usePersistent;
    }

    private void glBufferStorage(int target, long size, int flags) {
        if(GL.getCapabilities().OpenGL44) {
            GL44.glBufferStorage(GL_ARRAY_BUFFER, size, flags);
        } else {
            ARBBufferStorage.glBufferStorage(GL_ARRAY_BUFFER, size, flags);
        }
    }

    public int baseMeshVertexCount() {
        return baseMeshVertexCount;
    }

    private void restoreVao() {
        int previousVaoId = BufferRendererAccessor.getCurrentVertexArray();
        if(previousVaoId > 0)
            glBindVertexArray(previousVaoId);
    }

    private void restoreVbo() {
        int previousVboId = BufferRendererAccessor.getCurrentVertexBuffer();
        if(previousVboId > 0)
            glBindBuffer(GL_ARRAY_BUFFER, previousVboId);
    }

    @Override
    public void close() {
        glDeleteVertexArrays(vaoId);
        glDeleteBuffers(drawBufferId);
        glDeleteBuffers(writeBufferId);
        glDeleteBuffers(meshId);
        if(!usePersistent) {
            MemoryUtil.memFree(writeBuffer);
        }
    }

    public void clear() {
        writeBuffer.clear();
    }

    public void put(float x, float y, float z) {
        writeBuffer.put(x);
        writeBuffer.put(y);
        writeBuffer.put(z);
    }

    public void swap() {
        if(usePersistent) {
            int tmpId = drawBufferId;
            FloatBuffer tmpBuffer = drawBuffer;
            drawBufferId = writeBufferId;
            drawBuffer = writeBuffer;
            writeBufferId = tmpId;
            writeBuffer = tmpBuffer;
            glBindBuffer(GL_ARRAY_BUFFER, drawBufferId);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        } else {
            glBindBuffer(GL_ARRAY_BUFFER, drawBufferId);
            writeBuffer.flip();
            glBufferSubData(GL_ARRAY_BUFFER, 0, writeBuffer);
        }
        restoreVbo();
    }

    public void bind() {
        glBindVertexArray(vaoId);
    }

    public void unbind() {
        restoreVao();
    }
}
