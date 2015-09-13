package org.cyclops.integrateddynamics.part.aspect.read.world;

import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectProperties;

/**
 * Aspect that checks if it is night in the target world.
 * @author rubensworks
 */
public class AspectReadBooleanWorldIsNight extends AspectReadBooleanWorldBase {

    @Override
    protected String getUnlocalizedBooleanWorldType() {
        return "isnight";
    }

    @Override
    protected ValueTypeBoolean.ValueBoolean getValue(PartTarget target, AspectProperties properties) {
        return ValueTypeBoolean.ValueBoolean.of(!MinecraftHelpers.isDay(target.getTarget().getPos().getWorld()));
    }
}
