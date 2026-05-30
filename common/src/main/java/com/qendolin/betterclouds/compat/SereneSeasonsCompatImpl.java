package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.config.ConfigManager;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;

public class SereneSeasonsCompatImpl extends SereneSeasonsCompat {
    private static final Api API = Api.load();

    private static String seasonName(Object season) {
        if (season instanceof Enum<?> enumSeason) {
            return enumSeason.name();
        }
        return season == null ? "" : season.toString();
    }

    private Object getRelativeSeason(Object season, int d) {
        if (d == 0) {
            return season;
        }

        if (!(season instanceof Enum<?> enumSeason) || API == null || API.subSeasons.length == 0) {
            return season;
        }

        int index = enumSeason.ordinal();
        int length = API.subSeasons.length;
        index = ((index + d) % length + length) % length;
        return API.subSeasons[index];
    }

    private float getSeasonCloudiness(Object season) {
        String key = seasonName(season).toLowerCase(Locale.ROOT);
        return SUB_SEASON_CLOUDINESS_LOOKUP.getOrDefault(key, config -> 1.0f)
                .apply(ConfigManager.instance().sereneSeasonsConfig);
    }

    private int getSubSeasonTicks(Object state) throws InvocationTargetException, IllegalAccessException {
        int time = (int) API.getSeasonCycleTicks.invoke(state);
        int duration = (int) API.getSubSeasonDuration.invoke(state);
        if (duration <= 0) {
            return 0;
        }
        return time % duration;
    }

    @Override
    public float getCloudinessFactor(Level world) {
        if (API == null) {
            return 1.0f;
        }
        try {
            Object state = API.getSeasonState.invoke(null, world);
            if (state == null) {
                return 1.0f;
            }
            Object season = API.getSubSeason.invoke(state);
            if (season == null) {
                return 1.0f;
            }

            int seasonTicks = getSubSeasonTicks(state);
            int seasonDuration = (int) API.getSubSeasonDuration.invoke(state);
            if (seasonDuration <= 0) {
                return getSeasonCloudiness(season);
            }
            int half = seasonTicks < seasonDuration / 2 ? 0 : 1;

            int dayDuration = 24000;
            if (API.getDayDuration != null) {
                dayDuration = (int) API.getDayDuration.invoke(state);
            }
            int transitionTicks = (int) (ConfigManager.instance().sereneSeasonsConfig.transitionDays * dayDuration);
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

            return Mth.clampedLerp(start, end, blend);
        } catch (InvocationTargetException | IllegalAccessException e) {
            return 1.0f;
        }
    }

    private record Api(Method getSeasonState, Method getSubSeason, Method getSeasonCycleTicks,
                       Method getSubSeasonDuration, Method getDayDuration, Object[] subSeasons) {

        private static Api load() {
            try {
                Class<?> seasonHelperClass = Class.forName("sereneseasons.api.season.SeasonHelper");
                Method getSeasonState = Arrays.stream(seasonHelperClass.getMethods())
                        .filter(method -> method.getName().equals("getSeasonState") && method.getParameterCount() == 1)
                        .findFirst()
                        .orElseThrow(NoSuchMethodException::new);

                Class<?> seasonStateClass = Class.forName("sereneseasons.api.season.ISeasonState");
                Method getSubSeason = seasonStateClass.getMethod("getSubSeason");
                Method getSeasonCycleTicks = seasonStateClass.getMethod("getSeasonCycleTicks");
                Method getSubSeasonDuration = seasonStateClass.getMethod("getSubSeasonDuration");
                Method getDayDuration = Arrays.stream(seasonStateClass.getMethods())
                        .filter(method -> method.getName().equals("getDayDuration") && method.getParameterCount() == 0)
                        .findFirst()
                        .orElse(null);

                Class<?> subSeasonClass = Class.forName("sereneseasons.api.season.Season$SubSeason");
                Method values = subSeasonClass.getMethod("values");
                Object[] subSeasons = (Object[]) values.invoke(null);

                return new Api(getSeasonState, getSubSeason, getSeasonCycleTicks, getSubSeasonDuration, getDayDuration, subSeasons);
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                     InvocationTargetException ignored) {
                return null;
            }
        }
    }
}
