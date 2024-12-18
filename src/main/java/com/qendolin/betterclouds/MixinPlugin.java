package com.qendolin.betterclouds;

import com.bawnorton.mixinsquared.MixinSquaredBootstrap;
import com.qendolin.betterclouds.compat.IrisCompat;
import com.qendolin.betterclouds.compat.SodiumExtraCompat;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MixinPlugin implements IMixinConfigPlugin {

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if(mixinClassName.endsWith("DimensionEffectsOverworldMixin")) {
            return SodiumExtraCompat.IS_LOADED;
        }
        if(mixinClassName.endsWith("BackgroundRendererMixinMixin")) {
            return SodiumExtraCompat.IS_LOADED;
        }
        if(mixinClassName.endsWith("ExtendedShaderAccessor") || mixinClassName.endsWith("FallbackShaderAccessor")) {
            return IrisCompat.IS_LOADED;
        }
        return true;
    }

    @Override
    public void onLoad(String mixinPackage) {
        MixinSquaredBootstrap.init();
    }

    @Override
    public String getRefMapperConfig() {
        return "";
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return List.of();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
