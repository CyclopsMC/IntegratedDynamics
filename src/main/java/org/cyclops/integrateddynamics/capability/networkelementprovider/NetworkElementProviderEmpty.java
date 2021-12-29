package org.cyclops.integrateddynamics.capability.networkelementprovider;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
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
    public Collection<INetworkElement> createNetworkElements(Level world, BlockPos blockPos) {
        return Collections.emptyList();
    }
}
