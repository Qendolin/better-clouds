package com.qendolin.betterclouds.gui;

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
        public TimelineKeyframe(float position) {
            super(position);
        }
    }

    public static class TimelineKeyframeElement extends StopElement<TimelineKeyframeElement, TimelineKeyframe> {

        public TimelineKeyframeElement(Bounds trackBounds, TimelineKeyframe keyframe, StopGroup<TimelineKeyframeElement, TimelineKeyframe> group) {
            super(trackBounds, group, keyframe);
        }
    }

    public int addStop(float pos) {
        return addStop(createStopElement(createStop(pos)));
    }
}
