package com.qendolin.betterclouds.mixin.required.yacl;

import com.qendolin.betterclouds.mixin.duck.ListOptionDuck;
import dev.isxander.yacl3.impl.ListOptionImpl;
import org.spongepowered.asm.mixin.*;

@Mixin(ListOptionImpl.class)
public class ListOptionMixin implements ListOptionDuck {
    @Mutable
    @Shadow
    @Final
    private boolean collapsed;
    @Unique
    private boolean betterclouds$forceExpanded;

    @Override
    public void betterclouds$setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
    }

    @Override
    public void betterclouds$setForceExpanded(boolean forceExpanded) {
        this.betterclouds$forceExpanded = forceExpanded;
    }

    @Override
    public boolean betterclouds$forceExpanded() {
        return betterclouds$forceExpanded;
    }
}
