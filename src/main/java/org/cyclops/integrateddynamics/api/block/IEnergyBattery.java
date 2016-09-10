package org.cyclops.integrateddynamics.api.block;

/**
 * Capability containers that can hold energy.
 * @author rubensworks
 */
public interface IEnergyBattery {

    /**
     * @return The currently stored energy.
     */
    public int getStoredEnergy();

    /**
     * @return The maximum amount of energy that can be stored.
     */
    public int getMaxStoredEnergy();

    /**
     * Add the given energy amount to the battery.
     * @param energy The energy amount to add.
     * @param simulate If the addition should be stimulated.
     * @return The amount of energy that was added.
     */
    public int addEnergy(int energy, boolean simulate);

    /**
     * Remove the given energy amount from the battery.
     * @param energy The energy amount to remove.
     * @param simulate If the consumption should be stimulated.
     * @return The amount of energy that was consumed.
     */
    public int consume(int energy, boolean simulate);

}
