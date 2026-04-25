package com.qendolin.betterclouds.mixin.required.yacl;

import com.qendolin.betterclouds.duck.OptionDuck;
import com.qendolin.betterclouds.duck.StringControllerDuck;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.gui.controllers.string.StringController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = StringController.class, remap = false)
public abstract class StringControllerMixin implements StringControllerDuck {
    @Shadow
    public abstract Option<String> option();

    @Override
    public void better_clouds$setDescription(OptionDescription description) {
        ((OptionDuck) option()).better_clouds$setDescription(description);
    }
}
