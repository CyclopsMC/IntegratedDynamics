package org.cyclops.integrateddynamics.capability.path;

import net.minecraft.util.EnumFacing;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.path.IPathElement;
import org.cyclops.integrateddynamics.api.path.ISidedPathElement;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;

import java.util.Set;

/**
 * Implementation of {@link IPathElement} for {@link TileMultipartTicking}.
 * @author rubensworks
 */
public class PathElementTileMultipartTicking extends PathElementTile<TileMultipartTicking> {

    public PathElementTileMultipartTicking(TileMultipartTicking tile, ICable cable) {
        super(tile, cable);
    }

    @Override
    public Set<ISidedPathElement> getReachableElements() {
        // Add the reachable path elements from the parts that provide one.
        Set<ISidedPathElement> pathElements = super.getReachableElements();
        for (EnumFacing side : EnumFacing.VALUES) {
            if (getTile().getPartContainer().hasCapability(PathElementConfig.CAPABILITY, side)) {
                pathElements.addAll(getTile().getPartContainer()
                        .getCapability(PathElementConfig.CAPABILITY, side).getReachableElements());
            }
        }
        return pathElements;
    }
}
