package org.cyclops.integrateddynamics.capability.energystorage;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    public void serialize(ValueOutput output) {
        super.serialize(output);
        output.putInt("capacity", capacity);
    }

    @Override
    public void deserialize(ValueInput input) {
        super.deserialize(input);
        capacity = Math.max(0, input.getIntOr("capacity", capacity));
    }

    @Override
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}
