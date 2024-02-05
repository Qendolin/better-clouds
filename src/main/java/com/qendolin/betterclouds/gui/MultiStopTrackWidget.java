package com.qendolin.betterclouds.gui;


import com.qendolin.betterclouds.Main;
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
import java.util.Collections;
import java.util.List;

public abstract class MultiStopTrackWidget<T extends MultiStopTrackWidget.StopElement<T, D>, D extends MultiStopTrackWidget.StopData> implements Element, Drawable, Selectable {
    private static final Identifier ICONS_TEXTURE = new Identifier(Main.MODID, "textures/gui/betterclouds/gui_icons.png");

    public StopChangedCallback<D> onStopChanged;
    public StopSelectedCallback<D> onStopSelected;
    public StopRemovedCallback<D> onStopRemoved;
    public StopAddedCallback<D> onStopAdded;

    @FunctionalInterface
    public interface StopChangedCallback<D extends MultiStopTrackWidget.StopData> {
        void invoke(int index, D data);
    }

    @FunctionalInterface
    public interface StopSelectedCallback<D extends MultiStopTrackWidget.StopData> {
        void invoke(int index, @Nullable D data);
    }

    @FunctionalInterface
    public interface StopAddedCallback<D extends MultiStopTrackWidget.StopData> {
        void invoke(int index, @Nullable D data);
    }

    @FunctionalInterface
    public interface StopRemovedCallback<D extends MultiStopTrackWidget.StopData> {
        void invoke(int index, @Nullable D data);
    }


    public boolean active = true;
    protected boolean focused = false;
    protected boolean hovered = false;
    public final Bounds bounds;
    public final Bounds trackBounds;


    protected final List<T> stops = new ArrayList<>();
    protected final StopGroup<T, D> stopGroup = new StopGroup<>();
    protected Bounds addStopBounds = null;

    protected boolean dirty;

    public MultiStopTrackWidget(Bounds bounds) {
        this.bounds = bounds;
        this.trackBounds = new Bounds(bounds).setHeight(20).inset(5, 0);
        this.stopGroup.onFocusedChanged = value -> {
            if (onStopSelected == null) return;
            if (value == null) {
                onStopSelected.invoke(-1, null);
            } else {
                onStopSelected.invoke(stops.indexOf(value), value.data);
            }
        };
    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        hovered = isMouseOver(mouseX, mouseY) && active;

        updateHoveredStop(mouseX, mouseY);

        drawTrack(context, mouseX, mouseY, delta);

        drawStops(context, mouseX, mouseY, delta);

        if (bounds.overlaps(mouseX, mouseY) && (!stopGroup.isAnyHovered() || Screen.hasControlDown())) {
            updateAddStop(mouseX, mouseY, delta);
            drawAddStop(context, mouseX, mouseY, delta);
        } else {
            addStopBounds = null;
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return bounds.overlaps(mouseX, mouseY);
    }

    protected void updateHoveredStop(int mouseX, int mouseY) {
        T current = null;
        if (hovered) {
            if (stopGroup.isAnyFocused() && stopGroup.getFocused().bounds.overlaps(mouseX, mouseY)) {
                current = stopGroup.getFocused();
            } else {
                for (T stop : stops) {
                    if (stop.bounds.overlaps(mouseX, mouseY)) {
                        current = stop;
                        if (!stopGroup.isFocused(stop)) {
                            break;
                        }
                    }
                }
            }
        }
        stopGroup.setHovered(current);
    }

    protected void drawTrack(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawBorder(trackBounds.x() - 1, trackBounds.y(), trackBounds.width() + 2, trackBounds.height() + 1, (hovered || focused) ? Colors.WHITE : 0xFF444444);
    }

    protected void drawStops(DrawContext context, int mouseX, int mouseY, float delta) {
        for (T stop : stops) {
            if (stopGroup.isFocused(stop) || stopGroup.isHovered(stop)) continue;
            stop.render(context, mouseX, mouseY, delta);
        }
        if (stopGroup.isAnyFocused()) {
            stopGroup.getFocused().render(context, mouseX, mouseY, delta);
        }
        if (stopGroup.isAnyHovered() && stopGroup.getHovered() != stopGroup.getFocused()) {
            stopGroup.getHovered().render(context, mouseX, mouseY, delta);
        }
    }

    protected void updateAddStop(int mouseX, int mouseY, float delta) {
        int hoverX = getTrackMouseX(mouseX);
        if (addStopBounds == null)
            addStopBounds = new Bounds(trackBounds).setSize(11, 16).offset(0, trackBounds.height() + 1);
        addStopBounds.setX(trackBounds.x() + hoverX - 11 / 2);
    }

    protected void drawAddStop(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawVerticalLine(addStopBounds.centerX(), trackBounds.limitY() - trackBounds.height() / 3 - 1, trackBounds.limitY(), Colors.WHITE);

        boolean hovered = addStopBounds.overlaps(mouseX, mouseY);
        context.drawTexture(ICONS_TEXTURE, addStopBounds.x(), addStopBounds.y(), 12, 16, 16, hovered ? 16 : 0, 12, 16, 64, 64);
    }

    protected int getTrackMouseX(int mouseX) {
        return MathHelper.clamp(mouseX - trackBounds.x(), 0, trackBounds.width() - 1);
    }

    @Nullable
    @Override
    public GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
        if (!this.active) {
            return null;
        }
        if (!this.isFocused()) {
            return GuiNavigationPath.of(this);
        }
        return null;
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
        if (focused && !stopGroup.isAnyFocused()) {
            focusStop(0);
        }
    }

    protected void focusStop(int index) {
        if(stops.isEmpty()) return;
        index = MathHelper.clamp(index, 0, stops.size());
        T stop = stops.get(index);
        focusStop(stop);
    }

    protected void focusStop(T stop) {
        stopGroup.setFocused(stop);
    }

    public ScreenRect getNavigationFocus() {
        return new ScreenRect(bounds.x(), bounds.y(), bounds.width(), bounds.height());
    }

    public boolean isHovered() {
        return hovered;
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
        if(!active) return false;
        if ((modifiers & GLFW.GLFW_MOD_ALT) != 0) {
            int index = focusedIndex();
            if (keyCode == GLFW.GLFW_KEY_LEFT && stopGroup.isAnyFocused()) {
                int prev = index == 0 ? count() - 1 : index - 1;
                focusStop(prev);
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_RIGHT && stopGroup.isAnyFocused()) {
                int next = (index + 1) % count();
                focusStop(next);
                return true;
            }

            return false;
        }

        float step = 1f / trackBounds.width();
        if ((modifiers & GLFW.GLFW_MOD_SHIFT) != 0) step *= 10;
        if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0) step /= 10;
        if (keyCode == GLFW.GLFW_KEY_LEFT && stopGroup.isAnyFocused()) {
            setStopPosition(focusedIndex(), getStopPosition(focusedIndex()) - step);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_RIGHT && stopGroup.isAnyFocused()) {
            setStopPosition(focusedIndex(), getStopPosition(focusedIndex()) + step);
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_DELETE && modifiers == 0) {
            removeStop(focusedIndex());
            return true;
        }

        return false;
    }

    public int focusedIndex() {
        if (!stopGroup.isAnyFocused()) {
            return -1;
        }
        return stops.indexOf(stopGroup.getFocused());
    }

    public D getStop(int index) {
        if(index == -1) return null;
        return stops.get(index).data;
    }


    public int count() {
        return stops.size();
    }

    public void setStopPosition(int index, float pos) {
        if (index == -1) return;

        T stop = stops.get(index);
        float prevPos = stop.getPosition();
        stop.setPosition(pos);
        if (prevPos == pos) return;

        if (onStopChanged != null) onStopChanged.invoke(index, stop.data);

        dirty = true;
        sortStops();
    }

    public float getStopPosition(int index) {
        if (index == -1) return 0;
        return stops.get(index).getPosition();
    }

    public T removeStop(int index) {
        if (index == -1) return null;

        T stop = stops.get(index);
        if (stopGroup.isFocused(stop)) {
            unselectStop();
        }
        this.stops.remove(stop);
        dirty = true;
        if(onStopRemoved != null) onStopRemoved.invoke(index, stop.data);
        return stop;
    }

    protected void sortStops() {
        Collections.sort(stops);
    }

    public void unselectStop() {
        stopGroup.setFocused(null);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(!active) return false;
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
        if (addStopBounds != null && addStopBounds.overlaps(mouseX, mouseY)) {
            float pos = getTrackPosition(mouseX);
            int index = addStop(createStopElement(createStop(pos)));
            focusStop(index);
            return true;
        }
        if (stopGroup.isAnyHovered() && stopGroup.getHovered().bounds.overlaps(mouseX, mouseY)) {
            focusStop(stopGroup.getHovered());
            return true;
        }
        if (stopGroup.isAnyFocused() && stopGroup.getFocused().bounds.overlaps(mouseX, mouseY)) {
            return true;
        }
        for (T stop : stops) {
            if (stop.bounds.overlaps(mouseX, mouseY)) {
                focusStop(stop);
                return true;
            }
        }
        return false;
    }

    protected float getTrackPosition(double mouseX) {
        float pos = ((float) mouseX - trackBounds.x()) / (trackBounds.width() - 1f);
        pos = MathHelper.clamp(pos, 0, 1);
        return pos;
    }

    protected int addStop(T stop) {
        stops.add(stop);
        dirty = true;
        sortStops();
        int index = stops.indexOf(stop);
        if(onStopAdded != null) onStopAdded.invoke(index, stop.data);
        return index;
    }

    protected int addStop(D data) {
        T element = createStopElement(data);
        return addStop(element);
    }

    protected void setStops(List<D> stops) {
        unselectStop();
        dirty = true;
        this.stops.clear();
        if(stops != null) {
            for (D stop : stops) {
                this.stops.add(createStopElement(stop));
            }
        }
    }

    protected abstract T createStopElement(D data);

    protected abstract D createStop(float pos);

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if(!active) return false;
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
        if (!stopGroup.isAnyFocused()) return false;

        float pos = getTrackPosition(mouseX);
        setStopPosition(focusedIndex(), pos);

        return true;
    }

    public D focusedStop() {
        if (!stopGroup.isAnyFocused()) {
            return null;
        }
        return stopGroup.getFocused().data;
    }

    public boolean isStopLocked(int index) {
        if (index == -1) return false;
        return stops.get(index).isLocked();
    }

    public void setStopLocked(int index, boolean locked) {
        if (index == -1) return;
        stops.get(index).setLocked(locked);
    }

    public static class StopGroup<T extends StopElement<T, D>, D extends StopData> {
        public ChangeCallback<T> onFocusedChanged;
        public ChangeCallback<T> onHoveredChanged;

        protected T hoveredElement;
        protected T focusedElement;

        public boolean isHovered(T element) {
            return hoveredElement == element;
        }

        public boolean isFocused(T element) {
            return focusedElement == element;
        }

        public boolean isAnyFocused() {
            return focusedElement != null;
        }

        public T getFocused() {
            return focusedElement;
        }

        public void setFocused(T element) {
            if (focusedElement == element) return;
            focusedElement = element;
            if (onFocusedChanged != null) onFocusedChanged.callback(focusedElement);
        }

        public boolean isAnyHovered() {
            return hoveredElement != null;
        }

        public T getHovered() {
            return hoveredElement;
        }

        public void setHovered(T element) {
            if (hoveredElement == element) return;
            hoveredElement = element;
            if (onHoveredChanged != null) onHoveredChanged.callback(hoveredElement);
        }

        public interface ChangeCallback<T> {
            void callback(T value);
        }
    }

    public static class StopData implements Comparable<StopData> {
        protected boolean locked;
        protected float position;

        public StopData(float position) {
            this.position = position;
        }

        public boolean locked() {
            return locked;
        }

        public void setLocked(boolean locked) {
            this.locked = locked;
        }

        public float position() {
            return position;
        }

        public void setPosition(float position) {
            this.position = position;
        }

        @Override
        public int compareTo(StopData o) {
            return Float.compare(this.position(), o.position());
        }
    }

    public static class StopElement<T extends StopElement<T, D>, D extends StopData> implements Drawable, Element, Comparable<StopElement<T, D>> {
        protected final Bounds trackBounds;
        protected final Bounds bounds;
        protected final Bounds pointerBounds;
        protected final StopGroup<T, D> group;

        protected D data;

        public StopElement(Bounds trackBounds, StopGroup<T, D> group, D data) {
            this.trackBounds = new Bounds(trackBounds);
            this.bounds = new Bounds(trackBounds).setWidth(11).expand(0, 0, 16, 0);
            this.pointerBounds = new Bounds(bounds).setY(trackBounds.limitY() + 1).setSize(11, 16);
            this.group = group;
            this.data = data;
            setPosition(data.position());
            setLocked(data.locked());
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            boolean active = isHovered() || isFocused();

            if (active) {
                context.drawVerticalLine(pointerBounds.x() + 11 / 2, pointerBounds.y() - trackBounds.height() / 3 - 2, pointerBounds.y(), Colors.WHITE);
            }

            if (data.locked()) {
                context.drawTexture(ICONS_TEXTURE, pointerBounds.x() + 3, pointerBounds.y(), 8, 16, 32, active ? 48 : 32, 8, 16, 64, 64);
                return;
            }

            context.drawTexture(ICONS_TEXTURE, pointerBounds.x(), pointerBounds.y(), 12, 16, 0, active ? 16 : 0, 12, 16, 64, 64);
        }

        public boolean isHovered() {
            //noinspection unchecked
            return group.isHovered((T) this);
        }

        @Override
        public boolean isFocused() {
            //noinspection unchecked
            return group.isFocused((T) this);
        }

        @Override
        public void setFocused(boolean focused) {
            //noinspection unchecked
            group.setFocused((T) this);
        }

        public float getPosition() {
            return data.position();
        }

        public void setPosition(float pos) {
            if (data.locked()) return;

            data.setPosition(pos);
            int x = trackBounds.x() + (int) (pos * (trackBounds.width() - 1)) - 11 / 2;
            bounds.setX(x);
            pointerBounds.setX(bounds.x());
        }

        public boolean isLocked() {
            return data.locked();
        }

        public void setLocked(boolean locked) {
            data.setLocked(locked);
        }

        @Override
        public int compareTo(StopElement<T, D> o) {
            return data.compareTo(o.data);
        }
    }
}
