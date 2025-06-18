package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.mixin.optional.AstrocraftPlanetManagerAccessor;
import com.qendolin.betterclouds.mixin.optional.AstrocraftPlanetRendererAccessor;
import mod.lwhrvw.astrocraft.MathFuncs;
import mod.lwhrvw.astrocraft.planets.Body;
import mod.lwhrvw.astrocraft.planets.PlanetManager;
import mod.lwhrvw.astrocraft.planets.StarBody;
import org.joml.Vector2f;
import org.joml.Vector3d;

public class AstrocraftCompat {

    public static Vector2f getMoonDirection() {
        double time = PlanetManager.getPlanetTime();
        Body obs = AstrocraftPlanetManagerAccessor.getObsMount();
        StarBody sun = AstrocraftPlanetManagerAccessor.getSun();
        if(sun == null || obs == null)
            return null;

        Body moon = sun.findChild("earth.moon");
        Vector3d direction = moon.getAngularPosition(obs, time);
        return new Vector2f((float) direction.x, (float) direction.y);
    }

    public static Vector2f getSunDirection() {
        double time = PlanetManager.getPlanetTime();
        Body obs = AstrocraftPlanetManagerAccessor.getObsMount();
        StarBody sun = AstrocraftPlanetManagerAccessor.getSun();
        if(sun == null || obs == null)
            return null;

        Vector3d direction = sun.getAngularPosition(obs, time);
        return new Vector2f((float) direction.x, (float) direction.y);
    }
}
