package com.qendolin.betterclouds.mixin.duck;

public interface ListOptionDuck {
    void betterclouds$setCollapsed(boolean collapsed);

    void betterclouds$setForceExpanded(boolean forceExpanded);

    boolean betterclouds$forceExpanded();
}
