package org.cyclops.integrateddynamics.capability.energystorage;

import net.neoforged.neoforge.transfer.energy.EnergyHandler;

/**
 * An energy storage with a mutable capacity.
 * @author rubensworks
 */
public interface IEnergyStorageCapacity extends EnergyHandler {

    public void setCapacity(int capacity);

}
