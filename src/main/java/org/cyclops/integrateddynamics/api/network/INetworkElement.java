package org.cyclops.integrateddynamics.api.network;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Objects that can be an element of a {@link INetwork}.
 * Multiple instances for the same 'element' can be created, so the comparator implementation must
 * make sure that these instances are considered equal.
 * These instances are used as a simple way of referring to these elements.
 * @author rubensworks
 */
public interface INetworkElement extends Comparable<INetworkElement> {

    /**
     * @return The tick interval to update this element.
     */
    public int getUpdateInterval();

    /**
     * @return If this element should be updated. This method is only called once during network initialization.
     */
    public boolean isUpdate();

    /**
     * Update at the tick interval specified.
     * @param network The network to update in.
     */
    public void update(INetwork network);

    /**
     * Called right before the network is terminated or will be reset.
     * @param network The network to update in.
     */
    public void beforeNetworkKill(INetwork network);

    /**
     * Called right after this network is initialized.
     * @param network The network to update in.
     */
    public void afterNetworkAlive(INetwork network);

    /**
     * Called right after this network has come alive again,
     * for example after a network restart.
     * @param network The network to update in.
     */
    public void afterNetworkReAlive(INetwork network);

    /**
     * Add the itemstacks to drop when this element is removed.
     * @param itemStacks The itemstack list to add to.
     * @param dropMainElement If the part itself should also be dropped.
     */
    public void addDrops(List<ItemStack> itemStacks, boolean dropMainElement);

    /**
     * Called when this element is added to the network.
     * @param network The network.
     * @return If the addition succeeded.
     */
    public boolean onNetworkAddition(INetwork network);

    /**
     * Called when this element is removed from the network.
     * @param network The network.
     */
    public void onNetworkRemoval(INetwork network);

    /**
     * Called when this element is about to be removed.
     * This is called before {@link INetwork#removeNetworkElementPre(INetworkElement)}.
     * @param network The network.
     */
    public void onPreRemoved(INetwork network);

    /**
     * Called when this element has been removed.
     * This is called after {@link INetwork#removeNetworkElementPost(INetworkElement)}.
     * @param network The network.
     */
    public void onPostRemoved(INetwork network);

    /**
     * Called when a neighbouring block is updated, more specifically when
     * {@link net.minecraft.block.Block#neighborChanged(IBlockState, World, BlockPos, Block, BlockPos)} is called.
     * @param network The network to update in.
     * @param world The world in which the neighbour was updated.
     * @param neighborBlock block type of the neighbour that was updated.
     */
    public void onNeighborBlockChange(@Nullable INetwork network, IBlockAccess world, Block neighborBlock);

    /**
     * Set the priority and channel of this element in the network.
     * @deprecated Should only be called from {@link INetwork#setPriorityAndChannel(INetworkElement, int, int)}!
     * @param network The network this element is present in.
     * @param priority The new priority
     * @param channel The new channel
     */
    @Deprecated
    public void setPriorityAndChannel(INetwork network, int priority, int channel);

    /**
     * @return The priority of this element in the network.
     */
    public int getPriority();

    /**
     * @return The channel of this element in the network.
     */
    public int getChannel();

    /**
     * Invalidate this network element.
     * @param network The network.
     */
    public void invalidate(INetwork network);
    /**
     * Check if this element can be revalidated if it has been invalidated.
     * @param network The network.
     * @return If it can be revalidated.
     */
    public boolean canRevalidate(INetwork network);
    /**
     * Revalidate this network element after it has been invalidated.
     * @param network The network.
     */
    public void revalidate(INetwork network);

}
