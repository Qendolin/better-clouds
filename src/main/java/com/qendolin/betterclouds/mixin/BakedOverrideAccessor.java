package com.qendolin.betterclouds.mixin;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ModelOverrideList.BakedOverride.class)
public interface BakedOverrideAccessor {
    @Accessor
    BakedModel getModel();

    @Invoker
    boolean callTest(float[] values);
}
