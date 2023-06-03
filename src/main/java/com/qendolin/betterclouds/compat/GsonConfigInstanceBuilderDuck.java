package com.qendolin.betterclouds.compat;

import com.google.gson.GsonBuilder;
import dev.isxander.yacl.config.GsonConfigInstance;

import java.util.function.UnaryOperator;

public interface GsonConfigInstanceBuilderDuck<T> {
    GsonConfigInstance.Builder<T> betterclouds$appendGsonBuilder(UnaryOperator<GsonBuilder> operator);
}
