package org.cyclops.integrateddynamics.part.aspect.read.world;

import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;

/**
 * Aspect that checks if it is day in the target world.
 * @author rubensworks
 */
public class AspectReadBooleanWorldIsDay extends AspectReadBooleanWorldBase {

    @Override
    protected String getUnlocalizedBooleanWorldType() {
        return "isday";
    }

    @Override
    protected ValueTypeBoolean.ValueBoolean getValue(PartTarget target, IAspectProperties properties) {
        return ValueTypeBoolean.ValueBoolean.of(MinecraftHelpers.isDay(target.getTarget().getPos().getWorld()));
    }
}
