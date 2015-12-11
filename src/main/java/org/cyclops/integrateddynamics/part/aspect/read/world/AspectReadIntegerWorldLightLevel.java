package org.cyclops.integrateddynamics.part.aspect.read.world;

import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;

/**
 * Aspect that takes the target light level.
 * @author rubensworks
 */
public class AspectReadIntegerWorldLightLevel extends AspectReadIntegerWorldBase {

    @Override
    protected String getUnlocalizedIntegerWorldType() {
        return "lightlevel";
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ValueTypeInteger.ValueInteger getValue(PartTarget target, IAspectProperties properties) {
        return ValueTypeInteger.ValueInteger.of(target.getTarget().getPos().getWorld().getLight(target.getTarget().getPos().getBlockPos()));
    }
}
