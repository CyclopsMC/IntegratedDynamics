package org.cyclops.integrateddynamics.capability.networkelementprovider;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.INetworkElementProvider;

import java.util.Collection;
import java.util.Collections;

/**
 * A network element provider for a single element.
 * @author rubensworks
 */
public abstract class NetworkElementProviderSingleton<N extends INetwork> implements INetworkElementProvider<N> {
    @Override
    public Collection<INetworkElement<N>> createNetworkElements(World world, BlockPos blockPos) {
        return Collections.singleton(createNetworkElement(world, blockPos));
    }

    public abstract INetworkElement<N> createNetworkElement(World world, BlockPos blockPos);
}
