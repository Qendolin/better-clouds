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
    private boolean better_clouds$forceExpanded;

    @Override
    public void better_clouds$setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
    }

    @Override
    public void better_clouds$setForceExpanded(boolean forceExpanded) {
        this.better_clouds$forceExpanded = forceExpanded;
    }

    @Override
    public boolean better_clouds$forceExpanded() {
        return better_clouds$forceExpanded;
    }
}
