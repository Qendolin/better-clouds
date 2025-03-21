package com.qendolin.betterclouds.mixin;

import com.qendolin.betterclouds.util.DisableMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

import net.minecraft.client.render.RenderPhase;
import org.spongepowered.asm.mixin.gen.Accessor;

@DisableMixin(
    /*? if >1.21.4 >>*/ /*true*/
)
@Pseudo
@Mixin(RenderPhase.class)
public interface RenderPhaseAccessor {

    //? if <=1.21.4 {
    @Accessor("CLOUDS_TARGET")
    static RenderPhase.Target getCloudsTarget() {
        return null;
    }
    //?}
}
