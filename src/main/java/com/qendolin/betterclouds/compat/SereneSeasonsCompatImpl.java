package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.Main;
import net.minecraft.world.World;
import sereneseasons.api.season.SeasonHelper;

public class SereneSeasonsCompatImpl extends SereneSeasonsCompat {

    @Override
    public float getCloudinessFactor(World world) {
        var state = SeasonHelper.getSeasonState(world);
        if(state == null) return 1.0f;
        var season = state.getSubSeason();
        if(season == null) return 1.0f;
        String key = season.asString();
        return SUB_SEASON_CLOUDINESS_VALUES.getOrDefault(key, config -> 1.0f)
            .apply(Main.getConfig().sereneSeasonsConfig);
    }
}
