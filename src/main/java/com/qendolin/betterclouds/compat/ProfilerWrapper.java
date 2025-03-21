package com.qendolin.betterclouds.compat;

import net.minecraft.util.profiler.Profiler;

//? if >=1.21.3 {
import net.minecraft.util.profiler.Profilers;
//?} else {
/*import net.minecraft.client.MinecraftClient;
*///?}

public class ProfilerWrapper {

    public static Profiler getProfiler() {
        //? if >=1.21.3 {
        return Profilers.get();
        //?} else {
        /*return MinecraftClient.getInstance().getProfiler();
        *///?}
    }
}
