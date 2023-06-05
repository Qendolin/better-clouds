package com.qendolin.betterclouds.clouds;

import com.qendolin.betterclouds.mixin.BufferRendererAccessor;
import com.qendolin.betterclouds.mixin.VertexBufferAccessor;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static com.qendolin.betterclouds.Main.glCompat;
import static org.lwjgl.opengl.GL32.*;

public class Buffer implements AutoCloseable {
    private final boolean usePersistent;
    private final int size;
    private final boolean fancy;

    private final int vaoId;
    private int drawBufferId;
    private int writeBufferId;
    private final int meshId;
    private final int instanceVertexCount;

    // The draw buffer is null if usePersistent is false
    private FloatBuffer drawBuffer;
    private FloatBuffer writeBuffer;
    private int swapCount = 0;

    public Buffer(int size, boolean fancy, boolean usePersistent) {
        this.usePersistent = usePersistent && (glCompat.arbBufferStorage || glCompat.openGl44);
        this.size = size;
        this.fancy = fancy;

        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);
        glCompat.objectLabel(glCompat.GL_VERTEX_ARRAY, vaoId, "clouds_buffer");

        meshId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, meshId);
        float[] mesh = fancy ? Mesh.FANCY_MESH : Mesh.FAST_MESH;
        instanceVertexCount = fancy ? Mesh.FANCY_MESH_VERTEX_COUNT : Mesh.FAST_MESH_VERTEX_COUNT;
        glBufferData(GL_ARRAY_BUFFER, mesh, GL_STATIC_DRAW);
        glCompat.objectLabel(glCompat.GL_BUFFER, meshId, "cloud_mesh");

        if (fancy) {
            glEnableVertexAttribArray(1);
            glEnableVertexAttribArray(2);
            glVertexAttribPointer(1, 3, GL_FLOAT, false, Mesh.FANCY_MESH_VERTEX_SIZE * Float.BYTES, 0);
            glVertexAttribPointer(2, 3, GL_FLOAT, false, Mesh.FANCY_MESH_VERTEX_SIZE * Float.BYTES, 3 * Float.BYTES);
        } else {
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
        }

        writeBufferId = glGenBuffers();
        drawBufferId = glGenBuffers();
        long vboSize = (long) size * size * 3 * Float.BYTES;
        if (usePersistent) {
            int flags = GL_MAP_WRITE_BIT | glCompat.GL_MAP_PERSISTENT_BIT | glCompat.GL_MAP_COHERENT_BIT;
            glBindBuffer(GL_ARRAY_BUFFER, writeBufferId);
            glCompat.bufferStorage(GL_ARRAY_BUFFER, vboSize, flags);
            writeBuffer = glMapBufferRange(GL_ARRAY_BUFFER, 0, vboSize, flags).asFloatBuffer();
            glCompat.objectLabel(glCompat.GL_BUFFER, writeBufferId, "cloud_positions_a");

            glBindBuffer(GL_ARRAY_BUFFER, drawBufferId);
            glCompat.bufferStorage(GL_ARRAY_BUFFER, vboSize, flags);
            drawBuffer = glMapBufferRange(GL_ARRAY_BUFFER, 0, vboSize, flags).asFloatBuffer();
            glCompat.objectLabel(glCompat.GL_BUFFER, drawBufferId, "cloud_positions_b");
        } else {
            writeBuffer = MemoryUtil.memAllocFloat((int) (vboSize / Float.BYTES));
            glCompat.objectLabel(glCompat.GL_BUFFER, writeBufferId, "cloud_positions");

            glBindBuffer(GL_ARRAY_BUFFER, drawBufferId);
            glBufferData(GL_ARRAY_BUFFER, vboSize, GL_DYNAMIC_DRAW);
        }

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glCompat.vertexAttribDivisor(0, 1);
        glCompat.vertexAttribDivisor(3, 1);

        restoreVao();
        restoreVbo();
    }

    private void restoreVao() {
        VertexBufferAccessor buffer = (VertexBufferAccessor) BufferRendererAccessor.getCurrentVertexBuffer();
        if (buffer == null) return;
        int previousVaoId = buffer.getVertexArrayId();
        if (previousVaoId > 0)
            glBindVertexArray(previousVaoId);
    }

    private void restoreVbo() {
        VertexBufferAccessor buffer = (VertexBufferAccessor) BufferRendererAccessor.getCurrentVertexBuffer();
        if (buffer == null) return;
        int previousVboId = buffer.getVertexBufferId();
        if (previousVboId > 0)
            glBindBuffer(GL_ARRAY_BUFFER, previousVboId);
    }

    public boolean hasChanged(int size, boolean fancy, boolean persistent) {
        return size != this.size || fancy != this.fancy || ((glCompat.arbBufferStorage || glCompat.openGl44) && persistent) != this.usePersistent;
    }

    public FloatBuffer writeBuffer() {
        return writeBuffer;
    }

    public int instanceVertexCount() {
        return instanceVertexCount;
    }

    @Override
    public void close() {
        glDeleteVertexArrays(vaoId);
        glDeleteBuffers(drawBufferId);
        glDeleteBuffers(writeBufferId);
        glDeleteBuffers(meshId);
        if (!usePersistent) {
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

    /**
     * The buffer (the vao specifically) should be bound when calling this method
     */
    public void swap() {
        if (usePersistent) {
            int tmpId = drawBufferId;
            FloatBuffer tmpBuffer = drawBuffer;
            drawBufferId = writeBufferId;
            drawBuffer = writeBuffer;
            writeBufferId = tmpId;
            writeBuffer = tmpBuffer;
            glBindBuffer(GL_ARRAY_BUFFER, drawBufferId);
            // bind vbo to vao
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        } else {
            glBindBuffer(GL_ARRAY_BUFFER, drawBufferId);
            writeBuffer.flip();
            glBufferSubData(GL_ARRAY_BUFFER, 0, writeBuffer);
        }
        swapCount++;
    }

    public int swapCount() {
        return swapCount;
    }

    public void bind() {
        glBindVertexArray(vaoId);
    }

    public void unbind() {
        restoreVao();
    }
}
