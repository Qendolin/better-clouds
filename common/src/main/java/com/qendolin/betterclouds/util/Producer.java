package com.qendolin.betterclouds.util;

@FunctionalInterface
public interface Producer<V> {
    V produce();
}
