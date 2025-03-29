package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.Main;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import sereneseasons.api.season.ISeasonState;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;

public class SereneSeasonsCompatImpl extends SereneSeasonsCompat {

    private Season.SubSeason getRelativeSeason(Season.SubSeason season, int d) {
        if (d == 0)
            return season;

        int index = season.ordinal();
        int length = Season.SubSeason.values().length;
        index = ((index + d) % length + length) % length;
        return Season.SubSeason.values()[index];
    }

    private float getSeasonCloudiness(Season.SubSeason season) {
        String key = season.asString();
        return SUB_SEASON_CLOUDINESS_LOOKUP.getOrDefault(key, config -> 1.0f)
            .apply(Main.getConfig().sereneSeasonsConfig);
    }

    private int getSubSeasonTicks(ISeasonState state) {
        int time = state.getSeasonCycleTicks();
        int duration = state.getSubSeasonDuration();
        return time % duration;
    }

    @Override
    public float getCloudinessFactor(World world) {
        var state = SeasonHelper.getSeasonState(world);
        if (state == null) return 1.0f;
        var season = state.getSubSeason();
        if (season == null) return 1.0f;

        int seasonTicks = getSubSeasonTicks(state);
        int seasonDuration = state.getSubSeasonDuration();
        int half = seasonTicks < seasonDuration / 2 ? 0 : 1;

        int transitionTicks = (int) (Main.getConfig().sereneSeasonsConfig.transitionDays * state.getDayDuration());
        transitionTicks = Math.min(transitionTicks, seasonDuration);

        if (transitionTicks <= 0) {
            return getSeasonCloudiness(season);
        }

        float start = getSeasonCloudiness(getRelativeSeason(season, half - 1));
        float end = getSeasonCloudiness(getRelativeSeason(season, half));

        float blend;
        if (half == 0) {
            blend = (float) seasonTicks / transitionTicks + 0.5f;
        } else {
            blend = (float) (seasonDuration - seasonTicks) / transitionTicks + 0.5f;
            blend = 1.0f - blend;
        }

        return MathHelper.clampedLerp(start, end, blend);
    }
}
