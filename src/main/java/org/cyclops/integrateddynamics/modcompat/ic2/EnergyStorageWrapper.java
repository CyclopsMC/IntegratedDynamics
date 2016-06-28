package org.cyclops.integrateddynamics.modcompat.ic2;

import ic2.api.tile.IEnergyStorage;

/**
 * @author rubensworks
 */
public class EnergyStorageWrapper implements IEnergyWrapper {

    private final IEnergyStorage energyStorage;

    public EnergyStorageWrapper(IEnergyStorage energyStorage) {
        this.energyStorage = energyStorage;
    }

    @Override
    public int getStored() {
        return energyStorage.getStored();
    }

    @Override
    public int getCapacity() {
        return energyStorage.getCapacity();
    }
}
