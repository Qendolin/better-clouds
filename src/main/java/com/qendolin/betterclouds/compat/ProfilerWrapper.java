package com.qendolin.betterclouds.compat;

import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;

public class ProfilerWrapper {

    public static Profiler getProfiler() {
        //? if >=1.21.3 {
        return Profilers.get();
        //?} else {
        //return MinecraftClient.getInstance().getProfiler();
        //?}
    }
}
