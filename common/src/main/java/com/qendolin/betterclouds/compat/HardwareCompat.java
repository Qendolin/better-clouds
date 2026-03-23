package com.qendolin.betterclouds.compat;

import java.util.stream.Stream;

public class HardwareCompat {
    public static boolean isMaybeIncompatible() {
        String cpu = GLCompat.getCpuInfo();
        if (cpu == null) cpu = "";
        String cpuNormalized = cpu.toLowerCase();
        String renderer = GLCompat.getRenderer();
        if (renderer == null) renderer = "";
        String rendererNormalized = renderer.toLowerCase();

        boolean isAmdRadeonRenderer = rendererNormalized.contains("amd") && rendererNormalized.contains("radeon");
        boolean isAmdRyzen3 = cpuNormalized.contains("amd ryzen 3");
        boolean isAmdRyzen32xx = Stream.of("3200g", "3200u", "3250u").anyMatch(cpuNormalized::contains);

        return isAmdRadeonRenderer && isAmdRyzen3 && isAmdRyzen32xx;
    }
}
