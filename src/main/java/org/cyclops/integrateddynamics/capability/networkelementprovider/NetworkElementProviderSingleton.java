package org.cyclops.integrateddynamics.capability.networkelementprovider;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.INetworkElementProvider;

import java.util.Collection;
import java.util.Collections;

/**
 * A network element provider for a single element.
 * @author rubensworks
 */
public abstract class NetworkElementProviderSingleton implements INetworkElementProvider {
    @Override
    public Collection<INetworkElement> createNetworkElements(Level world, BlockPos blockPos) {
        return Collections.singleton(createNetworkElement(world, blockPos));
    }

    public abstract INetworkElement createNetworkElement(Level world, BlockPos blockPos);
}
