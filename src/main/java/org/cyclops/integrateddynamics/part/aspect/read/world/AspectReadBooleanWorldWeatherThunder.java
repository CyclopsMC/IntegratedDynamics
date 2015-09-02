package org.cyclops.integrateddynamics.part.aspect.read.world;

import net.minecraft.world.World;

/**
 * Aspect that checks if there is thunder in the world.
 * @author rubensworks
 */
public class AspectReadBooleanWorldWeatherThunder extends AspectReadBooleanWorldWeatherBase {

    @Override
    protected String getUnlocalizedWeatherType() {
        return "thunder";
    }

    @Override
    protected boolean isWeather(World world) {
        return world.isThundering();
    }
}
