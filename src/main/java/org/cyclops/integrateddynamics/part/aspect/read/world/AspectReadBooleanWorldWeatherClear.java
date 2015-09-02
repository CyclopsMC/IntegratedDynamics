package org.cyclops.integrateddynamics.part.aspect.read.world;

import net.minecraft.world.World;

/**
 * Aspect that checks if there is clear weather in the world.
 * @author rubensworks
 */
public class AspectReadBooleanWorldWeatherClear extends AspectReadBooleanWorldWeatherBase {

    @Override
    protected String getUnlocalizedWeatherType() {
        return "clear";
    }

    @Override
    protected boolean isWeather(World world) {
        return !world.isRaining();
    }
}
