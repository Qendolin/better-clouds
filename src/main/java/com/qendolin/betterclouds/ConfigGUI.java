package com.qendolin.betterclouds;

import dev.isxander.yacl.api.*;
import dev.isxander.yacl.gui.OptionListWidget;
import dev.isxander.yacl.gui.TooltipButtonWidget;
import dev.isxander.yacl.gui.YACLScreen;
import dev.isxander.yacl.gui.controllers.BooleanController;
import dev.isxander.yacl.gui.controllers.ColorController;
import dev.isxander.yacl.gui.controllers.TickBoxController;
import dev.isxander.yacl.gui.controllers.slider.FloatSliderController;
import dev.isxander.yacl.gui.controllers.slider.IntegerSliderController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ConfigGUI {

    public static CustomScreen build(Screen parent) {
        YetAnotherConfigLib instance = YetAnotherConfigLib.create(Main.getConfigInstance(), (defaults, config, builder) -> {
            Map<String, Option<?>> commonOptions = new HashMap<>();
            commonOptions.put("chunkSize", createOption(int.class, "chunkSize")
                .binding(defaults.chunkSize, () -> config.chunkSize, val -> config.chunkSize = val)
                .controller(opt -> new IntegerSliderController(opt, 16, 128, 8))
                .build());
            commonOptions.put("distance", createOption(float.class, "distance")
                .binding(defaults.distance, () -> config.distance, val -> config.distance = val)
                .controller(opt -> new FloatSliderController(opt, 1, 4, 0.05f, ConfigGUI::formatAsTimes))
                .build());
            commonOptions.put("fuzziness", createOption(float.class, "fuzziness")
                .binding(defaults.fuzziness, () -> config.fuzziness, val -> config.fuzziness = val)
                .controller(opt -> new FloatSliderController(opt, 0, 1, 0.01f, ConfigGUI::formatAsPercent))
                .build());
            commonOptions.put("spacing", createOption(float.class, "spacing")
                .binding(defaults.spacing, () -> config.spacing, val -> config.spacing = val)
                .controller(opt -> new FloatSliderController(opt, 2, 32, 0.25f))
                .build());
            commonOptions.put("sparsity", createOption(float.class, "sparsity")
                .binding(defaults.sparsity, () -> config.sparsity, val -> config.sparsity = val)
                .controller(opt -> new FloatSliderController(opt, 0, 1, 0.01f, ConfigGUI::formatAsPercent))
                .build());
            commonOptions.put("shuffle", createOption(boolean.class, "shuffle")
                .binding(defaults.shuffle, () -> config.shuffle, val -> config.shuffle = val)
                .controller(TickBoxController::new)
                .build());
            commonOptions.put("saturation", createOption(float.class, "saturation")
                .binding(defaults.saturation, () -> config.saturation, val -> config.saturation = val)
                .controller(opt -> new FloatSliderController(opt, 0, 2, 0.05f, ConfigGUI::formatAsPercent))
                .build());
            commonOptions.put("tint", createOption(Color.class, "tint")
                .binding(new Color(defaults.tintRed, defaults.tintGreen, defaults.tintBlue), () -> new Color(config.tintRed, config.tintGreen, config.tintBlue), val -> {
                    config.tintRed = val.getRed() / 255f;
                    config.tintGreen = val.getGreen() / 255f;
                    config.tintBlue = val.getBlue() / 255f;
                })
                .controller(ColorController::new)
                .build());
            commonOptions.put("gamma", createOption(float.class, "gamma")
                .binding(defaults.gamma, () -> config.gamma, val -> config.gamma = val)
                .controller(opt -> new FloatSliderController(opt, -5, 5, 0.01f))
                .build());
            commonOptions.put("dayBrightness", createOption(float.class, "dayBrightness")
                    .binding(defaults.dayBrightness, () -> config.dayBrightness, val -> config.dayBrightness = val)
                    .controller(opt -> new FloatSliderController(opt, 0.1f, 4, 0.01f, ConfigGUI::formatAsPercent))
                    .build());
            commonOptions.put("nightBrightness", createOption(float.class, "nightBrightness")
                    .binding(defaults.nightBrightness, () -> config.nightBrightness, val -> config.nightBrightness = val)
                    .controller(opt -> new FloatSliderController(opt, 0.1f, 4, 0.01f, ConfigGUI::formatAsPercent))
                    .build());


            return builder
                .save(() -> Main.getConfigInstance().save())
                .title(Text.translatable(LANG_KEY_PREFIX + ".title"))
                .category(buildGeneratorCategory(defaults, config, commonOptions))
                .category(buildAppearanceCategory(defaults, config, commonOptions))
                .category(buildPerformanceCategory(defaults, config, commonOptions))
                .category(buildShadersCategory(defaults, config, commonOptions));
        });
        return new CustomScreen(instance, parent);
    }

    public static ConfigCategory buildGeneratorCategory(Config defaults, Config config, Map<String, Option<?>> commonOptions) {
        return ConfigCategory.createBuilder()
            .name(categoryLabel("generation"))
            .group(OptionGroup.createBuilder()
                .name(groupLabel("generation.visual"))
                .option(createOption(float.class, "jitter")
                    .binding(defaults.jitter, () -> config.jitter, val -> config.jitter = val)
                    .controller(opt -> new FloatSliderController(opt, 0, 1, 0.01f, ConfigGUI::formatAsPercent))
                    .build())
                .option(commonOptions.get("fuzziness"))
                .option(commonOptions.get("sparsity"))
                .option(createOption(float.class, "spreadY")
                    .binding(defaults.spreadY, () -> config.spreadY, val -> config.spreadY = val)
                    .controller(opt -> new FloatSliderController(opt, 0, 128, 0.5f))
                    .build())
                .option(commonOptions.get("spacing"))
                .option(createOption(float.class, "samplingScale")
                    .binding(defaults.samplingScale, () -> config.samplingScale, val -> config.samplingScale = val)
                    .controller(opt -> new FloatSliderController(opt, 0.25f, 4, 0.01f, ConfigGUI::formatAsTimes))
                    .build())
                .option(commonOptions.get("shuffle"))
                .build())
            .group(OptionGroup.createBuilder()
                .name(groupLabel("generation.performance"))
                .option(commonOptions.get("distance"))
                .option(commonOptions.get("chunkSize"))
                .build()
            )
            .build();
    }

    public static ConfigCategory buildAppearanceCategory(Config defaults, Config config, Map<String, Option<?>> commonOptions) {
        return ConfigCategory.createBuilder()
            .name(categoryLabel("appearance"))
            .group(OptionGroup.createBuilder()
                .name(groupLabel("appearance.geometry"))
                .option(createOption(float.class, "sizeXZ")
                    .binding(defaults.sizeXZ, () -> config.sizeXZ, val -> config.sizeXZ = val)
                    .controller(opt -> new FloatSliderController(opt, 2, 64, 1))
                    .build())
                .option(createOption(float.class, "sizeY")
                    .binding(defaults.sizeY, () -> config.sizeY, val -> config.sizeY = val)
                    .controller(opt -> new FloatSliderController(opt, 1, 32, 1))
                    .build())
                .option(createOption(float.class, "fakeScaleFalloffMin")
                    .binding(defaults.scaleFalloffMin, () -> config.scaleFalloffMin, val -> config.scaleFalloffMin = val)
                    .controller(opt -> new FloatSliderController(opt, 0, 1, 0.05f))
                    .build())
                .option(createOption(float.class, "windSpeed")
                    .binding(defaults.windSpeed, () -> config.windSpeed, val -> config.windSpeed = val)
                    .controller(opt -> new FloatSliderController(opt, 0, 0.1f, 0.005f, ConfigGUI::formatAsBlocksPerSecond))
                    .build())
                .option(createOption(float.class, "windFactor")
                    .binding(defaults.windFactor, () -> config.windFactor, val -> config.windFactor = val)
                    .controller(opt -> new FloatSliderController(opt, 0, 1, 0.05f))
                    .build())
                .build())
            .group(OptionGroup.createBuilder()
                .name(groupLabel("appearance.visibility"))
                .option(createOption(boolean.class, "enabled")
                    .binding(defaults.enabled, () -> config.enabled, val -> config.enabled = val)
                    .controller(opt -> new BooleanController(opt, val -> Text.translatable(LANG_KEY_PREFIX + ".entry.enabled." + val), false))
                    .build())
                .option(createOption(float.class, "opacity")
                    .binding(defaults.opacity, () -> config.opacity, val -> config.opacity = val)
                    .controller(opt -> new FloatSliderController(opt, 0, 1, 0.01f, ConfigGUI::formatAsPercent))
                    .build())
                .option(createOption(float.class, "fadeEdge")
                    .binding(defaults.fadeEdge, () -> config.fadeEdge, val -> config.fadeEdge = val)
                    .controller(opt -> new FloatSliderController(opt, 0.1f, 0.5f, 0.01f, ConfigGUI::formatAsPercent))
                    .build())
                .option(createOption(float.class, "alphaFactor")
                    .binding(defaults.alphaFactor, () -> config.alphaFactor, val -> config.alphaFactor = val)
                    .controller(opt -> new FloatSliderController(opt, 0, 1, 0.01f, ConfigGUI::formatAsPercent))
                    .build())
                .build())
            .group(OptionGroup.createBuilder()
                .name(groupLabel("appearance.color"))
                .option(commonOptions.get("gamma"))
                .option(commonOptions.get("dayBrightness"))
                .option(commonOptions.get("nightBrightness"))
                .option(commonOptions.get("saturation"))
                .option(commonOptions.get("tint"))
                .build())
            .build();
    }

    public static ConfigCategory buildShadersCategory(Config defaults, Config config, Map<String, Option<?>> commonOptions) {
        return ConfigCategory.createBuilder()
            .name(categoryLabel("shaders"))
            .group(OptionGroup.createBuilder()
                .name(groupLabel("shaders.general"))
                .collapsed(true)
                .option(LabelOption.create(Text.translatable(LANG_KEY_PREFIX+".text.shaders")))
                .option(createOption(boolean.class, "irisSupport")
                    .binding(defaults.irisSupport, () -> config.irisSupport, val -> config.irisSupport = val)
                    .controller(TickBoxController::new)
                    .build())
                .option(createOption(boolean.class, "cloudOverride")
                    .binding(defaults.cloudOverride, () -> config.cloudOverride, val -> config.cloudOverride = val)
                    .controller(TickBoxController::new)
                    .build())
                .build())
            .group(OptionGroup.createBuilder()
                .name(groupLabel("shaders.color"))
                .option(commonOptions.get("gamma"))
                .option(commonOptions.get("dayBrightness"))
                .option(commonOptions.get("nightBrightness"))
                .option(commonOptions.get("saturation"))
                .option(commonOptions.get("tint"))
                .build())
            .group(OptionGroup.createBuilder()
                .name(groupLabel("shaders.technical"))
                .option(createOption(float.class, "sunPathAngle")
                    .binding(defaults.sunPathAngle, () -> config.sunPathAngle, val -> config.sunPathAngle = val)
                    .controller(opt -> new FloatSliderController(opt, -60f, 60f, 1f, ConfigGUI::formatAsDegrees))
                    .build())
                .option(createOption(boolean.class, "useIrisFBO")
                    .binding(defaults.useIrisFBO, () -> config.useIrisFBO, val -> config.useIrisFBO = val)
                    .controller(TickBoxController::new)
                    .build())
                .option(createOption(boolean.class, "writeDepth")
                    .binding(defaults.writeDepth, () -> config.writeDepth, val -> config.writeDepth = val)
                    .controller(TickBoxController::new)
                    .build())
                .build())
            .build();
    }

    public static ConfigCategory buildPerformanceCategory(Config defaults, Config config, Map<String, Option<?>> commonOptions) {
        return ConfigCategory.createBuilder()
            .name(categoryLabel("performance"))
            .group(OptionGroup.createBuilder()
                .name(groupLabel("performance.generation"))
                .option(commonOptions.get("spacing"))
                .option(commonOptions.get("chunkSize"))
                .option(commonOptions.get("distance"))
                .option(commonOptions.get("sparsity"))
                .option(commonOptions.get("fuzziness"))
                .option(commonOptions.get("shuffle"))
                .build())
            .group(OptionGroup.createBuilder()
                .name(groupLabel("performance.technical"))
                .option(createOption(boolean.class, "usePersistentBuffers")
                    .binding(defaults.usePersistentBuffers, () -> config.usePersistentBuffers, val -> config.usePersistentBuffers = val)
                    .controller(TickBoxController::new)
                    .build())
                .build())
            .build();
    }

    private static <T> Option.Builder<T> createOption(Class<T> typeClass, String key) {
        return Option.createBuilder(typeClass)
            .name(optionLabel(key))
            .tooltip(optionTooltip(key))
            .instant(true);
    }

    private static Text formatAsBlocksPerSecond(Float value) {
        return Text.translatable(LANG_KEY_PREFIX+".unit.blocks_per_second", String.format("%.1f", value*20));
    }

    private static Text formatAsPercent(float value) {
        return Text.translatable(LANG_KEY_PREFIX+".unit.percent", ((int) (value*100)));
    }

    private static Text formatAsTimes(float value) {
        return Text.translatable(LANG_KEY_PREFIX+".unit.times", String.format("%.2f", value));
    }

    private static Text formatAsDegrees(Float value) {
        return Text.translatable(LANG_KEY_PREFIX+".unit.degrees", String.format("%.0f", value));
    }

    private static final String LANG_KEY_PREFIX = Main.MODID + ".config";

    private static Text categoryLabel(String key) {
        return Text.translatable(LANG_KEY_PREFIX+".category."+key);
    }

    private static Text categoryTooltip(String key) {
        return Text.translatable(LANG_KEY_PREFIX+".category."+key+".tooltip");
    }

    private static Text groupLabel(String key) {
        return Text.translatable(LANG_KEY_PREFIX+".group."+key);
    }

    private static Text optionLabel(String key) {
        return Text.translatable(LANG_KEY_PREFIX+".entry."+key);
    }

    private static Text optionTooltip(String key) {
        return Text.translatable(LANG_KEY_PREFIX+".entry."+key+".tooltip");
    }

    public static class CustomScreen extends YACLScreen {

        public TooltipButtonWidget hideShowButton;
        private boolean hidden = false;

        public CustomScreen(YetAnotherConfigLib config, Screen parent) {
            super(config, parent);
        }

        @Override
        protected void init() {
            super.init();
            remove(undoButton);

            hideShowButton = new TooltipButtonWidget(
                this,
                undoButton.getX(),
                undoButton.getY(),
                undoButton.getWidth(),
                undoButton.getHeight(),
                Text.translatable(LANG_KEY_PREFIX+".hide"),
                Text.empty(),
                btn -> toggleHidden()
            );
            addDrawableChild(hideShowButton);
            hideShowButton.active = client != null && client.world != null;

            remove(optionList);
            optionList = new CustomOptionListWidget(this, client, width, height);
            addSelectableChild(optionList);
        }

        @Override
        public void tick() {
            super.tick();

            if(Screen.hasShiftDown()) {
                cancelResetButton.active = true;
                cancelResetButton.setTooltip(Text.translatable(LANG_KEY_PREFIX+".reset.tooltip"));
            } else {
                cancelResetButton.active = false;
                cancelResetButton.setTooltip(Text.translatable(LANG_KEY_PREFIX+".reset.tooltip.holdShift"));
            }
        }

        private void toggleHidden() {
            hidden = !hidden;
            hideShowButton.setMessage(Text.translatable(LANG_KEY_PREFIX + (hidden ? ".show" : ".hide")));
        }

        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            if(hidden) {
                hideShowButton.render(matrices, mouseX, mouseY, delta);
                hideShowButton.renderHoveredTooltip(matrices);
                return;
            }

            super.render(matrices, mouseX, mouseY, delta);
        }

        @Override
        public void renderBackground(MatrixStack matrices) {
            if(client == null || client.world == null) {
                super.renderBackground(matrices);
            } else {
                fill(matrices, 0, 0, width / 3, height, 0x6B000000);
            }
        }

        @Override
        protected void finishOrSave() {
            close();
        }

        @Override
        public void close() {
            config.saveFunction().run();
            super.close();
        }
    }

    private static class CustomOptionListWidget extends OptionListWidget {
        public CustomOptionListWidget(YACLScreen screen, MinecraftClient client, int width, int height) {
            super(screen, client, width, height);
        }

        @Override
        protected void renderBackground(MatrixStack matrices) {
            if(client == null || client.world == null) {
                super.renderBackground(matrices);
                setRenderBackground(true);
            } else {
                setRenderBackground(false);
            }
        }
    }
}
