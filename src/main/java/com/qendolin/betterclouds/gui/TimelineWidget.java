package com.qendolin.betterclouds.gui;

import com.qendolin.betterclouds.gui.color.GammaRgbColor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Colors;

public class TimelineWidget extends MultiStopTrackWidget<TimelineWidget.TimelineKeyframeElement, TimelineWidget.TimelineKeyframe> {

    public TimelineWidget(Bounds bounds) {
        super(bounds);
    }

    @Override
    protected TimelineKeyframeElement createStopElement(TimelineKeyframe data) {
        return new TimelineKeyframeElement(trackBounds, data, stopGroup);
    }

    @Override
    protected TimelineKeyframe createStop(float pos) {
        return new TimelineKeyframe(pos);
    }

    public static class TimelineKeyframe extends StopData {
        private GammaRgbColor color1 = new GammaRgbColor(0, 0, 0, 1);
        private GammaRgbColor color2 = new GammaRgbColor(0, 0, 0, 1);
        public TimelineKeyframe(float position) {
            super(position);
        }

        public void setColors(GammaRgbColor color1, GammaRgbColor color2) {
            this.color1 = color1;
            this.color2 = color2;
        }
    }

    public static class TimelineKeyframeElement extends StopElement<TimelineKeyframeElement, TimelineKeyframe> {

        public TimelineKeyframeElement(Bounds trackBounds, TimelineKeyframe keyframe, StopGroup<TimelineKeyframeElement, TimelineKeyframe> group) {
            super(trackBounds, group, keyframe);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);
            if(data != null) {
                context.fillGradient(pointerBounds.x() + 2, pointerBounds.y() + 6, pointerBounds.x() + 2 + 7, pointerBounds.y() + 6 + 7, data.color1.pack(), data.color2.pack());
//                context.fill(pointerBounds.x() + 2, pointerBounds.y() + 6, pointerBounds.x() + 2 + 7, pointerBounds.y() + 6 + 7, data.color.pack());
            }
        }
    }

    public int addStop(float pos) {
        return addStop(createStopElement(createStop(pos)));
    }
}
