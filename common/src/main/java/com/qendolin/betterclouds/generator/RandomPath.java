package com.qendolin.betterclouds.generator;

import com.qendolin.betterclouds.BetterCloudsStatic;
import net.minecraft.util.Mth;

import java.nio.ByteBuffer;

public class RandomPath {

    public static final int TICKS_PER_POINT = 20;
    public static int points;
    public static long pathWrap;
    private static int[] path;

    private static double getPathSmooth(long ticks, float tickDelta, double travelSpeed, int coordinateIndex) {
        // equal to (ticks + tickDelta) * speed / ticks
        // but for high values of ticks, ticks + tickDelta = ticks due to precision loss
        // so we do it like this instead
        double x = Math.max(0, ticks * travelSpeed / TICKS_PER_POINT + tickDelta * travelSpeed / TICKS_PER_POINT);

        int index = Mth.floor(x);
        double delta = Mth.frac(x);

        double p0 = getPointCoordinate(index, coordinateIndex);
        double p1 = getPointCoordinate(index + 1, coordinateIndex);
        double p2 = getPointCoordinate(index + 2, coordinateIndex);
        double p3 = getPointCoordinate(index + 3, coordinateIndex);

        return catmullRomInterpolate(p0, p1, p2, p3, delta);
    }

    private static double getPointCoordinate(long index, int coordinate) {
        long wraps = index / points;
        index -= wraps * points;
        int value = path[(int) (index * 2 + coordinate)];
        if (coordinate == 0) value += (int) (pathWrap * wraps);
        return value;
    }

    private static double catmullRomInterpolate(double p0, double p1, double p2, double p3, double t) {
        double t2 = t * t;
        double t3 = t2 * t;

        // Catmull-Rom spline formula
        return 0.5 * (
                (2 * p1) +
                        (-p0 + p2) * t +
                        (2 * p0 - 5 * p1 + 4 * p2 - p3) * t2 +
                        (-p0 + 3 * p1 - 3 * p2 + p3) * t3
        );
    }

    public static double getPathX(long ticks, float tickDelta, double travelSpeed) {
        return getPathSmooth(ticks, tickDelta, travelSpeed, 0);
    }

    public static double getPathZ(long ticks, float tickDelta, double travelSpeed) {
        return getPathSmooth(ticks, tickDelta, travelSpeed, 1);
    }

    public static void initialize() {
        int[] path = null;
        String name = "/static/path.bin";
        try (var res = RandomPath.class.getResourceAsStream(name)) {
            if (res == null) {
                BetterCloudsStatic.getLogger().error("Failed to open cloud path resource. name={}", name);
            } else {
                byte[] bytes = res.readAllBytes();
                ByteBuffer buffer = ByteBuffer.wrap(bytes);
                path = new int[bytes.length / Integer.BYTES];
                for (int i = 0; i < path.length; i++) {
                    path[i] = buffer.getInt();
                }
            }
        } catch (Exception e) {
            BetterCloudsStatic.getLogger().error("Failed to load cloud path", e);
        }
        if (path == null) {
            // use fallback path that just moves in -x direction
            path = new int[16];
            for (int i = 0; i < path.length; i += 2) {
                path[i] = -i * TICKS_PER_POINT;
                path[i + 1] = 0;
            }
        }
        RandomPath.path = path;
        points = RandomPath.path.length / 2;
        pathWrap = 2L * RandomPath.path[RandomPath.path.length - 2] - RandomPath.path[RandomPath.path.length - 4];
    }

}
