package org.cyclops.integrateddynamics.part.aspect.read.world;

import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectProperties;

/**
 * Aspect that displays the target X position.
 * @author rubensworks
 */
public class AspectReadIntegerWorldPosX extends AspectReadIntegerWorldBase {

    @Override
    protected String getUnlocalizedIntegerWorldType() {
        return "posx";
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ValueTypeInteger.ValueInteger getValue(PartTarget target, AspectProperties properties) {
        return ValueTypeInteger.ValueInteger.of(target.getTarget().getPos().getBlockPos().getX());
    }
}
