package org.cyclops.integrateddynamics.capability.energystorage;

import net.neoforged.neoforge.transfer.energy.EnergyHandler;

/**
 * An energy storage with a mutable energy level.
 * @author rubensworks
 */
public interface IEnergyStorageMutable extends EnergyHandler {

    public void setEnergy(int energy);

}
