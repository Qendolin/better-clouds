package com.qendolin.betterclouds.config;

import com.google.common.collect.ImmutableList;
import com.qendolin.betterclouds.Main;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

// This is some real shit, don't use!
public class ConfigScreen<C extends ModConfig> extends Screen {
    private final Screen parent;
    private C config;
    private final String translationKeyPrefix;
    protected final Map<String, ConfigEntry> entries = new HashMap<>();
    private CloseAction<C> onClose;
    private SettingChangeCallback<C> onChange;
    protected ButtonWidget doneButton;

    protected static Map<Class<? extends Annotation>, WidgetFactory<? extends Annotation>> annotationWidgetFactories;

    static {
        annotationWidgetFactories = new HashMap<>();
        Entry.init();
    }

    private SettingList list;

    public interface WidgetFactory<A extends Annotation> {
        ClickableWidget create(A a, int x, int y, int w, int h, Field f, Object o, Function<?, String> s, ValueChangeCallback<?> c) throws IllegalAccessException;
    }

    public ConfigScreen(Screen parent, C config) {
        super(new TranslatableText(config.getId()+".config.title"));
        this.parent = parent;
        this.config = config;
        this.onClose = ConfigScreen::onCloseDefault;
        translationKeyPrefix = config.getId() + ".config.entry.";
    }

    public ConfigScreen<C> onChange(SettingChangeCallback<C> callback) {
        onChange = callback;
        return this;
    }

    public ConfigScreen<C> onClose(CloseAction<C> action) {
        onClose = action;
        return this;
    }

    // Thanks MidnightConfig
    class SettingList extends ElementListWidget<Setting> {
        public SettingList() {
            super(ConfigScreen.this.client, ConfigScreen.this.width, ConfigScreen.this.height, 36, ConfigScreen.this.height - 36, 25);
            centerListVertically = false;
            if(ConfigScreen.this.client != null && ConfigScreen.this.client.world != null) {
                setRenderBackground(false);
            }
        }
        @Override
        public int getRowWidth() { return 10000; }
        @Override
        public int getScrollbarPositionX() { return this.width - 7; }
        public void addSetting(Setting entry) {
            addEntry(entry);
        }
    }

    static class Setting extends ElementListWidget.Entry<Setting> {
        private final ClickableWidget widget;
        private final Text name;
        private final MinecraftClient client = MinecraftClient.getInstance();
        public Setting(ClickableWidget widget, Text name) {
            this.widget = widget;
            this.name = name;
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return ImmutableList.of(widget);
        }

        @Override
        public List<? extends Element> children() {
            return ImmutableList.of(widget);
        }

        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int centerY = y + widget.getHeight() / 2 - client.textRenderer.fontHeight / 2;
            DrawableHelper.drawTextWithShadow(matrices, client.textRenderer, name, 20, centerY, 0xffffff);

            widget.y = y;
            widget.render(matrices, mouseX, mouseY, tickDelta);
        }
    }

    public static <C extends ModConfig> void onCloseDefault(boolean save, C config, List<EntryValueSetter<?>> valueSetters) {
        if (!save) {
            for (EntryValueSetter<?> setter : valueSetters) {
                setter.discard(config);
            }
            return;
        }
        for (EntryValueSetter<?> setter : valueSetters) {
            setter.apply(config);
        }
        ConfigManager.save(config);
    }

    public static <T extends Annotation> void registerWidgetFactory(Class<T> annotation, WidgetFactory<T> factory) {
        ConfigScreen.annotationWidgetFactories.put(annotation, factory);
    }

    private <T> Function<T, String> getStringer(String name) {
        if(name == null || name.isEmpty()) return null;
        Method stringer = null;
        for (Method method : config.getClass().getDeclaredMethods()) {
            if(method.getName().equals(name)) {
                stringer = method;
                break;
            }
        }
        if(stringer == null) {
            ConfigManager.LOGGER.error(new NoSuchMethodException(config.getClass().getName() + "." + name + "(String)"));
            return null;
        }
        final Method finalStringer = stringer;
        finalStringer.setAccessible(true);
        return o -> {
            try {
                return (String) finalStringer.invoke(config, o);
            } catch (IllegalAccessException | InvocationTargetException e) {
                ConfigManager.LOGGER.error(e);
                return Objects.toString(o);
            }
        };
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.list.render(matrices, mouseX, mouseY, delta);
        drawCenteredText(matrices, textRenderer, title, width/2, 16, 0xFFFFFF);


        for (ConfigEntry entry : entries.values()) {
            if(entry.tooltip != null && mouseX >= 20 && mouseX <= entry.widget.x - 2 && mouseY > entry.widget.y && mouseY < entry.widget.y + entry.height) {
                renderTooltip(matrices, entry.tooltip, mouseX, mouseY);
            }
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    protected void init() {
        super.init();
        this.list = new SettingList();
        addSelectableChild(list);

        List<EntryValueSetter<?>> valueSetters = new ArrayList<>();

        doneButton = addDrawableChild(new ButtonWidget(this.width/2 + 4,this.height - 20 - 8,150,20, new TranslatableText("gui.done"), (button) -> {
            client.setScreen(parent);
            onClose.invoke(true, this.config, valueSetters);
        }));
        addDrawableChild(new ButtonWidget(this.width/2 - 150 - 4,this.height - 20 - 8,150,20, new TranslatableText("gui.cancel"), (button) -> {
            client.setScreen(parent);
            onClose.invoke(false, this.config, valueSetters);
        }));
        addDrawableChild(new ButtonWidget(this.width - 50 - 6, 8, 50, 20, new TranslatableText("controls.reset"), (button) -> {
            try {
                C defaultConfig = (C) config.getClass().getConstructor().newInstance();
                for (ConfigEntry entry : entries.values()) {
                    ((ValueHolder) entry.widget).setValue(entry.field.get(defaultConfig));
                }
            } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                Main.LOGGER.fatal(e);
            }
        }));

        int sx = width-200-20;
        int sy = 40;
        try {
            for (Field field : config.getClass().getFields()) {
                for (Annotation annotation : field.getAnnotations()) {
                    WidgetFactory provider = annotationWidgetFactories.getOrDefault(annotation.annotationType(), null);
                    if(provider == null) continue;
                    EntryValueSetter<?>[] setter = new EntryValueSetter[1];
                    String stringerName = (String) annotation.annotationType().getMethod("stringer").invoke(annotation);
                    ClickableWidget widget = provider.create(annotation, sx, sy, 200, 20, field, config, getStringer(stringerName), (prev, curr) -> {
                        if(onChange != null && setter[0] != null) onChange.invoke(config, field.getName(), setter[0], prev, curr);
                    });

                    String tooltipKey = translationKeyPrefix + field.getName() + ".tooltip";
                    TranslatableText tooltip = null;
                    if(I18n.hasTranslation(tooltipKey)) tooltip = new TranslatableText(tooltipKey);
                    Object startValue = ((ValueHolder<?>) widget).getValue();
                    entries.put(field.getName(), new ConfigEntry(field.getName(), field, widget.y, widget.x, widget.getHeight(), widget.getWidth(), widget, tooltip, startValue));
                    setter[0] = new EntryValueSetter<>(field, ((ValueHolder<?>) widget)::getValue, startValue);
                    valueSetters.add(setter[0]);
                    list.addSetting(new Setting(widget, new TranslatableText(translationKeyPrefix+field.getName())));
                    sy += widget.getHeight() + 10;
                }
            }
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            ConfigManager.LOGGER.fatal(e);
        }
    }

    protected static record ConfigEntry(String name, Field field, int y, int x, int height, int width, ClickableWidget widget, TranslatableText tooltip, Object startValue) {}
    public static record EntryValueSetter<V>(Field field, Supplier<V> valueSupplier, V startValue){
        public <T extends ModConfig> void apply(T config) {
            try {
                field.set(config, valueSupplier.get());
            } catch(IllegalArgumentException | IllegalAccessException e) {
                ConfigManager.LOGGER.error(e);
            }
        }
        public <T extends ModConfig> void discard(T config) {
            try {
                field.set(config, startValue);
            } catch(IllegalArgumentException | IllegalAccessException e) {
                ConfigManager.LOGGER.error(e);
            }
        }
    }

    public interface CloseAction<C extends ModConfig> {
        void invoke(boolean save, C config, List<EntryValueSetter<?>> valueSetters);
    }

    public interface ValueChangeCallback<T> {
        void invoke(T prev, T curr);
    }

    public interface SettingChangeCallback<C extends ModConfig> {
        void invoke(C config, String name, EntryValueSetter<?> setter, Object prev, Object curr);
    }
}