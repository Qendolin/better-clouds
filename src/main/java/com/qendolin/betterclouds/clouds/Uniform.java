package com.qendolin.betterclouds.clouds;

import org.apache.commons.lang3.NotImplementedException;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL32.*;

public abstract class Uniform {
    static final FloatBuffer UNIFORM_BUFFER = MemoryUtil.memAllocFloat(16);

    protected final String name;
    protected final int location;

    protected Uniform(String name, int location) {
        this.name = name;
        this.location = location;
    }

    public abstract void setMat4(Matrix4f mat);

    public abstract void setVec4(float x, float y, float z, float w);

    public abstract void setVec3(float x, float y, float z);

    public abstract void setVec3(Vector3f v);

    public abstract void setVec2(float x, float y);

    public abstract void setFloat(float f);

    public abstract void setInt(int i);

    @Override
    public String toString() {
        return "Uniform{" + "name='" + name + "'" + ", location=" + location + '}';
    }

    public static class Noop extends Uniform {
        protected Noop(String name, int location) {
            super(name, location);
        }

        @Override
        public void setMat4(Matrix4f mat) {}

        @Override
        public void setVec4(float x, float y, float z, float w) {}

        @Override
        public void setVec3(float x, float y, float z) {}

        @Override
        public void setVec3(Vector3f v) {}

        @Override
        public void setVec2(float x, float y) {}

        @Override
        public void setFloat(float f) {}

        @Override
        public void setInt(int i) {}
    }

    public static class Simple extends Uniform {
        protected Simple(String name, int location) {
            super(name, location);
        }

        @Override
        public void setMat4(Matrix4f mat) {
//            mat.writeColumnMajor(UNIFORM_BUFFER);
            mat.get(UNIFORM_BUFFER);
            UNIFORM_BUFFER.rewind();
            glUniformMatrix4fv(location, false, UNIFORM_BUFFER);
        }

        @Override
        public void setVec4(float x, float y, float z, float w) {
            glUniform4f(location, x, y, z, w);
        }

        @Override
        public void setVec3(float x, float y, float z) {
            glUniform3f(location, x, y, z);
        }

        @Override
        public void setVec3(Vector3f v) {
            glUniform3f(location, v.x, v.y, v.z);
        }

        @Override
        public void setVec2(float x, float y) {
            glUniform2f(location, x, y);
        }

        @Override
        public void setFloat(float f) {
            glUniform1f(location, f);
        }

        @Override
        public void setInt(int i) {
            glUniform1i(location, i);
        }
    }

    public static class Cached extends Uniform {
        private final float[] cache = {-1, -1, -1, -1};

        protected Cached(String name, int location) {
            super(name, location);
        }

        private boolean checkCache(float x, float y, float z, float w) {
            return x == cache[0] && y == cache[1] && z == cache[2] && w == cache[3];
        }
        private void setCache(float x, float y, float z, float w) {
            cache[0] = x; cache[1] = y; cache[2] = z; cache[3] = w;
        }

        @Override
        public void setMat4(Matrix4f mat) {
            throw new NotImplementedException();
        }

        @Override
        public void setVec4(float x, float y, float z, float w) {
            if(checkCache(x, y, z, w)) return;
            setCache(x, y, z, w);
            glUniform4f(location, x, y, z, w);
        }

        @Override
        public void setVec3(float x, float y, float z) {
            if(checkCache(x, y, z, 0)) return;
            setCache(x, y, z, 0);
            glUniform3f(location, x, y, z);
        }

        @Override
        public void setVec3(Vector3f v) {
            this.setVec3(v.x, v.y, v.z);
        }

        @Override
        public void setVec2(float x, float y) {
            if(checkCache(x, y, 0, 0)) return;
            setCache(x, y, 0, 0);
            glUniform2f(location, x, y);
        }

        @Override
        public void setFloat(float f) {
            if(checkCache(f, 0, 0, 0)) return;
            setCache(f, 0, 0, 0);
            glUniform1f(location, f);
        }

        @Override
        public void setInt(int i) {
            throw new NotImplementedException();
        }
    }
}
