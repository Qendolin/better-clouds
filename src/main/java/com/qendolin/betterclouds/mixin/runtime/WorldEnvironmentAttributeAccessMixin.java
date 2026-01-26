package com.qendolin.betterclouds.mixin.runtime;

//? if >=1.21.11 {
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.qendolin.betterclouds.BetterClouds;
import com.qendolin.betterclouds.config.ConfigManager;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.attribute.WorldEnvironmentAttributeAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@SuppressWarnings("UnusedMixin")
@Mixin(WorldEnvironmentAttributeAccess.class)
public class WorldEnvironmentAttributeAccessMixin {

    @ModifyReturnValue(
        method = "getAttributeValue(Lnet/minecraft/world/attribute/EnvironmentAttribute;)Ljava/lang/Object;",
        at = @At("RETURN")
    )
    private Object addCloudsYOffset(Object value, EnvironmentAttribute<?> attribute) {
        if (attribute != EnvironmentAttributes.CLOUD_HEIGHT_VISUAL || !(value instanceof Float)) {
            return value;
        }
        if (!BetterClouds.isEnabled()) {
            return value;
        }
        return (Float) value + ConfigManager.instance().yOffset;
    }
}
//?}
