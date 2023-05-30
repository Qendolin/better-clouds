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
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ConfigGUI {
    protected final Config config;
    protected final Config defaults;

    public final Option<Integer> chunkSize;
    public final Option<Float> distance;
    public final Option<Float> fuzziness;
    public final Option<Float> spacing;
    public final Option<Float> sparsity;
    public final Option<Boolean> shuffle;
    public final Option<Float> saturation;
    public final Option<Color> tint;
    public final Option<Float> gamma;
    public final Option<Float> dayBrightness;
    public final Option<Float> nightBrightness;
    public final Option<Float> randomPlacement;
    public final Option<Float> yRange;
    public final Option<Float> yOffset;
    public final Option<Float> samplingScale;
    public final Option<Float> sizeXZ;
    public final Option<Float> sizeY;
    public final Option<Float> scaleFalloffMin;
    public final Option<Float> travelSpeed;
    public final Option<Float> windFactor;
    public final Option<Float> colorVariationFactor;
    public final Option<Boolean> enabled;
    public final Option<Float> opacity;
    public final Option<Float> opacityFactor;
    public final Option<Float> fadeEdge;
    public final LabelOption irisDisclaimer;
    public final Option<Boolean> irisSupport;
    public final Option<Boolean> cloudOverride;
    public final Option<Float> sunPathAngle;
    public final Option<Boolean> useIrisFBO;
    public final Option<Boolean> writeDepth;
    public final Option<Float> upscaleResolutionFactor;
    public final Option<Boolean> usePersistentBuffers;

    protected final List<Pair<ConfigCategory.Builder, List<Pair<OptionGroup.Builder, List<Option<?>>>>>> categories = new ArrayList<>();

    protected final List<Pair<OptionGroup.Builder, List<Option<?>>>> generationCategory = new ArrayList<>();
    protected final List<Pair<OptionGroup.Builder, List<Option<?>>>> appearanceCategory = new ArrayList<>();
    protected final List<Pair<OptionGroup.Builder, List<Option<?>>>> performanceCategory = new ArrayList<>();
    protected final List<Pair<OptionGroup.Builder, List<Option<?>>>> shadersCategory = new ArrayList<>();

    protected final List<Option<?>> generationVisualGroup = new ArrayList<>();
    protected final List<Option<?>> generationPerformanceGroup = new ArrayList<>();
    protected final List<Option<?>> appearanceGeometryGroup = new ArrayList<>();
    protected final List<Option<?>> appearanceVisibilityGroup = new ArrayList<>();
    protected final List<Option<?>> appearanceColorGroup = new ArrayList<>();
    protected final List<Option<?>> performanceGenerationGroup = new ArrayList<>();
    protected final List<Option<?>> performanceTechnicalGroup = new ArrayList<>();
    protected final List<Option<?>> shadersGeneralGroup = new ArrayList<>();
    protected final List<Option<?>> shadersColorGroup = new ArrayList<>();
    protected final List<Option<?>> shadersTechnicalGroup = new ArrayList<>();

    public ConfigGUI(Config defaults, Config config) {
        this.defaults = defaults;
        this.config = config;

        this.chunkSize = createOption(int.class, "chunkSize")
            .binding(defaults.chunkSize, () -> config.chunkSize, val -> config.chunkSize = val)
            .controller(opt -> new IntegerSliderController(opt, 16, 128, 8))
            .build();
        this.distance = createOption(float.class, "distance")
            .binding(defaults.distance, () -> config.distance, val -> config.distance = val)
            .controller(opt -> new FloatSliderController(opt, 1, 4, 0.05f, ConfigGUI::formatAsTimes))
            .build();
        this.fuzziness = createOption(float.class, "fuzziness")
            .binding(defaults.fuzziness, () -> config.fuzziness, val -> config.fuzziness = val)
            .controller(opt -> new FloatSliderController(opt, 0, 1, 0.01f, ConfigGUI::formatAsPercent))
            .build();
        this.spacing = createOption(float.class, "spacing")
            .binding(defaults.spacing, () -> config.spacing, val -> config.spacing = val)
            .controller(opt -> new FloatSliderController(opt, 2, 64, 0.25f))
            .build();
        this.sparsity = createOption(float.class, "sparsity")
            .binding(defaults.sparsity, () -> config.sparsity, val -> config.sparsity = val)
            .controller(opt -> new FloatSliderController(opt, 0, 1, 0.01f, ConfigGUI::formatAsPercent))
            .build();
        this.shuffle = createOption(boolean.class, "shuffle")
            .binding(defaults.shuffle, () -> config.shuffle, val -> config.shuffle = val)
            .controller(TickBoxController::new)
            .build();
        this.saturation = createOption(float.class, "saturation")
            .binding(defaults.saturation, () -> config.saturation, val -> config.saturation = val)
            .controller(opt -> new FloatSliderController(opt, 0, 2, 0.05f, ConfigGUI::formatAsPercent))
            .build();
        this.tint = createOption(Color.class, "tint")
            .binding(new Color(defaults.tintRed, defaults.tintGreen, defaults.tintBlue), () -> new Color(config.tintRed, config.tintGreen, config.tintBlue), val -> {
                config.tintRed = val.getRed() / 255f;
                config.tintGreen = val.getGreen() / 255f;
                config.tintBlue = val.getBlue() / 255f;
            })
            .controller(ColorController::new)
            .build();
        this.gamma = createOption(float.class, "gamma")
            .binding(defaults.gamma, () -> config.gamma, val -> config.gamma = val)
            .controller(opt -> new FloatSliderController(opt, -5, 5, 0.01f))
            .build();
        this.dayBrightness = createOption(float.class, "dayBrightness")
            .binding(defaults.dayBrightness, () -> config.dayBrightness, val -> config.dayBrightness = val)
            .controller(opt -> new FloatSliderController(opt, 0.1f, 4, 0.01f, ConfigGUI::formatAsPercent))
            .build();
        this.nightBrightness = createOption(float.class, "nightBrightness")
            .binding(defaults.nightBrightness, () -> config.nightBrightness, val -> config.nightBrightness = val)
            .controller(opt -> new FloatSliderController(opt, 0.1f, 4, 0.01f, ConfigGUI::formatAsPercent))
            .build();
        this.randomPlacement = createOption(float.class, "randomPlacement")
            .binding(defaults.randomPlacement, () -> config.randomPlacement, val -> config.randomPlacement = val)
            .controller(opt -> new FloatSliderController(opt, 0, 1, 0.01f, ConfigGUI::formatAsPercent))
            .build();
        this.yRange = createOption(float.class, "yRange")
            .binding(defaults.yRange, () -> config.yRange, val -> config.yRange = val)
            .controller(opt -> new FloatSliderController(opt, 0, 128, 0.5f))
            .build();
        this.yOffset = createOption(float.class, "yOffset")
            .binding(defaults.yOffset, () -> config.yOffset, val -> config.yOffset = val)
            .controller(opt -> new FloatSliderController(opt, -64, 256, 8))
            .build();
        this.samplingScale = createOption(float.class, "samplingScale")
            .binding(defaults.samplingScale, () -> config.samplingScale, val -> config.samplingScale = val)
            .controller(opt -> new FloatSliderController(opt, 0.25f, 4, 0.01f, ConfigGUI::formatAsTimes))
            .build();
        this.sizeXZ = createOption(float.class, "sizeXZ")
            .binding(defaults.sizeXZ, () -> config.sizeXZ, val -> config.sizeXZ = val)
            .controller(opt -> new FloatSliderController(opt, 2, 64, 1))
            .build();
        this.sizeY = createOption(float.class, "sizeY")
            .binding(defaults.sizeY, () -> config.sizeY, val -> config.sizeY = val)
            .controller(opt -> new FloatSliderController(opt, 1, 32, 1))
            .build();
        this.scaleFalloffMin = createOption(float.class, "scaleFalloffMin")
            .binding(defaults.scaleFalloffMin, () -> config.scaleFalloffMin, val -> config.scaleFalloffMin = val)
            .controller(opt -> new FloatSliderController(opt, 0, 1, 0.05f, ConfigGUI::formatAsPercent))
            .build();
        this.travelSpeed = createOption(float.class, "travelSpeed")
            .binding(defaults.travelSpeed, () -> config.travelSpeed, val -> config.travelSpeed = val)
            .controller(opt -> new FloatSliderController(opt, 0, 0.1f, 0.005f, ConfigGUI::formatAsBlocksPerSecond))
            .build();
        this.windFactor = createOption(float.class, "windFactor")
            .binding(defaults.windFactor, () -> config.windFactor, val -> config.windFactor = val)
            .controller(opt -> new FloatSliderController(opt, 0, 1, 0.05f, ConfigGUI::formatAsPercent))
            .build();
        this.colorVariationFactor = createOption(float.class, "colorVariationFactor")
            .binding(defaults.colorVariationFactor, () -> config.colorVariationFactor, val -> config.colorVariationFactor = val)
            .controller(opt -> new FloatSliderController(opt, 0, 1, 0.05f, ConfigGUI::formatAsPercent))
            .build();
        this.enabled = createOption(boolean.class, "enabled")
            .binding(defaults.enabled, () -> config.enabled, val -> config.enabled = val)
            .controller(opt -> new BooleanController(opt, val -> Text.translatable(LANG_KEY_PREFIX + ".entry.enabled." + val), false))
            .build();
        this.opacity = createOption(float.class, "opacity")
            .binding(defaults.opacity, () -> config.opacity, val -> config.opacity = val)
            .controller(opt -> new FloatSliderController(opt, 0, 1, 0.01f, ConfigGUI::formatAsPercent))
            .build();
        this.opacityFactor = createOption(float.class, "opacityFactor")
            .binding(defaults.opacityFactor, () -> config.opacityFactor, val -> config.opacityFactor = val)
            .controller(opt -> new FloatSliderController(opt, 0, 1, 0.01f, ConfigGUI::formatAsPercent))
            .build();
        this.fadeEdge = createOption(float.class, "fadeEdge")
            .binding(defaults.fadeEdge, () -> config.fadeEdge, val -> config.fadeEdge = val)
            .controller(opt -> new FloatSliderController(opt, 0.1f, 0.5f, 0.01f, ConfigGUI::formatAsPercent))
            .build();
        this.irisDisclaimer = LabelOption.create(Text.translatable(LANG_KEY_PREFIX+".text.shaders"));
        this.irisSupport = createOption(boolean.class, "irisSupport")
            .binding(defaults.irisSupport, () -> config.irisSupport, val -> config.irisSupport = val)
            .controller(TickBoxController::new)
            .build();
        this.cloudOverride = createOption(boolean.class, "cloudOverride")
            .binding(defaults.cloudOverride, () -> config.cloudOverride, val -> config.cloudOverride = val)
            .controller(TickBoxController::new)
            .build();
        this.sunPathAngle = createOption(float.class, "sunPathAngle")
            .binding(defaults.sunPathAngle, () -> config.sunPathAngle, val -> config.sunPathAngle = val)
            .controller(opt -> new FloatSliderController(opt, -60f, 60f, 1f, ConfigGUI::formatAsDegrees))
            .build();
        this.useIrisFBO = createOption(boolean.class, "useIrisFBO")
            .binding(defaults.useIrisFBO, () -> config.useIrisFBO, val -> config.useIrisFBO = val)
            .controller(TickBoxController::new)
            .build();
        this.writeDepth = createOption(boolean.class, "writeDepth")
            .binding(defaults.writeDepth, () -> config.writeDepth, val -> config.writeDepth = val)
            .controller(TickBoxController::new)
            .build();
        this.upscaleResolutionFactor = createOption(float.class, "upscaleResolutionFactor")
            .binding(defaults.upscaleResolutionFactor, () -> config.upscaleResolutionFactor, val -> config.upscaleResolutionFactor = val)
            .controller(opt -> new FloatSliderController(opt, 0.25f, 1.0f, 0.25f, ConfigGUI::formatAsPercent))
            .build();
        this.usePersistentBuffers = createOption(boolean.class, "usePersistentBuffers")
            .binding(defaults.usePersistentBuffers, () -> config.usePersistentBuffers, val -> config.usePersistentBuffers = val)
            .controller(TickBoxController::new)
            .build();

        categories.add(new Pair<>(ConfigCategory.createBuilder()
            .name(categoryLabel("generation")), generationCategory));
        generationCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("generation.visual")), generationVisualGroup));
        generationVisualGroup.addAll(List.of(
            randomPlacement, fuzziness, sparsity, yRange, yOffset, spacing, samplingScale, shuffle
        ));
        generationCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("generation.performance")), generationPerformanceGroup));
        generationPerformanceGroup.addAll(List.of(distance, chunkSize));

        categories.add(new Pair<>(ConfigCategory.createBuilder()
            .name(categoryLabel("appearance")), appearanceCategory));
        appearanceCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("appearance.geometry")), appearanceGeometryGroup));
        appearanceGeometryGroup.addAll(List.of(sizeXZ, sizeY, scaleFalloffMin, travelSpeed, windFactor));
        appearanceCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("appearance.visibility")), appearanceVisibilityGroup));
        appearanceVisibilityGroup.addAll(List.of(enabled, opacity, opacityFactor, fadeEdge));
        appearanceCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("appearance.color")), appearanceColorGroup));
        appearanceColorGroup.addAll(List.of(colorVariationFactor, gamma, dayBrightness, nightBrightness, saturation, tint));

        categories.add(new Pair<>(ConfigCategory.createBuilder()
            .name(categoryLabel("performance")), performanceCategory));
        performanceCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("performance.generation")), performanceGenerationGroup));
        performanceGenerationGroup.addAll(List.of(spacing, chunkSize, distance, sparsity, fuzziness, shuffle));
        performanceCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("performance.technical")), performanceTechnicalGroup));
        performanceTechnicalGroup.addAll(List.of(usePersistentBuffers));

        categories.add(new Pair<>(ConfigCategory.createBuilder()
            .name(categoryLabel("shaders")), shadersCategory));
        shadersCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("shaders.general")), shadersGeneralGroup));
        shadersGeneralGroup.addAll(List.of(irisDisclaimer, irisSupport, cloudOverride));
        shadersCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("shaders.color")), shadersColorGroup));
        shadersColorGroup.addAll(List.of(gamma, dayBrightness, nightBrightness, saturation, tint));
        shadersCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("shaders.technical")), shadersTechnicalGroup));
        shadersTechnicalGroup.addAll(List.of(sunPathAngle, useIrisFBO, writeDepth, upscaleResolutionFactor));
    }

    public YetAnotherConfigLib.Builder apply(YetAnotherConfigLib.Builder builder) {
        builder = builder
            .save(() -> Main.getConfigInstance().save())
            .title(Text.translatable(LANG_KEY_PREFIX + ".title"));

        for (Pair<ConfigCategory.Builder, List<Pair<OptionGroup.Builder, List<Option<?>>>>> categoryPair : categories) {
            ConfigCategory.Builder categoryBuilder = categoryPair.getLeft();
            for (Pair<OptionGroup.Builder, List<Option<?>>> groupPair : categoryPair.getRight()) {
                OptionGroup.Builder groupBuilder = groupPair.getLeft();
                groupBuilder.options(groupPair.getRight());
                categoryBuilder.group(groupBuilder.build());
            }
            builder.category(categoryBuilder.build());
        }

        return builder;
    }

    public static ConfigGUI.CustomScreen create(Screen parent) {
        YetAnotherConfigLib yacl = YetAnotherConfigLib.create(Main.getConfigInstance(),
            (defaults, config, builder) -> new ConfigGUI(defaults, config).apply(builder));
        return new CustomScreen(yacl, parent);
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
        protected Screen hiddenScreen;
        protected boolean hidden;

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
                btn -> setHidden(!hidden)
            );
            addDrawableChild(hideShowButton);
            hideShowButton.active = client != null && client.world != null;

            remove(optionList);
            optionList = new CustomOptionListWidget(this, client, width, height);
            addSelectableChild(optionList);

            hiddenScreen = new HiddenScreen(title, hideShowButton);
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

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        public void setHidden(boolean hidden) {
            assert client != null;
            this.hidden = hidden;
            if(hidden) {
                hideShowButton.setMessage(Text.translatable(LANG_KEY_PREFIX+".show"));
                client.setScreen(hiddenScreen);
            } else {
                hideShowButton.setMessage(Text.translatable(LANG_KEY_PREFIX+".hide"));
                client.setScreen(this);
            }
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

        private static class HiddenScreen extends Screen {
            public HiddenScreen(Text title, ButtonWidget showButton) {
                super(title);
                addDrawableChild(showButton);
            }

            @Override
            public boolean shouldCloseOnEsc() {
                return false;
            }

            @Override
            public void renderBackground(MatrixStack matrices) {
                if(client == null || client.world == null) {
                    super.renderBackground(matrices);
                } else {
                    fill(matrices, 0, 0, width / 3, height, 0x6B000000);
                }
            }
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
