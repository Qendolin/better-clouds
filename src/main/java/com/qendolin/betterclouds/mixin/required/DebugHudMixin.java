package com.qendolin.betterclouds.mixin.required;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.qendolin.betterclouds.clouds.Debug;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import java.util.List;

@Mixin(DebugHud.class)
public class DebugHudMixin {

    @ModifyExpressionValue(method = "drawLeftText", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/DebugHud;getLeftText()Ljava/util/List;"))
    private List<String> drawMyText(List<String> list) {
        list.add(String.format("Clouds: %d/%d (%.2f)", Debug.currentRenderedClouds, Debug.currentTotalClouds, (float) Debug.totalClouds / Debug.totalPoints));
        return list;
    }

}
