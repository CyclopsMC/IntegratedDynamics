package org.cyclops.integrateddynamics.part.aspect.read.world;

import net.minecraft.world.World;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectProperties;

/**
 * Aspect that checks if there is clear weather in the world.
 * @author rubensworks
 */
public abstract class AspectReadBooleanWorldWeatherBase extends AspectReadBooleanWorldBase {

    @Override
    protected String getUnlocalizedBooleanWorldType() {
        return "weather." + getUnlocalizedWeatherType();
    }

    protected abstract String getUnlocalizedWeatherType();

    protected abstract boolean isWeather(World world);

    @Override
    protected ValueTypeBoolean.ValueBoolean getValue(PartTarget target, AspectProperties properties) {
        return ValueTypeBoolean.ValueBoolean.of(isWeather(target.getTarget().getPos().getWorld()));
    }
}
