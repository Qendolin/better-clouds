package com.qendolin.betterclouds.clouds;

import java.util.Objects;

public class PrimitiveChangeDetector {
    private Object[] values;
    private final boolean initialResult;

    public PrimitiveChangeDetector() {
        initialResult = true;
    }
    public PrimitiveChangeDetector(boolean initialResult) {
        this.initialResult = initialResult;
    }

    public void reset() {
        this.values = null;
    }

    public boolean hasChanged(Object ...values) {
        if(this.values == null) {
            this.values = values;
            return initialResult;
        }

        for (int i = 0; i < this.values.length; i++) {
            if(!Objects.equals(values[i], this.values[i])) {
                this.values = values;
                return true;
            }
        }

        return false;
    }
}
