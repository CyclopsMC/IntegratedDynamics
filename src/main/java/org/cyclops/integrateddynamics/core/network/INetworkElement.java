package org.cyclops.integrateddynamics.core.network;

import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * Objects that can be an element of a {@link org.cyclops.integrateddynamics.core.network.Network}.
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
    public void update(Network network);

    /**
     * Called right before the network is terminated or will be reset.
     * @param network The network to update in.
     */
    public void beforeNetworkKill(Network network);

    /**
     * Called right after this network is initialized.
     * @param network The network to update in.
     */
    public void afterNetworkAlive(Network network);

    /**
     * Add the itemstacks to drop when this element is removed.
     * @param itemStacks The itemstack list to add to.
     */
    public void addDrops(List<ItemStack> itemStacks);

    /**
     * Called when this element is added to the network.
     * @param network The network.
     * @return If the addition succeeded.
     */
    public boolean onNetworkAddition(Network network);

    /**
     * Called when this element is removed from the network.
     * @param network The network.
     */
    public void onNetworkRemoval(Network network);

    /**
     * Called when this element should refresh its state.
     * @param network The network.
     */
    public void refresh(Network network);

}
