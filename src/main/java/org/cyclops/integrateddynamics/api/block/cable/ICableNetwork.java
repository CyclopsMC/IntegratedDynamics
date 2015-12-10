package org.cyclops.integrateddynamics.api.block.cable;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.cyclops.integrateddynamics.api.network.INetworkCarrier;
import org.cyclops.integrateddynamics.api.path.IPathElement;

/**
 * Interface for cables that are network-aware.
 * @author rubensworks
 */
public interface ICableNetwork<E extends IPathElement<E>> extends ICable<E>, INetworkCarrier {

    /**
     * (Re-)initialize the network at the given position.
     * @param world The world.
     * @param pos The position of this block.
     */
    public void initNetwork(World world, BlockPos pos);

}
