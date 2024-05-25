package org.cyclops.integrateddynamics.capability.path;

import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.path.IPathElement;
import org.cyclops.integrateddynamics.api.path.ISidedPathElement;

import java.util.Set;

/**
 * Implementation of {@link IPathElement} for cables.
 * @author rubensworks
 */
public abstract class PathElementCable extends PathElementDefault {

    protected abstract ICable getCable();

    @Override
    public Set<ISidedPathElement> getReachableElements() {
        Set<ISidedPathElement> elements = Sets.newHashSet();
        BlockPos pos = getPosition().getBlockPos();
        for (Direction side : Direction.values()) {
            if (getCable().isConnected(side)) {
                BlockPos posOffset = pos.relative(side);
                Direction pathElementSide = side.getOpposite();
                IPathElement pathElement = BlockEntityHelpers.getCapability(getPosition().getLevel(true), posOffset, pathElementSide, Capabilities.PathElement.BLOCK).orElse(null);
                if (pathElement == null) {
                    IntegratedDynamics.clog(org.apache.logging.log4j.Level.ERROR, String.format("The position at %s was incorrectly marked " +
                            "as reachable as path element by %s at %s side %s.", posOffset, getCable(), pos, side));
                } else {
                    elements.add(SidedPathElement.of(pathElement, pathElementSide));
                }
            }
        }
        return elements;
    }
}
