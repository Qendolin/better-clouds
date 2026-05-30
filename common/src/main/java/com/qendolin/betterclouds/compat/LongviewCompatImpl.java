package com.qendolin.betterclouds.compat;

import page.langeweile.longview.impl.LongviewImpl;

public class LongviewCompatImpl extends LongviewCompat {

    @Override
    public boolean isReverseZ() {
        return LongviewImpl.isZReversed();
    }

    @Override
    public boolean isZClipped() {
        return LongviewImpl.isGlZZeroToOne();
    }
}
