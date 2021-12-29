package org.cyclops.integrateddynamics.capability.path;

import net.minecraft.core.Direction;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.path.IPathElement;
import org.cyclops.integrateddynamics.api.path.ISidedPathElement;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityMultipartTicking;

import java.util.Set;

/**
 * Implementation of {@link IPathElement} for {@link BlockEntityMultipartTicking}.
 * @author rubensworks
 */
public class PathElementTileMultipartTicking extends PathElementTile<BlockEntityMultipartTicking> {

    public PathElementTileMultipartTicking(BlockEntityMultipartTicking tile, ICable cable) {
        super(tile, cable);
    }

    @Override
    public Set<ISidedPathElement> getReachableElements() {
        // Add the reachable path elements from the parts that provide one.
        Set<ISidedPathElement> pathElements = super.getReachableElements();
        for (Direction side : Direction.values()) {
            getTile().getPartContainer().getCapability(PathElementConfig.CAPABILITY, side)
                    .ifPresent(pathElement -> pathElements.addAll(pathElement.getReachableElements()));
        }
        return pathElements;
    }
}
