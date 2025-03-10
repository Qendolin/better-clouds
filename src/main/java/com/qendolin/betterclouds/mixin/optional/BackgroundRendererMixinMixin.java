package com.qendolin.betterclouds.mixin.optional;

import com.bawnorton.mixinsquared.TargetHandler;
import com.qendolin.betterclouds.compat.SodiumExtraCompat;
import net.minecraft.client.render.BackgroundRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BackgroundRenderer.class, priority = 1500)
public class BackgroundRendererMixinMixin {

    // For 1.21 (and .1 unofficially) sodium extra 0.5 and 0.6 exist, thats why both are needed

    //? if <=1.21.1 {
    /*@TargetHandler(
        mixin = "me.flashyreese.mods.sodiumextra.mixin.fog.MixinBackgroundRenderer",
        name = "applyFog"
    )
    @Inject(
        method = "@MixinSquared:Handler",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private static void preventFogModification5(CallbackInfo ci) {
        preventFogModificationCommon(ci);
    }
    *///?}

    //? if >=1.21 {
    @TargetHandler(
        mixin = "me.flashyreese.mods.sodiumextra.mixin.fog.MixinFogRenderer",
        name = "applyFog"
    )
    @Inject(
        method = "@MixinSquared:Handler",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private static void preventFogModification6(CallbackInfo ci) {
        preventFogModificationCommon(ci);
    }
    //?}

    @Unique
    private static void preventFogModificationCommon(CallbackInfo ci) {
        if(SodiumExtraCompat.PREVENT_FOG_MODIFICATION.get()) {
            ci.cancel();
        }
    }
}
