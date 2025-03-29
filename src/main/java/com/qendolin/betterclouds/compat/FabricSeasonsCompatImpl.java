package com.qendolin.betterclouds.compat;

//? if fabric {

import com.qendolin.betterclouds.Main;
import io.github.lucaargolo.seasons.FabricSeasons;
import io.github.lucaargolo.seasons.utils.Season;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class FabricSeasonsCompatImpl extends FabricSeasonsCompat {

    private float getSeasonCloudiness(Season season) {
        String key = season.asString();
        return SEASON_CLOUDINESS_LOOKUP.getOrDefault(key, config -> 1.0f)
            .apply(Main.getConfig().fabricSeasonsConfig);
    }

    @Override
    public float getCloudinessFactor(World world) {
        var season = FabricSeasons.getCurrentSeason(world);
        if (season == null)
            return 1.0f;


        long duration = season.getSeasonLength();
        long transitionTicks = (long) (Main.getConfig().fabricSeasonsConfig.transitionDays * 24000);
        transitionTicks = Math.min(transitionTicks, duration);
        if (transitionTicks <= 0) {
            return getSeasonCloudiness(season);
        }

        long ticksLeft = FabricSeasons.getTimeToNextSeason(world);

        float curr = getSeasonCloudiness(season);
        float next = getSeasonCloudiness(FabricSeasons.getNextSeason(world, season));

        float blend = 1.0f - (float) ticksLeft / transitionTicks;

        return MathHelper.clampedLerp(curr, next, blend);
    }
}
//?} else {
/*public class FabricSeasonsCompatImpl extends FabricSeasonsCompat.Stub {
}
*///?}