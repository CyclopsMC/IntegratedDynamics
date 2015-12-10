package org.cyclops.integrateddynamics.part.aspect.read.world;

import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectProperties;

/**
 * Aspect that gets the amount of ticks until rain.
 * @author rubensworks
 */
public class AspectReadIntegerWorldRainCountdown extends AspectReadIntegerWorldBase {

    @Override
    protected String getUnlocalizedIntegerWorldType() {
        return "raincountdown";
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ValueTypeInteger.ValueInteger getValue(PartTarget target, AspectProperties properties) {
        return ValueTypeInteger.ValueInteger.of(target.getTarget().getPos().getWorld().getWorldInfo().getRainTime());
    }
}
