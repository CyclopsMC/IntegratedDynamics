package org.cyclops.integrateddynamics.capability.networkelementprovider;

import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.INetworkElementProvider;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Network element provider for {@link IPartContainer}.
 * @author rubensworks
 */
public class NetworkElementProviderPartContainer implements INetworkElementProvider {

    private final IPartContainer partContainer;

    public NetworkElementProviderPartContainer(IPartContainer partContainer) {
        this.partContainer = partContainer;
    }

    @Override
    public Collection<INetworkElement> createNetworkElements(Level world, BlockPos blockPos) {
        Set<INetworkElement> sidedElements = Sets.newHashSet();
        for(Map.Entry<Direction, IPartType<?, ?>> entry : partContainer.getParts().entrySet()) {
            sidedElements.add(entry.getValue().createNetworkElement(partContainer, DimPos.of(world, blockPos), entry.getKey()));
        }
        return sidedElements;
    }
}
