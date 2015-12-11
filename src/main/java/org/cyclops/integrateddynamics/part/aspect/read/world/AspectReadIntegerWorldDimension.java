package org.cyclops.integrateddynamics.part.aspect.read.world;

import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;

/**
 * Aspect that displays the world dimension id.
 * @author rubensworks
 */
public class AspectReadIntegerWorldDimension extends AspectReadIntegerWorldBase {

    @Override
    protected String getUnlocalizedIntegerWorldType() {
        return "dimension";
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ValueTypeInteger.ValueInteger getValue(PartTarget target, IAspectProperties properties) {
        return ValueTypeInteger.ValueInteger.of(target.getTarget().getPos().getWorld().provider.getDimensionId());
    }
}
