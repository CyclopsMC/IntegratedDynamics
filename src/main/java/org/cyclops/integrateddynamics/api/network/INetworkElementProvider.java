package org.cyclops.integrateddynamics.api.network;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;

/**
 * Classes that can create instances of an {@link INetworkElement}.
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
