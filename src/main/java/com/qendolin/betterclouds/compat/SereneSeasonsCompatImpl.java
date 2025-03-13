package com.qendolin.betterclouds.compat;

import net.minecraft.world.World;
import sereneseasons.api.season.SeasonHelper;

public class SereneSeasonsCompatImpl extends SereneSeasonsCompat {

    public SereneSeasonsCompatImpl() {

    }

    @Override
    public float getCloudinessFactor(World world) {
        var state = SeasonHelper.getSeasonState(world);
        if(state == null) return 1.0f;
        var season = state.getSubSeason();
        if(season == null) return 1.0f;
        String key = season.asString();
        return SUB_SEASON_CLOUDINESS_VALUES.getOrDefault(key, 1.0f);
    }
}
