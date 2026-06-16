package com.qendolin.betterclouds.mixin.duck;

import dev.isxander.yacl3.api.OptionDescription;

public interface OptionDuck {
    void better_clouds$setDescription(OptionDescription description);

    void better_clouds$appendToDescription(OptionDescription description);
}
