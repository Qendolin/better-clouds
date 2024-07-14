package com.qendolin.betterclouds.compat;

import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiRenderParam;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class DistantHorizons2CompatImpl extends DistantHorizonsSharedCompatImpl {

    private Field dhProjectionMatrixField;
    private Method getValuesAsArrayMethod;

    public DistantHorizons2CompatImpl() {
        super();

        try {
            dhProjectionMatrixField = DhApiRenderParam.class.getField("dhProjectionMatrix");
            getValuesAsArrayMethod = Class.forName("com.seibel.distanthorizons.coreapi.util.math.Mat4f").getMethod("getValuesAsArray");
        } catch (NoSuchFieldException | NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException("Your versions of Better Clouds and Distant Horizons are not compatible!", e);
        }
    }

    @Override
    public void disableLodClouds() {
        // nothing
    }

    float[] getDhProjectionMatrixValues(DhApiRenderParam renderParam) {
        try {
            // Mat4f
            Object matrix = dhProjectionMatrixField.get(renderParam);
            Object values = getValuesAsArrayMethod.invoke(matrix);
            return (float[]) values;
        } catch (IllegalAccessException | InvocationTargetException | ClassCastException e) {
            throw new RuntimeException("Your versions of Better Clouds and Distant Horizons are not compatible!", e);
        }
    }

}
