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
    private OptionDescription better_clouds$originalDescription;

    @Override
    public void better_clouds$setDescription(OptionDescription description) {
        this.better_clouds$originalDescription = description;
        this.description = description;
    }

    @Override
    public void better_clouds$appendToDescription(OptionDescription description) {
        if (this.better_clouds$originalDescription == null)
            this.better_clouds$originalDescription = this.description;
        this.description = OptionDescription.of(this.better_clouds$originalDescription.text(), description.text());
    }
}
