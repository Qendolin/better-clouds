package com.qendolin.betterclouds.compat;

import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;

public class ProfilerWrapper {

    public static Profiler getProfiler() {
        return Profilers.get();
    }
}
