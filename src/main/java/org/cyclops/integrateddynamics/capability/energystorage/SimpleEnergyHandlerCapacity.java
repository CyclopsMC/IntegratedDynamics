package org.cyclops.integrateddynamics.capability.energystorage;

import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;

/**
 * @author rubensworks
 */
public class SimpleEnergyHandlerCapacity extends SimpleEnergyHandler implements IEnergyStorageCapacity {
    public SimpleEnergyHandlerCapacity(int capacity) {
        super(capacity);
    }

    public SimpleEnergyHandlerCapacity(int capacity, int maxTransfer) {
        super(capacity, maxTransfer);
    }

    public SimpleEnergyHandlerCapacity(int capacity, int maxInsert, int maxExtract) {
        super(capacity, maxInsert, maxExtract);
    }

    public SimpleEnergyHandlerCapacity(int capacity, int maxInsert, int maxExtract, int energy) {
        super(capacity, maxInsert, maxExtract, energy);
    }

    @Override
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}
