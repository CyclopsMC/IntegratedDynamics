package org.cyclops.integrateddynamics.capability.networkelementprovider;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.INetworkElementProvider;

import java.util.Collection;
import java.util.Collections;

/**
 * An dummy network element provider implementation.
 * @author rubensworks
 */
public class NetworkElementProviderEmpty implements INetworkElementProvider {
    @Override
    public Collection<INetworkElement> createNetworkElements(World world, BlockPos blockPos) {
        return Collections.emptyList();
    }
}
