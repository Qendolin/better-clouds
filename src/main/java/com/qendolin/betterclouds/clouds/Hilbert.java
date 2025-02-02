package com.qendolin.betterclouds.clouds;

public class Hilbert {
    /**
     * Converts 2D coordinates to the corresponding Hilbert curve index.
     *
     * @param n  Size of the grid (must be a power of 2)
     * @param xy Array containing the x and y coordinates
     * @return Hilbert index corresponding to the coordinates
     */
    public static int coordsToHilbertIndex(int n, int[] xy) {
        int index = 0;
        int x = xy[0];
        int y = xy[1];

        for (int s = n / 2; s > 0; s /= 2) {
            int rx = (x & s) > 0 ? 1 : 0;
            int ry = (y & s) > 0 ? 1 : 0;

            index += s * s * ((3 * rx) ^ ry);
            rotate(n, xy, rx, ry);
        }

        return index;
    }

    /**
     * Converts a Hilbert curve index to the corresponding 2D coordinates.
     *
     * @param n     Size of the grid (must be a power of 2)
     * @param index Hilbert index
     * @return Array containing the x and y coordinates
     */
    public static int[] hilbertIndexToCoords(int n, int index) {
        int[] xy = new int[]{0, 0};

        for (int s = 1; s < n; s *= 2) {
            int rx = (index / 2) & 1;
            int ry = (index ^ rx) & 1;

            rotate(s, xy, rx, ry);

            xy[0] += s * rx;
            xy[1] += s * ry;
            index /= 4;
        }

        return xy;
    }

    /**
     * Rotates or flips a quadrant appropriately.
     *
     * @param n  Size of the grid (must be a power of 2)
     * @param xy Array containing the x and y coordinates
     * @param rx X coordinate bit (0 or 1)
     * @param ry Y coordinate bit (0 or 1)
     */
    private static void rotate(int n, int[] xy, int rx, int ry) {
        if (ry == 0) {
            // Rotate or flip based on rx
            if (rx == 1) {
                xy[0] = n - 1 - xy[0];
                xy[1] = n - 1 - xy[1];
            }

            // Swap x and y
            int temp = xy[0];
            xy[0] = xy[1];
            xy[1] = temp;
        }
    }
}
