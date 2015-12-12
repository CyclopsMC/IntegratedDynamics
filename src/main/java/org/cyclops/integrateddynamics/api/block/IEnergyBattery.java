package org.cyclops.integrateddynamics.api.block;

import org.cyclops.cyclopscore.datastructure.DimPos;

/**
 * An interface for containers that can hold energy.
 * @author rubensworks
 */
public interface IEnergyBattery {

    /**
     * @return The position this container is at.
     */
    public DimPos getPosition();

    /**
     * @return The currently stored energy.
     */
    public int getStoredEnergy();

    /**
     * @return The maximum amount of energy that can be stored.
     */
    public int getMaxStoredEnergy();

    /**
     * Add the given energy amount to the network.
     * @param energy The energy amount to add.
     */
    public void addEnergy(int energy);

    /**
     * Remove the given energy amount from the network.
     * @param energy The energy amount to remove.
     * @param simulate If the consumption should be stimulated.
     * @return The amount of energy that was consumed.
     */
    public int consume(int energy, boolean simulate);

}
