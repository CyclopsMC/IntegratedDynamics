package org.cyclops.integrateddynamics.part.aspect.read.world;

import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeLong;

/**
 * Aspect that displays the world time of the target space world.
 * @author rubensworks
 */
public class AspectReadLongWorldTime extends AspectReadLongWorldBase {

    @Override
    protected String getUnlocalizedLongWorldType() {
        return "time";
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ValueTypeLong.ValueLong getValue(PartTarget target, IAspectProperties properties) {
        return ValueTypeLong.ValueLong.of(target.getTarget().getPos().getWorld().getWorldTime());
    }
}
