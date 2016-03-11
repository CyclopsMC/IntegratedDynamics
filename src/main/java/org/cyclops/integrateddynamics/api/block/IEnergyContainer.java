package org.cyclops.integrateddynamics.api.block;

import net.minecraft.item.ItemStack;

/**
 * An interface for items that can hold energy.
 * @author rubensworks
 */
public interface IEnergyContainer {

    /**
     * @param itemStack The item stack carrying the energy.
     * @return The currently stored energy.
     */
    public int getStoredEnergy(ItemStack itemStack);

    /**
     * @param itemStack The item stack carrying the energy.
     * @return The maximum amount of energy that can be stored.
     */
    public int getMaxStoredEnergy(ItemStack itemStack);

    /**
     * Add the given energy amount to the item.
     * @param itemStack The item stack carrying the energy.
     * @param energy The energy amount to add.
     * @param simulate If the addition should be stimulated.
     * @return The amount of energy that was added.
     */
    public int addEnergy(ItemStack itemStack, int energy, boolean simulate);

    /**
     * Remove the given energy amount from the item.
     * @param itemStack The item stack carrying the energy.
     * @param energy The energy amount to remove.
     * @param simulate If the consumption should be stimulated.
     * @return The amount of energy that was consumed.
     */
    public int consume(ItemStack itemStack, int energy, boolean simulate);

}
