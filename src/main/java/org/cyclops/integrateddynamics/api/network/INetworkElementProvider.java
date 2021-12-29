package org.cyclops.integrateddynamics.api.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.Collection;

/**
 * Capability that can create instances of an {@link INetworkElement}.
 * Blocks that provide this capability MUST properly call
 * {@link org.cyclops.integrateddynamics.core.helper.NetworkHelpers#onElementProviderBlockNeighborChange(Level, BlockPos, Block, Direction, BlockPos)}.
 * @author rubensworks
 */
public interface INetworkElementProvider {

    /**
     * Create network element instances for the given position.
     * @param world The world.
     * @param blockPos The position.
     * @return A collection of all network elements at this position.
     */
    public Collection<INetworkElement> createNetworkElements(Level world, BlockPos blockPos);

}
