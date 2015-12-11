package org.cyclops.integrateddynamics.part.aspect.read.world;

import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;

/**
 * Aspect that displays the time for the current day.
 * @author rubensworks
 */
public class AspectReadIntegerWorldDayTime extends AspectReadIntegerWorldBase {

    @Override
    protected String getUnlocalizedIntegerWorldType() {
        return "daytime";
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ValueTypeInteger.ValueInteger getValue(PartTarget target, IAspectProperties properties) {
        return ValueTypeInteger.ValueInteger.of((int) target.getTarget().getPos().getWorld().getWorldTime() % MinecraftHelpers.MINECRAFT_DAY);
    }
}
