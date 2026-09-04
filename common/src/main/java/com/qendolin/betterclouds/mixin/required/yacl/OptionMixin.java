package com.qendolin.betterclouds.mixin.required.yacl;

import com.qendolin.betterclouds.mixin.duck.OptionDuck;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.impl.OptionImpl;
import org.spongepowered.asm.mixin.*;

@Mixin(OptionImpl.class)
public class OptionMixin implements OptionDuck {
    @Shadow
    private OptionDescription description;

    @Unique
    private OptionDescription betterclouds$originalDescription;

    @Override
    public void betterclouds$setDescription(OptionDescription description) {
        this.betterclouds$originalDescription = description;
        this.description = description;
    }

    @Override
    public void betterclouds$appendToDescription(OptionDescription description) {
        if (this.betterclouds$originalDescription == null)
            this.betterclouds$originalDescription = this.description;
        this.description = OptionDescription.of(this.betterclouds$originalDescription.text(), description.text());
    }
}
