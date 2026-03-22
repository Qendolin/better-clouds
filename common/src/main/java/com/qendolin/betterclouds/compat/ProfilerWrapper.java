package com.qendolin.betterclouds.compat;

import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;

public class ProfilerWrapper {

    public static ProfilerFiller getProfiler() {
        return Profiler.get();
    }
}
