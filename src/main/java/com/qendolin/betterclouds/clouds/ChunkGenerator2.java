package com.qendolin.betterclouds.clouds;

import net.minecraft.util.math.MathHelper;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ChunkGenerator2 {

    public static int GEN_CHUNK_SIZE = 128;
    public static int MAX_SUB_LVL = 4;
    public static int GEN_CHUNK_SIZE_2 = GEN_CHUNK_SIZE * GEN_CHUNK_SIZE;
    public static int BYTES_PER_COORD = 1;

    private final Sampler sampler = new Sampler();

    // inverse coordinate to index table, contains packed x,z positions
    long[] index;
    int[] regionOffsets;
    //    final short[] generationOrder;
    final short[][] generationOrder;
    final short[] generationOrderInverse;
    // number of chunks along one axis
    int size;
    // min and max chunk positions (inclusive)
    int minx;
    int maxx;
    int minz;
    int maxz;
    boolean initialized = false;
    ByteBuffer buffer;

    int cx = 0;
    int cz = 0;

    float spacing;

    RegionMap regionMap;

    public ChunkGenerator2(ByteBuffer buffer, int size, float spacing) {
        this.buffer = buffer;
        this.size = size;
        this.spacing = spacing;
        this.index = new long[size * size];
        this.regionOffsets = new int[size * size * 2];
        this.regionMap = new RegionMap(size);
        this.generationOrder = new short[GEN_CHUNK_SIZE_2][];
        this.generationOrderInverse = new short[GEN_CHUNK_SIZE_2];
        initGenerationOrder();
    }


    private static int interleaveBits(int x) {
        x = (x | (x << 8)) & 0x00FF00FF;
        x = (x | (x << 4)) & 0x0F0F0F0F;
        x = (x | (x << 2)) & 0x33333333;
        x = (x | (x << 1)) & 0x55555555;
        return x;
    }

    private static int encodeMorton(int x, int y) {
        return (interleaveBits(y) << 1) | interleaveBits(x);
    }

    private static int deinterleaveBits(int x) {
        x &= 0x55555555;
        x = (x | (x >> 1)) & 0x33333333;
        x = (x | (x >> 2)) & 0x0F0F0F0F;
        x = (x | (x >> 4)) & 0x00FF00FF;
        x = (x | (x >> 8)) & 0x0000FFFF;
        return x;
    }

    private static int[] decodeMorton(int index) {
        int x = deinterleaveBits(index);
        int y = deinterleaveBits(index >> 1);
        return new int[]{x, y};
    }


    private void initGenerationOrder() {
//        initGenerationOrderRecursive(0, 0, 0, 0, GEN_CHUNK_SIZE);

        for (int i = 0; i < GEN_CHUNK_SIZE_2; i++) {
            var xy = decodeMorton(i);
            generationOrder[i] = new short[]{(short) xy[0], (short) xy[1]};
            generationOrderInverse[xy[0] + xy[1] * GEN_CHUNK_SIZE] = (short) i;
        }

//        for (int i = 0; i < GEN_CHUNK_SIZE_2; i++) {
//            int[] coords = Hilbert.hilbertIndexToCoords(GEN_CHUNK_SIZE, i);
////            generationOrder[i] = (short) ((coords[1]) << 8 | (coords[0] & 0xffL));
//            generationOrder[i] = new short[] {(short) coords[0], (short) coords[1]};
//        }

//        int i = 0;
//        int minsize = GEN_CHUNK_SIZE >> MAX_SUB_LVL;
//        for (int oz = 0; oz < GEN_CHUNK_SIZE; oz += minsize) {
//            for (int ox = 0; ox < GEN_CHUNK_SIZE; ox += minsize) {
//                for (int iz = 0; iz < minsize; iz++) {
//                    for (int ix = 0; ix < minsize; ix++) {
////                        generationOrder[i++] = (short) ((oz + iz) << 8 | ((ox + ix) & 0xffL));
//                        generationOrder[i++] = new short[] {(short) (ox + ix), (short) (oz+iz)};
//                    }
//                }
//            }
//        }
    }

//    private void initGenerationOrderRecursive(int bi, int bx, int bz, int lvl, int size) {
//        int ci = 0;
//        if(lvl == MAX_SUB_LVL) {
//            for(int dz = 0; dz < size; dz++) {
//                for(int dx = 0; dx < size; dx++) {
//                    generationOrder[bi + ci] = new short[] {(short) (bx + dx), (short) (bz + dz)};
//                    generationOrderInverse[bx + dx + GEN_CHUNK_SIZE * (bz + dz)] = (short) (bi + ci);
//                    ci++;
//                }
//            }
//            return;
//        }
//        int di = countOf(lvl+1);
//        for (int dz = 0; dz < size; dz += size / 2) {
//            for (int dx = 0; dx < size; dx += size / 2) {
//                initGenerationOrderRecursive(bi + di * ci, bx + dx, bz + dz, lvl+1, size/2);
//                ci++;
//            }
//        }
//    }

    // result is always odd
    public static int calculateSize(float blockDistance, float spacing) {
        int points = MathHelper.ceil(2 * blockDistance / spacing);
        // 128 points per gen chunk
        return MathHelper.ceilDiv(points, GEN_CHUNK_SIZE * 2) * 2 + 1;
    }

    public int clouds() {
        return MathHelper.square(size * GEN_CHUNK_SIZE);
    }

//    public float[] bounds(int i) {
//        int x = (int) this.index[i];
//        int z = (int) (this.index[i] >> 32);
//
//        return new float[]{
//            x * spacing * GEN_CHUNK_SIZE,
//            z * spacing * GEN_CHUNK_SIZE,
//            (x + 1) * spacing * GEN_CHUNK_SIZE,
//            (z + 1) * spacing * GEN_CHUNK_SIZE
//        };
//    }

    public void bounds(int i, float[] out) {
        int x = (int) this.index[i];
        int z = (int) (this.index[i] >> 32);

        out[0] = x * spacing * GEN_CHUNK_SIZE;
        out[1] = z * spacing * GEN_CHUNK_SIZE;
        out[2] = (x + 1) * spacing * GEN_CHUNK_SIZE;
        out[3] = (z + 1) * spacing * GEN_CHUNK_SIZE;
    }

//    public float[] bounds(int i, int lvl, int sub) {
//        if (lvl == 0) return bounds(i);
//        // base
//        int bx = GEN_CHUNK_SIZE * (int) this.index[i];
//        int bz = GEN_CHUNK_SIZE * (int) (this.index[i] >> 32);
//
//        int dsize = GEN_CHUNK_SIZE >> lvl;
//
//        int mask = ((1 << lvl) - 1);
//        int dx = dsize * (sub & mask);
//        int dz = dsize * (sub >> lvl);
//
//        return new float[]{
//            (bx + dx) * spacing,
//            (bz + dz) * spacing,
//            (bx + dx + dsize) * spacing,
//            (bz + dz + dsize) * spacing
//        };
//    }

//    public float[] bounds(int i, int lvl, int sub) {
//        if (lvl == 0) return bounds(i);
//        // base
//        int bx = GEN_CHUNK_SIZE * (int) this.index[i];
//        int bz = GEN_CHUNK_SIZE * (int) (this.index[i] >> 32);
//
//        int dsize = GEN_CHUNK_SIZE >> lvl;
//        int index = sub * dsize * dsize;
//        int dx = this.generationOrder[index][0];
//        int dz = this.generationOrder[index][1];
//
//        return new float[]{
//            (bx + dx) * spacing,
//            (bz + dz) * spacing,
//            (bx + dx + dsize) * spacing,
//            (bz + dz + dsize) * spacing
//        };
//    }

//    public float[] bounds(int i, int lvl, int x, int z) {
//        if (lvl == 0) return bounds(i);
//        // base
//        int bx = GEN_CHUNK_SIZE * (int) this.index[i];
//        int bz = GEN_CHUNK_SIZE * (int) (this.index[i] >> 32);
//
//        int dsize = GEN_CHUNK_SIZE >> lvl;
//        x *= dsize;
//        z *= dsize;
//        int index = this.generationOrderInverse[x + GEN_CHUNK_SIZE * z];
//        int dx = this.generationOrder[index][0];
//        int dz = this.generationOrder[index][1];
//
//        return new float[]{
//            (bx + dx) * spacing,
//            (bz + dz) * spacing,
//            (bx + dx + dsize) * spacing,
//            (bz + dz + dsize) * spacing
//        };
//    }

    public void bounds(int i, int lvl, int x, int z, float[] out) {
        if (lvl == 0) {
            bounds(i, out);
            return;
        }
        // base
        int bx = GEN_CHUNK_SIZE * (int) this.index[i];
        int bz = GEN_CHUNK_SIZE * (int) (this.index[i] >> 32);

        int dsize = GEN_CHUNK_SIZE >> lvl;
        x *= dsize;
        z *= dsize;
//        int index = this.generationOrderInverse[x + GEN_CHUNK_SIZE * z];
//        int dx = this.generationOrder[index][0];
//        int dz = this.generationOrder[index][1];

        out[0] = (bx + x) * spacing;
        out[1] = (bz + z) * spacing;
        out[2] = (bx + x + dsize) * spacing;
        out[3] = (bz + z + dsize) * spacing;
    }

    public int startOf(int i, int lvl, int sub) {
        int base = i * GEN_CHUNK_SIZE_2;
        int dcount = countOf(lvl);
        return base + sub * dcount;
    }


    public int startOf(int i, int lvl, int x, int z) {
        int base = i * GEN_CHUNK_SIZE_2;
//        int dcount = countOf(lvl);
        int dsize = GEN_CHUNK_SIZE >> lvl;
        x *= dsize;
        z *= dsize;
        int index = this.generationOrderInverse[x + GEN_CHUNK_SIZE * z];
        return base + index; // * dcount;
    }

//    public int startOf(int i, int lvl, int x, int y) {
//        int base = i * GEN_CHUNK_SIZE_2;
//        int dcount = countOf(lvl);
//        return base + sub * dcount;
//    }

    public int countOf(int lvl) {
        int size = GEN_CHUNK_SIZE >> lvl;
        return size * size;
    }

    // Idea: decouple generation chunks from visibility chunks.
    // One generation chunk is subdivided into multiple visibility chunks.
    // The vis chunks are always contiguous in memory.
    // For generation parallelization the gen chunks can also be subdivided
    //
    // Open issue: How to make coords local?
    // Store offset per gen chunk?

    // TODO:
    // Use names: region, chunk and section
    // A region has an offset and coherent memory
    // A chunk is a generation unit for parallelization
    // A section is the smallest culling unit
    public boolean update(double x, double z) {
        // grid center
        int cx = MathHelper.floor(x / (spacing * GEN_CHUNK_SIZE));
        int cz = MathHelper.floor(z / (spacing * GEN_CHUNK_SIZE));

        // mc view distance gives the radius, a vd=2 equals a 5x5 square

        // center didn't move
        if (initialized && cx == this.cx && cz == this.cz) {
            return false;
        }

        int minx = cx - size / 2;
        int minz = cz - size / 2;
        int maxx = cx + size / 2;
        int maxz = cz + size / 2;

        int sliceLength = GEN_CHUNK_SIZE_2 * BYTES_PER_COORD;
        if (initialized) {
            for (int i = 0; i < this.index.length; i++) {
                int xx = (int) this.index[i];
                int zz = (int) (this.index[i] >> 32);
                int rx = xx;
                int rz = zz;

                // calculate replacement positions by mirroring
                if (xx > maxx) rx = minx - (xx - this.maxx);
                else if (xx < minx) rx = maxx - (xx - this.minx);
                if (zz > maxz) rz = minz - (zz - this.maxz);
                else if (zz < minz) rz = maxz - (zz - this.minz);

                // chunk is inside new bounds, keep it
                if (xx == rx && zz == rz) continue;

                long packed = (((long) rz) << 32) | (rx & 0xffffffffL);
                this.index[i] = packed;

                ByteBuffer slice = buffer.slice(i * sliceLength, sliceLength);
                generate(slice, rx, rz, rx - minx, rz - minz);
            }

            int dx = cx - this.cx;
            int dz = cz - this.cz;
            regionMap.shift(dx, dz);
        } else {
            initialized = true;
            int i = 0;
            for (int zz = minz; zz <= maxz; zz++) {
                for (int xx = minx; xx <= maxx; xx++) {
                    ByteBuffer slice = buffer.slice(i * sliceLength, sliceLength);
                    generate(slice, xx, zz, xx - minx, zz - minz);
                    long packed = (((long) zz) << 32) | (xx & 0xffffffffL);
                    this.index[i++] = packed;
                }
            }
        }

        for (int i = 0; i < this.index.length; i++) {
            int xx = (int) this.index[i];
            int zz = (int) (this.index[i] >> 32);
            this.regionOffsets[i * 2] = xx;
            this.regionOffsets[i * 2 + 1] = zz;
        }

        this.minx = minx;
        this.maxx = maxx;
        this.minz = minz;
        this.maxz = maxz;
        this.cx = cx;
        this.cz = cz;
        return true;
    }

    public void generate(ByteBuffer slice, int chunkx, int chunkz, int rx, int rz) {
        slice.clear();
        int index = 0;
        int length = GEN_CHUNK_SIZE_2;
        // local pos
        for (int i = 0; i < generationOrder.length; i++) {
//            short packed = generationOrder[i];
//            int lx = packed & 0xff;
//            int lz = packed >> 8;
            int lx = generationOrder[i][0];
            int lz = generationOrder[i][1];
            lx = i % GEN_CHUNK_SIZE;
            lz = i / GEN_CHUNK_SIZE;
//        }
//        for (int lz = 0; lz < GEN_CHUNK_SIZE; lz++) {
//            for (int lx = 0; lx < GEN_CHUNK_SIZE; lx++) {
            // grid pos
            int gx = chunkx * GEN_CHUNK_SIZE + lx;
            int gz = chunkz * GEN_CHUNK_SIZE + lz;

            // real pos
            float x = gx * spacing;
            float z = gz * spacing;

            float value = (float) index++ / length;
//            value = (float) Math.random();
//            value = 0;
            value = sampler.sample(MathHelper.floor(x), MathHelper.floor(z), 0.5f, 0f, 1f);
//            value = (rx + size * rz) / 255f;
            if (value <= 0) value = 0;
            value = value * value;
//            value = new Vector2f(lx, lz).div(128).length();

//            slice.put(x);
            value = MathHelper.clamp(value, 0, 1);
//            slice.put((byte) MathHelper.ceil(value * 255 + Byte.MIN_VALUE));
            slice.put((byte) (MathHelper.ceil(value * 255) & 0xff));
//            slice.put(z);
//            slice.put(0); // vec3 padding
//            }
        }
    }


    public static class RegionMap {

        final int size;
        final int[] map;

        public RegionMap(int size) {
            this.size = size;
            this.map = new int[size * size];
            for (int i = 0; i < this.map.length; i++) {
                this.map[i] = i;
            }
        }

        public void shift(int dx, int dz) {
            int[] old = Arrays.copyOf(map, map.length);
            // make d [0, size)
            dx = ((dx % size) + size) % size;
            dz = ((dz % size) + size) % size;
            for (int row = 0; row < size; row++) {
                for (int col = 0; col < size; col++) {
                    int rowFrom = (row + size - dz) % size;
                    int colFrom = (col + size - dx) % size;
                    int from = rowFrom * size + colFrom;
                    int to = row * size + col;
                    int value = old[from];
                    map[to] = value;
                }
            }
        }

        public int[] values() {
            return map;
        }

    }

}
