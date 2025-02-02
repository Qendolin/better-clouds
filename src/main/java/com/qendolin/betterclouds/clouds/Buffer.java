package com.qendolin.betterclouds.clouds;

import com.qendolin.betterclouds.Main;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL43;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static com.qendolin.betterclouds.Main.getConfig;
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
    private long prevInstancePointer = -1;

    public int compactBufferId;
    public int cullingBufferId;
    public ByteBuffer cullingBuffer;
    public int atomicCounterId;
    public int drawIndirectBufferId;

    public int tmp_drawCommandCount;

    public Buffer(int size, boolean fancy, boolean preferPersistent) {
        boolean usePersistent = preferPersistent && (glCompat.arbBufferStorage || glCompat.openGl44);
        this.size = size;
        this.fancy = fancy;

        float[] mesh = fancy ? Mesh.FANCY_MESH : Mesh.FAST_MESH;
        instanceVertexCount = fancy ? Mesh.FANCY_MESH_VERTEX_COUNT : Mesh.FAST_MESH_VERTEX_COUNT;

        compactBufferId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, compactBufferId);
        glCompat.objectLabelDev(glCompat.GL_BUFFER, compactBufferId, "compact_buffer");
        glBufferData(GL_ARRAY_BUFFER, (long) size * size * 4 * Float.BYTES, GL_DYNAMIC_DRAW);

        cullingBufferId = glGenBuffers();
        glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, cullingBufferId);
        glCompat.objectLabelDev(glCompat.GL_BUFFER, cullingBufferId, "culling_buffer");
        glCompat.bufferStorage(GL43.GL_SHADER_STORAGE_BUFFER, (long) size * size * 4 * Float.BYTES, GL_MAP_WRITE_BIT | glCompat.GL_MAP_PERSISTENT_BIT | glCompat.GL_MAP_COHERENT_BIT);
        // todo: use chunk size instead of 32
        long cullingBufferSize = MathHelper.square(MathHelper.ceilDiv(size, 32)) * (4 * Float.BYTES + 2 * Integer.BYTES);
        cullingBuffer = glMapBufferRange(GL43.GL_SHADER_STORAGE_BUFFER, 0, cullingBufferSize, GL_MAP_WRITE_BIT | glCompat.GL_MAP_PERSISTENT_BIT | glCompat.GL_MAP_COHERENT_BIT);
        atomicCounterId = glGenBuffers();
        glBindBuffer(GL43.GL_ATOMIC_COUNTER_BUFFER, atomicCounterId);
        glCompat.objectLabelDev(glCompat.GL_BUFFER, atomicCounterId, "atomic_counter");
        glBufferData(GL43.GL_ATOMIC_COUNTER_BUFFER, Integer.BYTES, GL_DYNAMIC_DRAW);
        drawIndirectBufferId = glGenBuffers();
        glBindBuffer(GL43.GL_DRAW_INDIRECT_BUFFER, drawIndirectBufferId);
        glCompat.objectLabelDev(glCompat.GL_BUFFER, drawIndirectBufferId, "draw_indirect_buffer");
//        int drawCommandCount = MathHelper.ceilDiv(size * size, 64);
        int drawCommandCount = MathHelper.square(MathHelper.ceilDiv(2 * getConfig().blockDistance(), 32));
        tmp_drawCommandCount = drawCommandCount;
        glBufferData(GL43.GL_DRAW_INDIRECT_BUFFER, (long) drawCommandCount * 4 * Integer.BYTES, GL_DYNAMIC_DRAW);
        var drawCommandStaticData = new int[drawCommandCount * 4];
        for (int i = 0; i < drawCommandStaticData.length; i += 4) {
            drawCommandStaticData[i] = instanceVertexCount; // vertex count
//            drawCommandStaticData[i+3] = 64 * (i / 4); // base instance
        }
        glBufferSubData(GL43.GL_DRAW_INDIRECT_BUFFER, 0, drawCommandStaticData);

        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);
        glCompat.objectLabelDev(glCompat.GL_VERTEX_ARRAY, vaoId, "clouds_buffer");

        meshId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, meshId);
        glBufferData(GL_ARRAY_BUFFER, mesh, GL_STATIC_DRAW);
        glCompat.objectLabelDev(glCompat.GL_BUFFER, meshId, "cloud_mesh");

        if (fancy) {
//            glEnableVertexAttribArray(1);
//            glEnableVertexAttribArray(2);
//            glVertexAttribPointer(1, 3, GL_FLOAT, false, Mesh.FANCY_MESH_VERTEX_SIZE * Float.BYTES, 0);
//            glVertexAttribPointer(2, 3, GL_FLOAT, false, Mesh.FANCY_MESH_VERTEX_SIZE * Float.BYTES, 3 * Float.BYTES);
        } else {
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
        }

        writeBufferId = glGenBuffers();
        drawBufferId = glGenBuffers();
        if (size <= 0) {
            // There is no way for the size to be zero or less, but I've reports of it happening regardless.
            Main.LOGGER.error("Impossible, invalid buffer size of {}, forcing it to 1", size);
            size = 1;
        }
        long vboSize = (long) size * size * 4 * Float.BYTES;
        if (usePersistent) {
            try {
                allocatePersistent(vboSize);
            } catch (IllegalStateException e) {
                Main.getConfig().usePersistentBuffers = false;
                Main.getConfigHandler().save();
                usePersistent = false;
                Main.LOGGER.error(e);
            }
        }

        if (!usePersistent) {
            allocateMutable(vboSize);
        }

        this.usePersistent = usePersistent;

        glBindBuffer(GL_ARRAY_BUFFER, compactBufferId);
        glEnableVertexAttribArray(0);
        setVAPointerToInstance(0);
        glCompat.vertexAttribDivisor(0, 1);

        Resources.unbindVao();
        Resources.unbindVbo();
    }

    private void allocatePersistent(long vboSize) {
        int flags = GL_MAP_WRITE_BIT | glCompat.GL_MAP_PERSISTENT_BIT | glCompat.GL_MAP_COHERENT_BIT;
        glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, writeBufferId);
        glCompat.bufferStorage(GL43.GL_SHADER_STORAGE_BUFFER, vboSize, flags);
        ByteBuffer buffer = glMapBufferRange(GL43.GL_SHADER_STORAGE_BUFFER, 0, vboSize, flags);
        if (buffer == null) throw new IllegalStateException("glMapBufferRange returned null");
        writeBuffer = buffer.asFloatBuffer();
        glCompat.objectLabelDev(glCompat.GL_BUFFER, writeBufferId, "cloud_positions_a");

        glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, drawBufferId);
        glCompat.bufferStorage(GL43.GL_SHADER_STORAGE_BUFFER, vboSize, flags);
        buffer = glMapBufferRange(GL43.GL_SHADER_STORAGE_BUFFER, 0, vboSize, flags);
        if (buffer == null) throw new IllegalStateException("glMapBufferRange returned null");
        drawBuffer = buffer.asFloatBuffer();
        glCompat.objectLabelDev(glCompat.GL_BUFFER, drawBufferId, "cloud_positions_b");
    }

    private void allocateMutable(long vboSize) {
        writeBuffer = MemoryUtil.memAllocFloat((int) (vboSize / Float.BYTES));
        glCompat.objectLabelDev(glCompat.GL_BUFFER, writeBufferId, "cloud_positions");

        glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, drawBufferId);
        glBufferData(GL43.GL_SHADER_STORAGE_BUFFER, vboSize, GL_DYNAMIC_DRAW);
    }

    public void setVAPointerToInstance(int baseInstance) {
        // The caller must bind the vao and vbo
        int stride = Float.BYTES * 4;
        long pointer = (long) stride * baseInstance;
        if (pointer == prevInstancePointer) return;
        prevInstancePointer = pointer;
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, pointer);
    }

    public boolean hasChanged(int size, boolean fancy, boolean persistent) {
        return size != this.size || fancy != this.fancy || ((glCompat.arbBufferStorage || glCompat.openGl44) && persistent) != this.usePersistent;
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
        writeBuffer.put(0);
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
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 4 * Float.BYTES, 0);
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

    public void bindDrawBuffer() {
        glBindBuffer(GL_ARRAY_BUFFER, drawBufferId);
    }

    public void unbind() {
        Resources.unbindVao();
    }

    public int drawBufferId() {
        return drawBufferId;
    }
}
