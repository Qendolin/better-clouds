package com.qendolin.betterclouds.config.preset;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public final class PresetListSerializer<T extends AbstractPresetConfig> implements JsonSerializer<List<T>> {
    @Override
    public JsonElement serialize(List<T> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray json = new JsonArray();
        for (T preset : src) {
            if (preset.key == null || preset.editable) {
                json.add(context.serialize(preset));
            }
        }
        return json;
    }
}
