package org.cyclops.integrateddynamics.part.aspect.read.redstone;

import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;

/**
 * Aspect that can retrieve the comparator value.
 * @author rubensworks
 */
public class AspectReadIntegerRedstoneComparator extends AspectReadIntegerRedstoneBase {

    @Override
    protected String getUnlocalizedIntegerRedstoneType() {
        return "comparator";
    }

    @Override
    protected int getValue(PartTarget target) {
        DimPos dimPos = target.getTarget().getPos();
        return dimPos.getWorld().getBlockState(dimPos.getBlockPos()).getBlock().getComparatorInputOverride(dimPos.getWorld(), dimPos.getBlockPos());
    }

}
