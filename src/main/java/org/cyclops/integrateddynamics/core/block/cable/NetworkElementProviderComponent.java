package org.cyclops.integrateddynamics.core.block.cable;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.cyclops.integrateddynamics.core.network.INetworkElement;
import org.cyclops.integrateddynamics.core.network.INetworkElementProvider;
import org.cyclops.integrateddynamics.core.network.Network;

import java.util.List;

/**
 * Component for helping {@link INetworkElementProvider} instances.
 * @author rubensworks
 */
public class NetworkElementProviderComponent {

    private final INetworkElementProvider networkElementProvider;

    public NetworkElementProviderComponent(INetworkElementProvider networkElementProvider) {
        this.networkElementProvider = networkElementProvider;
    }

    /**
     * Called before this block is destroyed.
     * @param network The network
     * @param world The world.
     * @param pos The position.
     */
    public void onPreBlockDestroyed(Network network, World world, BlockPos pos) {
        // Drop all parts types as item.
        if(!world.isRemote) {
            List<ItemStack> itemStacks = Lists.newLinkedList();
            for (INetworkElement networkElement : networkElementProvider.createNetworkElements(world, pos)) {
                networkElement.addDrops(itemStacks);
                networkElement.onPreRemoved(network);
            }
            for (ItemStack itemStack : itemStacks) {
                Block.spawnAsEntity(world, pos, itemStack);
            }
        }
    }

}
