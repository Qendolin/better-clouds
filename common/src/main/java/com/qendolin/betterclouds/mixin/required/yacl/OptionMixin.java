package com.qendolin.betterclouds.mixin.required.yacl;

import com.qendolin.betterclouds.duck.OptionDuck;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.impl.OptionImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(OptionImpl.class)
public class OptionMixin implements OptionDuck {
    @Shadow
    private OptionDescription description;

    @Override
    public void better_clouds$setDescription(OptionDescription description) {
        this.description = description;
    }
}
