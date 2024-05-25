package org.cyclops.integrateddynamics.capability.energystorage;

import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * An energy storage with a mutable energy level.
 * @author rubensworks
 */
public interface IEnergyStorageMutable extends IEnergyStorage {

    public void setEnergy(int energy);

}
