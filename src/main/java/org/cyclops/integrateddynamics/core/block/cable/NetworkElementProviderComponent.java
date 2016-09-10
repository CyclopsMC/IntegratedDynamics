package org.cyclops.integrateddynamics.core.block.cable;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.INetworkElementProvider;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderConfig;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Component for helping {@link INetworkElementProvider} instances.
 * @author rubensworks
 */
public class NetworkElementProviderComponent<N extends INetwork> {

    @SuppressWarnings("unchecked")
    protected INetworkElementProvider<N> getNetworkElementProvider(World world, BlockPos pos) {
        return (INetworkElementProvider<N>) TileHelpers.getCapability(world, pos, null, NetworkElementProviderConfig.CAPABILITY);
    }

    /**
     * Called before this block is destroyed.
     * @param network The network. Null if this element is part of a corrupted network, should not happen though.
     * @param world The world.
     * @param pos The position.
     * @param dropMainElement If the main part element should be dropped.
     */
    public void onPreBlockDestroyed(@Nullable N network, World world, BlockPos pos, boolean dropMainElement) {
        // Drop all parts types as item.
        if(!world.isRemote) {
            List<ItemStack> itemStacks = Lists.newLinkedList();
            INetworkElementProvider<N> networkElementProvider = getNetworkElementProvider(world, pos);
            for (INetworkElement<N> networkElement : networkElementProvider.createNetworkElements(world, pos)) {
                networkElement.addDrops(itemStacks, dropMainElement);
                if (network != null) {
                    networkElement.onPreRemoved(network);
                    network.removeNetworkElementPre(networkElement);
                    network.removeNetworkElementPost(networkElement);
                    networkElement.onPostRemoved(network);
                }
            }
            for (ItemStack itemStack : itemStacks) {
                Block.spawnAsEntity(world, pos, itemStack);
            }
        }
    }

    /**
     * Called when a neighbouring block is updated, more specifically when
     * {@link net.minecraft.block.Block#neighborChanged(IBlockState, World, BlockPos, Block)} is called.
     * @param network The network to update in.
     * @param world The world in which the neighbour was updated.
     * @param pos The position of the center block.
     * @param neighborBlock The block type of the neighbour that was updated.
     */
    public void onBlockNeighborChange(@Nullable N network, World world, BlockPos pos, Block neighborBlock) {
        if (!world.isRemote) {
            INetworkElementProvider<N> networkElementProvider = getNetworkElementProvider(world, pos);
            for (INetworkElement<N> networkElement : networkElementProvider.createNetworkElements(world, pos)) {
                networkElement.onNeighborBlockChange(network, world, neighborBlock);
            }
        }
    }

}
