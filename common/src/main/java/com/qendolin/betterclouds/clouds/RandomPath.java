package com.qendolin.betterclouds.clouds;

import com.qendolin.betterclouds.BetterCloudsStatic;
import java.nio.ByteBuffer;
import net.minecraft.util.Mth;

public class RandomPath {

    private static final int TICKS_PER_POINT = 20;
    private static int[] path;

    private static int points;
    private static int pathWrap;

    private static double getPathLinear(double time, double travelSpeed, int coordinateIndex) {
        double x = time / TICKS_PER_POINT * travelSpeed;
        int index = Mth.floor(x);
        double fractionalPart = Mth.frac(x);

        double p0 = getPointCoordinate(index, coordinateIndex);
        double p1 = getPointCoordinate(index + 1, coordinateIndex);

        return p0 + (p1 - p0) * fractionalPart;
    }

    private static double getPathSmooth(double time, double travelSpeed, int coordinateIndex) {
        double x = time / TICKS_PER_POINT * travelSpeed;
        int index = Mth.floor(x);
        double fractionalPart = Mth.frac(x);

        double p0 = getPointCoordinate(index, coordinateIndex);
        double p1 = getPointCoordinate(index + 1, coordinateIndex);
        double p2 = getPointCoordinate(index + 2, coordinateIndex);
        double p3 = getPointCoordinate(index + 3, coordinateIndex);

        return catmullRomInterpolate(p0, p1, p2, p3, fractionalPart);
    }

    private static double getPointCoordinate(int index, int coordinate) {
        int wraps = index / points;
        index -= wraps * points;
        int value = path[index * 2 + coordinate];
        if (coordinate == 0) value += pathWrap * wraps;
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

    public static double getPathX(double time, double travelSpeed) {
        return getPathSmooth(time, travelSpeed, 0);
    }

    public static double getPathZ(double time, double travelSpeed) {
        return getPathSmooth(time, travelSpeed, 1);
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
        pathWrap = 2 * RandomPath.path[RandomPath.path.length - 2] - RandomPath.path[RandomPath.path.length - 4];
    }

}
