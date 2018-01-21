package org.cyclops.integrateddynamics.api.network;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;

/**
 * Capability that can create instances of an {@link INetworkElement}.
 * Blocks that provide this capability MUST properly call
 * {@link org.cyclops.integrateddynamics.core.helper.NetworkHelpers#onElementProviderBlockNeighborChange(World, BlockPos, Block, net.minecraft.util.EnumFacing)}.
 * @author rubensworks
 */
public interface INetworkElementProvider {

    /**
     * Create network element instances for the given position.
     * @param world The world.
     * @param blockPos The position.
     * @return A collection of all network elements at this position.
     */
    public Collection<INetworkElement> createNetworkElements(World world, BlockPos blockPos);

}
