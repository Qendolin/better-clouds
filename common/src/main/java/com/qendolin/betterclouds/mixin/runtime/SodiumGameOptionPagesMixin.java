package com.qendolin.betterclouds.mixin.runtime;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.qendolin.betterclouds.config.Config;
import com.qendolin.betterclouds.config.ConfigManager;
import net.caffeinemc.mods.sodium.client.gui.SodiumGameOptionPages;
import net.caffeinemc.mods.sodium.client.gui.options.OptionGroup;
import net.caffeinemc.mods.sodium.client.gui.options.OptionImpl;
import net.caffeinemc.mods.sodium.client.gui.options.control.TickBoxControl;
import net.caffeinemc.mods.sodium.client.gui.options.storage.OptionStorage;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@SuppressWarnings("UnusedMixin")
@Mixin(value = SodiumGameOptionPages.class, remap = false)
public class SodiumGameOptionPagesMixin {

    @WrapOperation(
        method = "quality",
        at = @At(
            value = "INVOKE",
            target = "Lnet/caffeinemc/mods/sodium/client/gui/options/OptionGroup$Builder;build()Lnet/caffeinemc/mods/sodium/client/gui/options/OptionGroup;",
            ordinal = 1
        ))
    private static OptionGroup addBetterCloudsToggle(OptionGroup.Builder instance, Operation<OptionGroup> original) {
        var accessor = (SodiumOptionGroupBuilderAccessor) instance;
        var options = accessor.getOptions();
        for (int i = 0; i < options.size(); i++) {
            var option = options.get(i);
            if (option.getValue() instanceof CloudRenderMode) {
                OptionStorage<Config> storage = new OptionStorage<>() {
                    @Override
                    public Config getData() {
                        return ConfigManager.instance();
                    }

                    @Override
                    public void save() {
                        ConfigManager.handler().save();
                    }
                };
                var toggle = OptionImpl.createBuilder(Boolean.TYPE, storage)
                    .setName(Text.translatable("betterclouds.compat.sodium.option.enabled.name"))
                    .setTooltip(Text.translatable("betterclouds.compat.sodium.option.enabled.tooltip"))
                    .setControl(TickBoxControl::new)
                    .setBinding((opts, value) -> opts.enabled = value, (opts) -> opts.enabled).build();
                options.add(i + 1, toggle);
                break;
            }
        }


        return original.call(instance);
    }
}
