package org.cyclops.integrateddynamics.capability.energystorage;

import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * An energy storage with a mutable capacity.
 * @author rubensworks
 */
public interface IEnergyStorageCapacity extends IEnergyStorage {

    public void setCapacity(int capacity);

}
