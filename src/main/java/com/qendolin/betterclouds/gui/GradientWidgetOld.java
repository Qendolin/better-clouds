package com.qendolin.betterclouds.gui;

import com.google.common.collect.ImmutableList;
import com.qendolin.betterclouds.Main;
import com.qendolin.betterclouds.gui.color.GammaRgbColor;
import com.qendolin.betterclouds.gui.color.IColor;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class GradientWidgetOld<StopSpace extends IColor<StopSpace, ?>> implements Element, Drawable, Selectable {
    private static final Identifier ICONS_TEXTURE = new Identifier(Main.MODID, "textures/gui/betterclouds/gui_icons.png");
    protected boolean focused = false;
    protected boolean hovered = false;

    public final Bounds bounds;
    public final Bounds gradientBounds;

    public StopSelectedCallback<StopSpace> onStopSelected;
    public Consumer<Float> onStopMoved;

    protected Class<? extends IColor<?, ?>> interpSpace;
    protected final Class<StopSpace> stopSpace;

    protected List<GradientStopElement<StopSpace>> stops = new ArrayList<>();
    protected GradientGenerator<StopSpace, ?> generator;
    protected GradientStopGroup<StopSpace> stopGroup = new GradientStopGroup<>();
    protected Bounds addStopBounds = null;

    private boolean dirty = true;

    public <InterpSpace extends IColor<InterpSpace, ?>> GradientWidgetOld(Bounds bounds, Class<StopSpace> stopSpace, Class<InterpSpace> interpSpace) {
        this.interpSpace = interpSpace;
        this.stopSpace = stopSpace;
        this.bounds = new Bounds(bounds);
        this.gradientBounds = new Bounds(bounds).setHeight(20).inset(5, 0);
        this.stopGroup.onFocusedChanged = value -> {
            if(onStopSelected == null) return;
            if(value == null) {
                onStopSelected.callback(-1, 0, null);
            } else {
                onStopSelected.callback(stops.indexOf(value), value.position(), value.color());
            }
        };
        setInterpSpace(interpSpace);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        hovered = isMouseOver(mouseX, mouseY);

        GradientStopElement<StopSpace> currentHoveredStop = null;
        if(hovered) {
            if(stopGroup.isAnyFocused() && stopGroup.getFocused().bounds.overlaps(mouseX, mouseY)) {
                currentHoveredStop = stopGroup.getFocused();
            } else {
                for (GradientStopElement<StopSpace> stop : stops) {
                    if(stop.bounds.overlaps(mouseX, mouseY)) {
                        currentHoveredStop = stop;
                        if(!stopGroup.isFocused(stop)) {
                            break;
                        }
                    }
                }
            }
        }
        stopGroup.setHovered(currentHoveredStop);

        if(dirty) {
            generator.generate();
            dirty = false;
        }

        for (int x = 0; x < gradientBounds.width(); x++) {
            context.drawVerticalLine(gradientBounds.x()+x, gradientBounds.y(), gradientBounds.limitY(), generator.get(x).pack());
        }
        context.drawBorder(gradientBounds.x()-1, gradientBounds.y(), gradientBounds.width()+2, gradientBounds.height()+1, (hovered || focused) ? Colors.WHITE : 0xFF444444);

        for (GradientStopElement<?> stop : stops) {
            if(stopGroup.isFocused(stop) || stopGroup.isHovered((stop))) continue;
            stop.render(context, mouseX, mouseY, delta);
        }
        if(stopGroup.isAnyFocused()) {
            stopGroup.getFocused().render(context, mouseX, mouseY, delta);
        }
        if(stopGroup.isAnyHovered() && stopGroup.getHovered() != stopGroup.getFocused()) {
            stopGroup.getHovered().render(context, mouseX, mouseY, delta);
        }

        if(bounds.overlaps(mouseX, mouseY) && (!stopGroup.isAnyHovered() || Screen.hasControlDown())) {
            // draw add stop button
            int hoverX = MathHelper.clamp(mouseX - gradientBounds.x(), 0, gradientBounds.width() - 1);
            GammaRgbColor color = generator.get(hoverX);
            context.drawVerticalLine(gradientBounds.x()+hoverX, gradientBounds.limitY() - gradientBounds.height()/3-1, gradientBounds.limitY(), isBright(color) ? Colors.BLACK : Colors.WHITE);
            if(addStopBounds == null) addStopBounds = new Bounds(gradientBounds).setSize(11,16).offset(0, gradientBounds.height()+1);
            addStopBounds.setX(gradientBounds.x() + hoverX - 11/2);
            boolean hovered = addStopBounds.overlaps(mouseX, mouseY);
            context.drawTexture(ICONS_TEXTURE, addStopBounds.x(), addStopBounds.y(), 12, 16, 16, hovered ? 16 : 0, 12, 16, 64, 64);
        } else {
            addStopBounds = null;
        }
    }

    public <InterpSpace extends IColor<InterpSpace, ?>> void setInterpSpace(Class<InterpSpace> space) {
        GradientGenerator<StopSpace, ?> old = generator;
        generator = new GradientGenerator<>(gradientBounds.width(), stopSpace, space, new GammaRgbColor(0,0,0,1));
        if(old != null) {
            for (GradientStop<StopSpace> stop : old.getStops()) {
                generator.addStop(stop);
            }
        }
        dirty = true;
    }

    private static boolean isBright(GammaRgbColor srgb) {
        float luma = (srgb.red*2+srgb.blue+srgb.green*3)/6;
        return luma > 0.5;
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
        if(focused && !stopGroup.isAnyFocused()) {
            selectStop(0);
        }
    }

    @Nullable
    @Override
    public GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
        if (!this.isFocused()) {
            return GuiNavigationPath.of(this);
        }
        return null;
    }

    public ScreenRect getNavigationFocus() {
        return new ScreenRect(bounds.x(), bounds.y(), bounds.width(), bounds.height());
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return bounds.overlaps(mouseX, mouseY);
    }

    @Override
    public SelectionType getType() {
        return focused ? SelectionType.FOCUSED : hovered ? SelectionType.HOVERED : SelectionType.NONE;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {

    }


    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if((modifiers & GLFW.GLFW_MOD_ALT) != 0) {
            int index = selectedIndex();
            if(keyCode == GLFW.GLFW_KEY_LEFT && stopGroup.isAnyFocused()) {
                int prev = index == 0 ? stopCount()-1 : index-1;
                selectStop(prev);
                return true;
            } else if(keyCode == GLFW.GLFW_KEY_RIGHT && stopGroup.isAnyFocused()) {
                int next = (index + 1) % stopCount();
                selectStop(next);
                return true;
            }
        } else {
            float step = 1f/gradientBounds.width();
            if((modifiers & GLFW.GLFW_MOD_SHIFT) != 0) step *= 10;
            if((modifiers & GLFW.GLFW_MOD_CONTROL) != 0) step /= 10;
            if(keyCode == GLFW.GLFW_KEY_LEFT && stopGroup.isAnyFocused()) {
                setSelectedStopPosition(selectedPosition() - step);
                return true;
            } else if(keyCode == GLFW.GLFW_KEY_RIGHT && stopGroup.isAnyFocused()) {
                setSelectedStopPosition(selectedPosition() + step);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
        if (addStopBounds != null && addStopBounds.overlaps(mouseX, mouseY)) {
            float pos = getMouseGradientPosition(mouseX);
            if(stops.isEmpty()) {
                addStop(pos, new GammaRgbColor(0, 0, 0,1).to(stopSpace), true);
            } else {
                addStop(pos, generator.getColor(pos).to(stopSpace), true);
            }
            return true;
        }
        if(stopGroup.isAnyHovered() && stopGroup.getHovered().bounds.overlaps(mouseX, mouseY)) {
            selectStop(stopGroup.getHovered());
            return true;
        }
        if(stopGroup.isAnyFocused() && stopGroup.getFocused().bounds.overlaps(mouseX, mouseY)) {
            return true;
        }
        for (GradientStopElement<StopSpace> stop : stops) {
            if(stop.bounds.overlaps(mouseX, mouseY)) {
                selectStop(stop);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if(button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
        if(!stopGroup.isAnyFocused()) return false;

        float pos = getMouseGradientPosition(mouseX);
        stopGroup.getFocused().setPosition(pos);
        if(onStopMoved != null) onStopMoved.accept(pos);
        dirty = true;
        sort();

        return true;
    }

    private float getMouseGradientPosition(double mouseX) {
        float pos = ((float) mouseX - gradientBounds.x()) / (gradientBounds.width() - 1f);
        pos = MathHelper.clamp(pos, 0, 1);
        return pos;
    }

    @FunctionalInterface
    public interface StopSelectedCallback<Space extends IColor<Space, ?>> {
        void callback(int index, float pos, Space color);
    }

    public static class GradientGenerator<StopSpace extends IColor<StopSpace, ?>, InterpSpace extends IColor<InterpSpace, ?>> {
        protected final Class<InterpSpace> interpSpace;
        protected final Class<StopSpace> stopSpace;
        protected final int resolution;
        protected final GammaRgbColor fallback;
        protected final GammaRgbColor[] cache;
        protected final List<GradientStop<StopSpace>> stops = new ArrayList<>();

        public GradientGenerator(int resolution, Class<StopSpace> stopSpace, Class<InterpSpace> interpSpace, GammaRgbColor fallback) {
            this.interpSpace = interpSpace;
            this.stopSpace = stopSpace;
            this.resolution = resolution;
            this.cache = new GammaRgbColor[resolution];
            this.fallback = fallback;
            generate();
        }

        public GradientGenerator<StopSpace, InterpSpace> copyWithResolution(int resolution) {
            GradientGenerator<StopSpace, InterpSpace> copy = new GradientGenerator<StopSpace, InterpSpace>(resolution, stopSpace, interpSpace, fallback);
            copy.stops.addAll(stops);
            return copy;
        }

        public GammaRgbColor get(int x) {
            if(x < 0) return fallback;
            if(x >= this.resolution) return fallback;

            return cache[x];
        }

        public void generate() {
            stops.sort((o1, o2) -> Float.compare(o1.position, o2.position));
            for (int x = 0; x < resolution; x++) {
                InterpSpace color = getColor((x)/(float)(resolution - 1));
                if (color == null) {
                    cache[x] = fallback;
                } else {
                    cache[x] = color.to(GammaRgbColor.class);
                    cache[x].toGamut();
                }
            }
        }

        public InterpSpace getColor(float u) {
            if(stops.isEmpty()) return null;

            u = MathHelper.clamp(u, 0, 1);

            GradientStop<StopSpace> prev = stops.get(stops.size()-1), next = stops.get(stops.size()-1);
            for (int i = 0; i < stops.size(); i++) {
                if (u <= stops.get(i).position) {
                    prev = stops.get(Math.max(0, i-1));
                    next = stops.get(i);
                    break;
                }
            }

            float frac = 0.0f;
            if(next.position != prev.position) {
                frac = (u - prev.position) / (next.position - prev.position);
            }

            return prev.color.to(interpSpace).lerp(next.color, frac);
        }

        public void addStop(GradientStop<StopSpace> stop) {
            stops.add(stop);
        }

        public List<GradientStop<StopSpace>> getStops() {
            return ImmutableList.copyOf(stops);
        }

        public void removeStop(GradientStop<?> stop) {
            stops.remove(stop);
        }

        public GammaRgbColor[] getAll() {
            return Arrays.copyOf(cache, cache.length);
        }
    }

    public static class GradientStopGroup<StopSpace extends IColor<StopSpace, ?>> {
        public ChangeCallback<GradientStopElement<StopSpace>> onFocusedChanged;
        public ChangeCallback<GradientStopElement<StopSpace>> onHoveredChanged;

        protected GradientStopElement<StopSpace> hoveredElement;
        protected GradientStopElement<StopSpace> focusedElement;

        public boolean isHovered(GradientStopElement<?> element) {
            return hoveredElement == element;
        }

        public void setHovered(GradientStopElement<StopSpace> element) {
            if(hoveredElement == element) return;
            hoveredElement = element;
            if(onHoveredChanged != null) onHoveredChanged.callback(hoveredElement);
        }

        public boolean isFocused(GradientStopElement<?> element) {
            return focusedElement == element;
        }

        public void setFocused(GradientStopElement<StopSpace> element) {
            if(focusedElement == element) return;
            focusedElement = element;
            if(onFocusedChanged != null) onFocusedChanged.callback(focusedElement);
        }

        public boolean isAnyFocused() {
            return focusedElement != null;
        }

        public GradientStopElement<StopSpace> getFocused() {
            return focusedElement;
        }

        public boolean isAnyHovered() {
            return hoveredElement != null;
        }

        public GradientStopElement<StopSpace> getHovered() {
            return hoveredElement;
        }

        public interface ChangeCallback<T> {
            void callback(T value);
        }
    }

    public static class GradientStop<Space extends IColor<Space, ?>> {
        public float position;
        private Space color;

        public GradientStop(float position, Space color) {
            this.position = position;
            this.color = color;
        }

        public void setColor(Space color) {
            this.color = color.copy();
            this.color.toGamut();
        }

        public Space color() {
            return this.color;
        }
    }

    public static class GradientStopElement<StopSpace extends IColor<StopSpace, ?>> implements Drawable, Element {
        protected final Bounds area;
        protected final GradientStop<StopSpace> stop;
        private GammaRgbColor cache;
        protected final Bounds bounds;
        protected final Bounds pointerBounds;
        protected boolean locked;
        protected final GradientStopGroup<StopSpace> group;

        public GradientStopElement(Bounds area, GradientStop<StopSpace> stop, GradientStopGroup<StopSpace> group) {
            this.area = new Bounds(area);
            this.bounds = new Bounds(area).setWidth(11).expand(0, 0, 16, 0);
            this.pointerBounds = new Bounds(bounds).setY(area.limitY()+1).setSize(11, 16);
            this.stop = stop;
            this.group = group;
            setPosition(stop.position);
            setColor(stop.color);
        }

        public void setPosition(float pos) {
            if(locked) return;
            pos = MathHelper.clamp(pos, 0, 1);
            int x = area.x() + (int)(pos * (area.width()-1)) - 11 / 2;
            stop.position = pos;
            bounds.setX(x);
            pointerBounds.setX(bounds.x());
        }

        public <S extends IColor<S, ?>> void setColor(S color) {
            if(locked) return;
            this.stop.setColor(this.stop.color().convert(color));
            this.cache = this.stop.color().to(GammaRgbColor.class);
        }

        public void setLocked(boolean locked) {
            this.locked = locked;
        }

        public boolean locked() {
            return locked;
        }

        public float position() {
            return stop.position;
        }

        public StopSpace color() {
            return stop.color();
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            boolean active = group.isHovered(this) || isFocused();

            if(active) {
                boolean bright = isBright(stop.color().to(GammaRgbColor.class));
                context.drawVerticalLine(pointerBounds.x() + 11 / 2, pointerBounds.y()-area.height()/3-2, pointerBounds.y(), bright ? Colors.BLACK : Colors.WHITE);
            }

            if(locked) {
                context.drawTexture(ICONS_TEXTURE, pointerBounds.x()+3, pointerBounds.y(), 8, 16, 32, active ? 48 : 32, 8, 16, 64, 64);
                return;
            }

            context.drawTexture(ICONS_TEXTURE, pointerBounds.x(), pointerBounds.y(), 12, 16, 0, active ? 16 : 0, 12, 16, 64, 64);
            context.fill(pointerBounds.x()+2, pointerBounds.y()+6, pointerBounds.x()+2+7, pointerBounds.y()+6+7, cache.pack());
        }

        @Override
        public void setFocused(boolean focused) {
            group.setFocused(this);
        }

        @Override
        public boolean isFocused() {
            return group.isFocused(this);
        }

        @Nullable
        @Override
        public GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
            if (!this.isFocused()) {
                return GuiNavigationPath.of(this);
            }
            return null;
        }
    }

    public <S extends IColor<S, ?>> void setSelectedStopColor(S color) {
        if(!stopGroup.isAnyFocused()) return;
        if(stopGroup.getFocused().locked()) return;
        stopGroup.getFocused().setColor(color);
        dirty = true;
    }

    public <S extends IColor<S, ?>> void setStopColor(int index, S color) {
        GradientStopElement<?> stop = stops.get(index);
        if(stopGroup.isFocused(stop)) {
            setSelectedStopColor(color);
            return;
        }
        stop.setColor(color);
        dirty = true;
    }

    public void setSelectedStopPosition(float pos) {
        if(!stopGroup.isAnyFocused()) return;
        if(stopGroup.getFocused().locked()) return;
        if(pos == stopGroup.getFocused().position()) return;
        stopGroup.getFocused().setPosition(pos);
        if(onStopMoved != null) onStopMoved.accept(pos);
        dirty = true;
        sort();
    }

    public <S extends IColor<S, ?>> void setStopPosition(int index, float pos) {
        GradientStopElement<?> stop = stops.get(index);
        if(stopGroup.isFocused(stop)) {
            setSelectedStopPosition(pos);
            return;
        }
        stop.setPosition(pos);
        dirty = true;
        sort();
    }

    public void unselectStop() {
        stopGroup.setFocused(null);
    }

    public int stopCount() {
        return stops.size();
    }

    public void removeSelectedStop() {
        if(!stopGroup.isAnyFocused()) return;
        this.stops.remove(stopGroup.getFocused());
        this.generator.removeStop(stopGroup.getFocused().stop);
        unselectStop();
        dirty = true;
    }

    public void removeStop(int index) {
        GradientStopElement<?> stop = stops.get(index);
        if(stopGroup.isFocused(stop)) {
            removeSelectedStop();
            return;
        }
        this.stops.remove(stop);
        this.generator.removeStop(stop.stop);
        dirty = true;
    }

    private void sort() {
        stops.sort((o1, o2) -> Float.compare(o1.position(), o2.position()));
    }

    public void selectStop(int i) {
        i = MathHelper.clamp(i, 0, stops.size());
        GradientStopElement<StopSpace> stop = stops.get(i);
        selectStop(stop);
    }

    protected void selectStop(GradientStopElement<StopSpace> stop) {
        stopGroup.setFocused(stop);
    }

    public void addStop(float position, StopSpace color, boolean select) {
        GradientStop<StopSpace> stop = new GradientStop<>(position, color);
        generator.addStop(stop);
        GradientStopElement<StopSpace> element = new GradientStopElement<>(gradientBounds, stop, stopGroup);
        stops.add(element);
        if(select) {
            selectStop(element);
        }
        dirty = true;
        sort();
    }

    public StopSpace selectedColor() {
        if(!stopGroup.isAnyFocused()) {
            return null;
        }
        return stopGroup.getFocused().color();
    }

    public float selectedPosition() {
        if(!stopGroup.isAnyFocused()) {
            return 0;
        }
        return stopGroup.getFocused().position();
    }

    public int selectedIndex() {
        if(!stopGroup.isAnyFocused()) {
            return -1;
        }
        return stops.indexOf(stopGroup.getFocused());
    }

    public void lockSelectedStop(boolean locked) {
        if(!stopGroup.isAnyFocused()) return;
        stopGroup.getFocused().setLocked(locked);
    }

    public void setStopLocked(int index, boolean locked) {
        GradientStopElement<?> stop = stops.get(index);
        if(stopGroup.isFocused(stop)) {
            lockSelectedStop(locked);
            return;
        }
        stop.setLocked(locked);
    }

    public boolean selectedLocked() {
        if(!stopGroup.isAnyFocused()) return false;
        return stopGroup.getFocused().locked();
    }

    public GammaRgbColor[] getGradient(int resolution) {
        GradientGenerator<StopSpace, ?> copy = generator.copyWithResolution(resolution);
        copy.generate();
        return copy.getAll();
    }
}
