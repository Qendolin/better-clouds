package com.qendolin.betterclouds;

import com.bawnorton.mixinsquared.MixinSquaredBootstrap;
import com.qendolin.betterclouds.compat.IrisCompat;
import com.qendolin.betterclouds.compat.SodiumExtraCompat;
import com.qendolin.betterclouds.util.DisableMixin;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.util.Annotations;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class MixinPlugin implements IMixinConfigPlugin {

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith("DimensionEffectsOverworldMixin")) {
            return SodiumExtraCompat.IS_LOADED;
        }
        if (mixinClassName.endsWith("BackgroundRendererMixinMixin")) {
            return SodiumExtraCompat.IS_LOADED;
        }
        if (mixinClassName.endsWith("ExtendedShaderAccessor") || mixinClassName.endsWith("FallbackShaderAccessor")) {
            return IrisCompat.isLoaded();
        }
        return !isDisabledByAnnotation(mixinClassName);
    }

    private static final String DISABLE_ANNOTATION_DESC = Type.getDescriptor(DisableMixin.class);

    private boolean isDisabledByAnnotation(String mixinClassName) {
        try {
            ClassNode classNode = MixinService.getService()
                .getBytecodeProvider()
                .getClassNode(mixinClassName);
            var annotation = classNode.invisibleAnnotations.stream()
                .filter(n -> DISABLE_ANNOTATION_DESC.equals(n.desc))
                .findFirst();
            if(annotation.isEmpty())
                return false;
            return Annotations.<Boolean>getValue(annotation.get(), "value", DisableMixin.class);
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
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
