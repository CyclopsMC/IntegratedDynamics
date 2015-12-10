package org.cyclops.integrateddynamics.part.aspect.read.world;

import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectProperties;

/**
 * Aspect that displays the target Y position.
 * @author rubensworks
 */
public class AspectReadIntegerWorldPosY extends AspectReadIntegerWorldBase {

    @Override
    protected String getUnlocalizedIntegerWorldType() {
        return "posy";
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ValueTypeInteger.ValueInteger getValue(PartTarget target, AspectProperties properties) {
        return ValueTypeInteger.ValueInteger.of(target.getTarget().getPos().getBlockPos().getY());
    }
}
