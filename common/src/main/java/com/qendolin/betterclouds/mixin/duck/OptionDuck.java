package com.qendolin.betterclouds.mixin.duck;

import dev.isxander.yacl3.api.OptionDescription;

public interface OptionDuck {
    void betterclouds$setDescription(OptionDescription description);

    void betterclouds$appendToDescription(OptionDescription description);
}
