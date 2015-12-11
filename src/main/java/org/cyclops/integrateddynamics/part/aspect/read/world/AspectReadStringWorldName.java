package org.cyclops.integrateddynamics.part.aspect.read.world;

import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeString;

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
    protected ValueTypeString.ValueString getValue(PartTarget target, IAspectProperties properties) {
        return ValueTypeString.ValueString.of(target.getTarget().getPos().getWorld().getWorldInfo().getWorldName());
    }
}
