package org.cyclops.integrateddynamics.part.aspect.read.world;

import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectProperties;

/**
 * Aspect that displays the world time of the target space world.
 * @author rubensworks
 */
public class AspectReadIntegerWorldTotalTime extends AspectReadIntegerWorldBase {

    @Override
    protected String getUnlocalizedIntegerWorldType() {
        return "totaltime";
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ValueTypeInteger.ValueInteger getValue(PartTarget target, AspectProperties properties) {
        return ValueTypeInteger.ValueInteger.of((int) target.getTarget().getPos().getWorld().getTotalWorldTime()); // TODO: Change datatype to long once implemented
    }
}
