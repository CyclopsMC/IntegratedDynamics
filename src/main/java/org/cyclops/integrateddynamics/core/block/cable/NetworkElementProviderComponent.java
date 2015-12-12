package org.cyclops.integrateddynamics.core.block.cable;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.INetworkElementProvider;

import java.util.List;

/**
 * Component for helping {@link INetworkElementProvider} instances.
 * @author rubensworks
 */
public class NetworkElementProviderComponent<N extends INetwork> {

    private final INetworkElementProvider<N> networkElementProvider;

    public NetworkElementProviderComponent(INetworkElementProvider<N> networkElementProvider) {
        this.networkElementProvider = networkElementProvider;
    }

    /**
     * Called before this block is destroyed.
     * @param network The network
     * @param world The world.
     * @param pos The position.
     */
    public void onPreBlockDestroyed(N network, World world, BlockPos pos) {
        // Drop all parts types as item.
        if(!world.isRemote) {
            List<ItemStack> itemStacks = Lists.newLinkedList();
            for (INetworkElement<N> networkElement : networkElementProvider.createNetworkElements(world, pos)) {
                networkElement.addDrops(itemStacks);
                networkElement.onPreRemoved(network);
            }
            for (ItemStack itemStack : itemStacks) {
                Block.spawnAsEntity(world, pos, itemStack);
            }
        }
    }

    /**
     * Called when a neighbouring block is updated, more specifically when
     * {@link net.minecraft.block.Block#onNeighborBlockChange(World, BlockPos, IBlockState, Block)} is called.
     * @param network The network to update in.
     * @param world The world in which the neighbour was updated.
     * @param pos The position of the center block.
     * @param neighborBlock The block type of the neighbour that was updated.
     */
    public void onBlockNeighborChange(N network, World world, BlockPos pos, Block neighborBlock) {
        if (!world.isRemote) {
            for (INetworkElement<N> networkElement : networkElementProvider.createNetworkElements(world, pos)) {
                networkElement.onNeighborBlockChange(network, world, neighborBlock);
            }
        }
    }

}
