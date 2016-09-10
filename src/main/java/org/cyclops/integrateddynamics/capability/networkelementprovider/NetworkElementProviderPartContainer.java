package org.cyclops.integrateddynamics.capability.networkelementprovider;

import com.google.common.collect.Sets;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.INetworkElementProvider;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Network element provider for {@link IPartContainer}.
 * @author rubensworks
 */
public class NetworkElementProviderPartContainer implements INetworkElementProvider<IPartNetwork> {

    private final IPartContainer partContainer;

    public NetworkElementProviderPartContainer(IPartContainer partContainer) {
        this.partContainer = partContainer;
    }

    @Override
    public Collection<INetworkElement<IPartNetwork>> createNetworkElements(World world, BlockPos blockPos) {
        Set<INetworkElement<IPartNetwork>> sidedElements = Sets.newHashSet();
        for(Map.Entry<EnumFacing, IPartType<?, ?>> entry : partContainer.getParts().entrySet()) {
            sidedElements.add(entry.getValue().createNetworkElement(partContainer, DimPos.of(world, blockPos), entry.getKey()));
        }
        return sidedElements;
    }
}
