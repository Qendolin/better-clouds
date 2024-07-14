package com.qendolin.betterclouds.compat;

import com.seibel.distanthorizons.api.DhApi;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiRenderParam;
import com.seibel.distanthorizons.api.objects.math.DhApiMat4f;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class DistantHorizons3CompatImpl extends DistantHorizonsSharedCompatImpl {

    private Field dhProjectionMatrixField;
    private Method getValuesAsArrayMethod;

    public DistantHorizons3CompatImpl() {
        super();

        try {
            dhProjectionMatrixField = DhApiRenderParam.class.getField("dhProjectionMatrix");
            getValuesAsArrayMethod = DhApiMat4f.class.getMethod("getValuesAsArray");
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            throw new RuntimeException("Your versions of Better Clouds and Distant Horizons are not compatible!", e);
        }
    }


    @Override
    public void disableLodClouds() {
        var option = DhApi.Delayed.configs.graphics().genericRendering().cloudRenderingEnabled();
        option.setValue(false);
    }

    float[] getDhProjectionMatrixValues(DhApiRenderParam renderParam) {
        try {
            // DhApiMat4f
            Object matrix = dhProjectionMatrixField.get(renderParam);
            Object values = getValuesAsArrayMethod.invoke(matrix);
            return (float[]) values;
        } catch (IllegalAccessException | InvocationTargetException | ClassCastException e) {
            throw new RuntimeException("Your versions of Better Clouds and Distant Horizons are not compatible!", e);
        }
    }

}
