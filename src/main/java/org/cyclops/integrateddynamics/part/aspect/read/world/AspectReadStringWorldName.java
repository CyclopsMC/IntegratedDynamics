package org.cyclops.integrateddynamics.part.aspect.read.world;

import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeString;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectProperties;

/**
 * Aspect that takes the target world name.
 * @author rubensworks
 */
public class AspectReadStringWorldName extends AspectReadStringWorldBase {

    @Override
    protected String getUnlocalizedStringWorldType() {
        return "worldname";
    }

    @Override
    protected ValueTypeString.ValueString getValue(PartTarget target, AspectProperties properties) {
        return ValueTypeString.ValueString.of(target.getTarget().getPos().getWorld().getWorldInfo().getWorldName());
    }
}
