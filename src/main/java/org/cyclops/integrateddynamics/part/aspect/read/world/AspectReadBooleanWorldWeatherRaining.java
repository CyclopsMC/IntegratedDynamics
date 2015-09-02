package org.cyclops.integrateddynamics.part.aspect.read.world;

import net.minecraft.world.World;

/**
 * Aspect that checks if there is rain in the world.
 * @author rubensworks
 */
public class AspectReadBooleanWorldWeatherRaining extends AspectReadBooleanWorldWeatherBase {

    @Override
    protected String getUnlocalizedWeatherType() {
        return "raining";
    }

    @Override
    protected boolean isWeather(World world) {
        return world.isRaining();
    }
}
