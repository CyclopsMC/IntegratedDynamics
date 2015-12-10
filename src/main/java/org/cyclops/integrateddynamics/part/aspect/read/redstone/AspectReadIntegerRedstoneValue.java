package org.cyclops.integrateddynamics.part.aspect.read.redstone;

import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;

/**
 * Aspect that can retrieve the redstone level.
 * @author rubensworks
 */
public class AspectReadIntegerRedstoneValue extends AspectReadIntegerRedstoneBase {

    @Override
    protected String getUnlocalizedIntegerRedstoneType() {
        return "value";
    }

    @Override
    protected int getValue(PartTarget target) {
        DimPos dimPos = target.getTarget().getPos();
        return dimPos.getWorld().getRedstonePower(dimPos.getBlockPos(), target.getCenter().getSide());
    }

}
