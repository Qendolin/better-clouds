package com.qendolin.betterclouds.clouds;

import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL33.*;

public class PerfTimer implements AutoCloseable {

    private int query;
    private int queryWaiting;
    private boolean first = true;
    private int frameCount = 0;
    private List<Double> gpu = new ArrayList<>();
    private List<Double> cpu = new ArrayList<>();
    private long startTime;

    public PerfTimer() {
        query = glGenQueries();
        queryWaiting = glGenQueries();
    }

    public void start() {
        glFinish();
        glBeginQuery(GL_TIME_ELAPSED, query);
        startTime = System.nanoTime();
    }

    public void stop() {
        glEndQuery(GL_TIME_ELAPSED);

        int done = queryWaiting;
        queryWaiting = query;
        query = done;

        if (first) {
            first = false;
            return;
        }

        long ns = glGetQueryObjectui64(done, GL_QUERY_RESULT);
        gpu.add(ns / 1e6);

        ns = System.nanoTime() - startTime;
        cpu.add(ns / 1e6);

        frameCount++;

        glFinish();
    }

    public List<Double> gpu() {
        return gpu;
    }

    public List<Double> cpu() {
        return cpu;
    }

    public int frames() {
        return frameCount;
    }

    public void reset() {
        if (!gpu.isEmpty())
            gpu = new ArrayList<>();
        if (!cpu.isEmpty())
            cpu = new ArrayList<>();
        frameCount = 0;
    }

    public void close() {
        glDeleteQueries(new int[] { query, queryWaiting });
    }

    public record Stats(double min, double max, double mean, double sd, double q25, double median, double q75) {
        public static Stats of(List<Double> times) {
            times.sort(Double::compare);
            double median = times.get(times.size() / 2);
            double q25 = times.get((int) Math.ceil(times.size() * 0.25));
            double q75 = times.get((int) Math.ceil(times.size() * 0.75));
            double min = times.get(0);
            double max = times.get(times.size() - 1);
            double mean = times.stream().mapToDouble(d -> d).average().orElse(0);
            double variance = times.stream().mapToDouble(d -> Mth.square(d - mean)).sum() / (times.size() - 1);
            if (times.size() == 1) variance = 0.0;
            double stdDev = Math.sqrt(variance);
            return new Stats(min, max, mean, stdDev, q25, median, q75);
        }

        public String formatted() {
            // idk why but the float format strings aren't working when I put them into the lang file
            return String.format("  %.3f | %.3f §7min, max§r\n  %.3f | %.3f §7mean, sd§r\n  %.3f | %.3f | %.3f §7q25, med, q75§r", min, max, mean, sd, q25, median, q75);
        }

        public String toString() {
            return String.format("min, max:      \t%.3f | %.3f\nmean, sd:      \t%.3f | %.3f\nq25, med, q75: \t%.3f | %.3f | %.3f", min, max, mean, sd, q25, median, q75);
        }
    }
}
