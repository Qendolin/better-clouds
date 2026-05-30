package com.qendolin.betterclouds.config.preset;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public final class PresetListSerializer<T extends AbstractPresetConfig> implements JsonSerializer<List<T>> {
    private final Supplier<Set<String>> defaultKeys;

    public PresetListSerializer(Supplier<Set<String>> defaultKeys) {
        this.defaultKeys = defaultKeys;
    }

    @Override
    public JsonElement serialize(List<T> src, Type typeOfSrc, JsonSerializationContext context) {
        Set<String> currentDefaultKeys = defaultKeys.get();
        JsonArray json = new JsonArray();
        for (T preset : src) {
            if (preset.key == null || !currentDefaultKeys.contains(preset.key)) {
                json.add(context.serialize(preset));
            }
        }
        return json;
    }
}
