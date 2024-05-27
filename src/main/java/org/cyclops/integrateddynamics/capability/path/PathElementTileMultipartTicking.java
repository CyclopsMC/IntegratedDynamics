package org.cyclops.integrateddynamics.capability.path;

import net.minecraft.core.Direction;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.path.IPathElement;
import org.cyclops.integrateddynamics.api.path.ISidedPathElement;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityMultipartTicking;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;

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
        INetwork network = getTile().getNetwork();
        IPartNetwork partNetwork = NetworkHelpers.getPartNetwork(network).orElse(null);
        for (Direction side : Direction.values()) {
            getTile().getPartContainer().getCapability(Capabilities.PathElement.PART, network, partNetwork, PartTarget.fromCenter(PartPos.of(getTile().getLevel(), getTile().getBlockPos(), side)))
                    .ifPresent(pathElement -> pathElements.addAll(pathElement.getReachableElements()));
        }
        return pathElements;
    }
}
