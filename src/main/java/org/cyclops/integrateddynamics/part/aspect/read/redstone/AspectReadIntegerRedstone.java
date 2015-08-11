package org.cyclops.integrateddynamics.part.aspect.read.redstone;

import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.part.aspect.read.AspectReadIntegerBase;

/**
 * Aspect that can retrieve the redstone level.
 * @author rubensworks
 */
public class AspectReadIntegerRedstone extends AspectReadIntegerBase {

    @Override
    protected String getUnlocalizedIntegerType() {
        return "redstone";
    }

    @Override
    protected ValueTypeInteger.ValueInteger getValue(PartTarget target) {
        DimPos dimPos = target.getTarget().getPos();
        int value = dimPos.getWorld().getRedstonePower(dimPos.getBlockPos(), target.getCenter().getSide());
        return ValueTypeInteger.ValueInteger.of(value);
    }

}
