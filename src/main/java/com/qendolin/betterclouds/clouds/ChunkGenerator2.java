package com.qendolin.betterclouds.clouds;

import net.minecraft.util.math.MathHelper;

import java.nio.FloatBuffer;

public class ChunkGenerator2 {

    public static int GEN_CHUNK_SIZE = 128;
    public static int MAX_SUB_LVL = 4;
    public static int GEN_CHUNK_SIZE_2 = GEN_CHUNK_SIZE * GEN_CHUNK_SIZE;

    // inverse coordinate to index table, contains packed x,z positions
    long[] index;
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
    FloatBuffer buffer;

    int cx = 0;
    int cz = 0;

    float spacing;

    public ChunkGenerator2(FloatBuffer buffer, int size, float spacing) {
        this.buffer = buffer;
        this.size = size;
        this.spacing = spacing;
        this.index = new long[size * size];
        this.generationOrder = new short[GEN_CHUNK_SIZE_2][];
        this.generationOrderInverse = new short[GEN_CHUNK_SIZE_2];
        initGenerationOrder();
    }

    private void initGenerationOrder() {
        initGenerationOrderRecursive(0, 0, 0, 0, GEN_CHUNK_SIZE);
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

    private void initGenerationOrderRecursive(int bi, int bx, int bz, int lvl, int size) {
        int ci = 0;
        if(lvl == MAX_SUB_LVL) {
            for(int dz = 0; dz < size; dz++) {
                for(int dx = 0; dx < size; dx++) {
                    generationOrder[bi + ci] = new short[] {(short) (bx + dx), (short) (bz + dz)};
                    generationOrderInverse[bx + dx + GEN_CHUNK_SIZE * (bz + dz)] = (short) (bi + ci);
                    ci++;
                }
            }
            return;
        }
        int di = countOf(lvl+1);
        for (int dz = 0; dz < size; dz += size / 2) {
            for (int dx = 0; dx < size; dx += size / 2) {
                initGenerationOrderRecursive(bi + di * ci, bx + dx, bz + dz, lvl+1, size/2);
                ci++;
            }
        }
    }

    // result is always odd
    public static int calculateSize(float blockDistance, float spacing) {
        int points = MathHelper.ceil(2 * blockDistance / spacing);
        // 128 points per gen chunk
        return MathHelper.ceilDiv(points, GEN_CHUNK_SIZE * 2) * 2 + 1;
    }

    public int clouds() {
        return MathHelper.square(size * GEN_CHUNK_SIZE);
    }

    public float[] bounds(int i) {
        int x = (int) this.index[i];
        int z = (int) (this.index[i] >> 32);

        return new float[]{
            x * spacing * GEN_CHUNK_SIZE,
            z * spacing * GEN_CHUNK_SIZE,
            (x + 1) * spacing * GEN_CHUNK_SIZE,
            (z + 1) * spacing * GEN_CHUNK_SIZE
        };
    }

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

    public float[] bounds(int i, int lvl, int sub) {
        if (lvl == 0) return bounds(i);
        // base
        int bx = GEN_CHUNK_SIZE * (int) this.index[i];
        int bz = GEN_CHUNK_SIZE * (int) (this.index[i] >> 32);

        int dsize = GEN_CHUNK_SIZE >> lvl;
        int index = sub * dsize * dsize;
        int dx = this.generationOrder[index][0];
        int dz = this.generationOrder[index][1];

        return new float[]{
            (bx + dx) * spacing,
            (bz + dz) * spacing,
            (bx + dx + dsize) * spacing,
            (bz + dz + dsize) * spacing
        };
    }

    public float[] bounds(int i, int lvl, int x, int z) {
        if (lvl == 0) return bounds(i);
        // base
        int bx = GEN_CHUNK_SIZE * (int) this.index[i];
        int bz = GEN_CHUNK_SIZE * (int) (this.index[i] >> 32);

        int dsize = GEN_CHUNK_SIZE >> lvl;
        x *= dsize;
        z *= dsize;
        int index = this.generationOrderInverse[x + GEN_CHUNK_SIZE * z];
        int dx = this.generationOrder[index][0];
        int dz = this.generationOrder[index][1];

        return new float[]{
            (bx + dx) * spacing,
            (bz + dz) * spacing,
            (bx + dx + dsize) * spacing,
            (bz + dz + dsize) * spacing
        };
    }

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
        int index = this.generationOrderInverse[x + GEN_CHUNK_SIZE * z];
        int dx = this.generationOrder[index][0];
        int dz = this.generationOrder[index][1];

        out[0] = (bx + dx) * spacing;
        out[1] = (bz + dz) * spacing;
        out[2] = (bx + dx + dsize) * spacing;
        out[3] = (bz + dz + dsize) * spacing;
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

        int sliceLength = GEN_CHUNK_SIZE_2 * 4;
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

                FloatBuffer slice = buffer.slice(i * sliceLength, sliceLength);
                generate(slice, rx, rz);
            }
        } else {
            initialized = true;
            int i = 0;
            for (int xx = minx; xx <= maxx; xx++) {
                for (int zz = minz; zz <= maxz; zz++) {
                    FloatBuffer slice = buffer.slice(i * sliceLength, sliceLength);
                    generate(slice, xx, zz);
                    long packed = (((long) zz) << 32) | (xx & 0xffffffffL);
                    this.index[i++] = packed;
                }
            }
        }

        this.minx = minx;
        this.maxx = maxx;
        this.minz = minz;
        this.maxz = maxz;
        this.cx = cx;
        this.cz = cz;
        return true;
    }

    public void generate(FloatBuffer slice, int chunkx, int chunkz) {
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
            float y = value * 64;

            slice.put(x);
            slice.put(y);
            slice.put(z);
            slice.put(0); // vec3 padding
//            }
        }
    }


}
