package org.cyclops.integrateddynamics.part.aspect.read.minecraft;

import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.part.aspect.read.AspectReadIntegerBase;

/**
 * Base class for integer Minecraft aspects.
 * @author rubensworks
 */
public abstract class AspectReadIntegerMinecraftBase extends AspectReadIntegerBase {

    @Override
    protected String getUnlocalizedIntegerType() {
        return "minecraft." + getUnlocalizedIntegerMinecraftType();
    }

    protected abstract String getUnlocalizedIntegerMinecraftType();

    @Override
    protected ValueTypeInteger.ValueInteger getValue(PartTarget target, IAspectProperties properties) {
        return ValueTypeInteger.ValueInteger.of(getValue(target));
    }

    protected abstract int getValue(PartTarget target);

}
