package com.qendolin.betterclouds.gui;

import com.google.common.collect.ImmutableList;
import com.qendolin.betterclouds.Main;
import com.qendolin.betterclouds.gui.color.GammaRgbColor;
import com.qendolin.betterclouds.gui.color.IColor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GradientWidget<StopSpace extends IColor<StopSpace, ?>> extends MultiStopTrackWidget<GradientWidget.GradientStopElement<StopSpace>, GradientWidget.GradientStop<StopSpace>> {
    private static final Identifier ICONS_TEXTURE = new Identifier(Main.MODID, "textures/gui/betterclouds/gui_icons.png");

    protected Class<? extends IColor<?, ?>> interpSpace;
    protected final Class<StopSpace> stopSpace;

    protected GradientGenerator<StopSpace, ?> generator;

    public <InterpSpace extends IColor<InterpSpace, ?>> GradientWidget(Bounds bounds, Class<StopSpace> stopSpace, Class<InterpSpace> interpSpace) {
        super(bounds);
        this.interpSpace = interpSpace;
        this.stopSpace = stopSpace;
        setInterpSpace(interpSpace);
    }

    public <InterpSpace extends IColor<InterpSpace, ?>> void setInterpSpace(Class<InterpSpace> space) {
        GradientGenerator<StopSpace, ?> old = generator;
        generator = new GradientGenerator<>(trackBounds.width(), stopSpace, space, new GammaRgbColor(0, 0, 0, 1));
        if (old != null) {
            for (GradientStop<StopSpace> stop : old.getStops()) {
                generator.addStop(stop);
            }
        }
        dirty = true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (dirty) {
            generator.generate();
            dirty = false;
        }
        super.render(context, mouseX, mouseY, delta);
    }

    protected void drawTrack(DrawContext context, int mouseX, int mouseY, float delta) {
        for (int x = 0; x < trackBounds.width(); x++) {
            context.drawVerticalLine(trackBounds.x() + x, trackBounds.y(), trackBounds.limitY(), generator.get(x).pack());
        }
        super.drawTrack(context, mouseX, mouseY, delta);
    }

    protected void drawAddStop(DrawContext context, int mouseX, int mouseY, float delta) {
        int lineX = addStopBounds.centerX();
        GammaRgbColor color = generator.get(lineX - trackBounds.x());
        context.drawVerticalLine(lineX, trackBounds.limitY() - trackBounds.height() / 3 - 1, trackBounds.limitY(), isBright(color) ? Colors.BLACK : Colors.WHITE);

        boolean hovered = addStopBounds.overlaps(mouseX, mouseY);
        context.drawTexture(ICONS_TEXTURE, addStopBounds.x(), addStopBounds.y(), 12, 16, 16, hovered ? 16 : 0, 12, 16, 64, 64);
    }

    private static boolean isBright(GammaRgbColor srgb) {
        float luma = (srgb.red * 2 + srgb.blue + srgb.green * 3) / 6;
        return luma > 0.5;
    }

    @Override
    protected GradientStopElement<StopSpace> createStopElement(GradientStop<StopSpace> data) {
        return new GradientStopElement<>(trackBounds, data, stopGroup);
    }

    @Override
    protected GradientStop<StopSpace> createStop(float pos) {
        StopSpace color;
        if (stops.isEmpty()) {
            color = new GammaRgbColor(0, 0, 0, 1).to(stopSpace);
        } else {
            color = generator.getColor(pos).to(stopSpace);
        }
        return new GradientStop<>(pos, color);
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

        public void generate() {
            stops.sort((o1, o2) -> Float.compare(o1.position, o2.position));
            for (int x = 0; x < resolution; x++) {
                InterpSpace color = getColor((x) / (float) (resolution - 1));
                if (color == null) {
                    cache[x] = fallback;
                } else {
                    cache[x] = color.to(GammaRgbColor.class);
                    cache[x].toGamut();
                }
            }
        }

        public InterpSpace getColor(float p) {
            if (stops.isEmpty()) return null;

            p = MathHelper.clamp(p, 0, 1);

            GradientStop<StopSpace> prev = stops.get(stops.size() - 1), next = stops.get(stops.size() - 1);
            for (int i = 0; i < stops.size(); i++) {
                if (p <= stops.get(i).position) {
                    prev = stops.get(Math.max(0, i - 1));
                    next = stops.get(i);
                    break;
                }
            }

            float frac = 0.0f;
            if (next.position != prev.position) {
                frac = (p - prev.position) / (next.position - prev.position);
            }

            return prev.color.to(interpSpace).lerp(next.color, frac);
        }

        public GradientGenerator<StopSpace, InterpSpace> copyWithResolution(int resolution) {
            GradientGenerator<StopSpace, InterpSpace> copy = new GradientGenerator<StopSpace, InterpSpace>(resolution, stopSpace, interpSpace, fallback);
            copy.stops.addAll(stops);
            return copy;
        }

        public GammaRgbColor get(int i) {
            if (i < 0) return fallback;
            if (i >= this.resolution) return fallback;

            return cache[i];
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

    public static class GradientStop<Space extends IColor<Space, ?>> extends StopData {
        private Space color;

        public GradientStop(float position, Space color) {
            super(position);
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

    public static class GradientStopElement<StopSpace extends IColor<StopSpace, ?>> extends StopElement<GradientStopElement<StopSpace>, GradientStop<StopSpace>> {
        private GammaRgbColor cache;

        public GradientStopElement(Bounds trackBounds, GradientStop<StopSpace> stop, StopGroup<GradientStopElement<StopSpace>, GradientStop<StopSpace>> group) {
            super(trackBounds, group, stop);
            setColor(stop.color);
        }

        public StopSpace getColor() {
            return data.color();
        }

        public <S extends IColor<S, ?>> void setColor(S color) {
            if (data.locked()) return;
            this.data.setColor(this.data.color().convert(color));
            this.cache = this.data.color().to(GammaRgbColor.class);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            boolean active = group.isHovered(this) || isFocused();

            if (active) {
                boolean bright = isBright(data.color().to(GammaRgbColor.class));
                context.drawVerticalLine(pointerBounds.x() + 11 / 2, pointerBounds.y() - trackBounds.height() / 3 - 2, pointerBounds.y(), bright ? Colors.BLACK : Colors.WHITE);
            }

            if (data.locked()) {
                context.drawTexture(ICONS_TEXTURE, pointerBounds.x() + 3, pointerBounds.y(), 8, 16, 32, active ? 48 : 32, 8, 16, 64, 64);
                return;
            }

            context.drawTexture(ICONS_TEXTURE, pointerBounds.x(), pointerBounds.y(), 12, 16, 0, active ? 16 : 0, 12, 16, 64, 64);
            context.fill(pointerBounds.x() + 2, pointerBounds.y() + 6, pointerBounds.x() + 2 + 7, pointerBounds.y() + 6 + 7, cache.pack());
        }
    }

    @Override
    public GradientStopElement<StopSpace> removeStop(int index) {
        GradientStopElement<StopSpace> removed = super.removeStop(index);
        if (removed == null) return null;
        generator.removeStop(removed.data);
        return removed;
    }

    public <S extends IColor<S, ?>> void setStopColor(int index, S color) {
        if (index == -1) return;
        GradientStopElement<StopSpace> stop = stops.get(index);
        stop.setColor(color);
        if (onStopChanged != null) onStopChanged.invoke(index, stop.data);
        dirty = true;
    }

    public StopSpace getStopColor(int index) {
        if (index == -1) return null;
        return stops.get(index).getColor();
    }

    public GammaRgbColor[] getGradient(int resolution) {
        GradientGenerator<StopSpace, ?> copy = generator.copyWithResolution(resolution);
        copy.generate();
        return copy.getAll();
    }

    public int addStop(float pos, StopSpace color) {
        return addStop(new GradientStopElement<>(trackBounds, new GradientStop<>(pos, color), stopGroup));
    }

    @Override
    protected int addStop(GradientStopElement<StopSpace> stop) {
        int index = super.addStop(stop);
        generator.addStop(stop.data);
        return index;
    }
}
