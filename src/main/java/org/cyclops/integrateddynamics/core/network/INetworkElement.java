package org.cyclops.integrateddynamics.core.network;

import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * Objects that can be an element of a {@link org.cyclops.integrateddynamics.core.network.Network}.
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
     */
    public void update();

    /**
     * Called right before the network is terminated or will be reset.
     */
    public void beforeNetworkKill();

    /**
     * Called right after this network is initialized.
     */
    public void afterNetworkAlive();

    /**
     * Add the itemstacks to drop when this element is removed.
     * @param itemStacks The itemstack list to add to.
     */
    public void addDrops(List<ItemStack> itemStacks);

}
