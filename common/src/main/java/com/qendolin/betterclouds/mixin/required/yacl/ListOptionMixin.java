package com.qendolin.betterclouds.mixin.required.yacl;

import com.qendolin.betterclouds.duck.ListOptionDuck;
import dev.isxander.yacl3.impl.ListOptionImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ListOptionImpl.class)
public class ListOptionMixin implements ListOptionDuck {
    @Mutable
    @Shadow
    @Final
    private boolean collapsed;
    @Unique
    private boolean forceExpanded;

    @Override
    public void better_clouds$setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
    }

    @Override
    public void better_clouds$setForceExpanded(boolean forceExpanded) {
        this.forceExpanded = forceExpanded;
    }

    @Override
    public boolean better_clouds$forceExpanded() {
        return forceExpanded;
    }
}
