package org.cyclops.integrateddynamics.core.network;

import net.minecraftforge.energy.IEnergyStorage;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork.PrioritizedPartPos;

public class EnergyChannel implements IEnergyStorage {
    private final EnergyNetwork network;
    private final int channel;

    public EnergyChannel(EnergyNetwork network, int channel) {
        this.network = network;
        this.channel = channel;
    }

    @Override
    public int receiveEnergy(int energy, boolean simulate) {
        energy = Math.min(energy, GeneralConfig.energyRateLimit);
        int toAdd = energy;
        IPositionedAddonsNetwork.PositionsIterator it = network.getPositionIterator(this.channel);
        while (it.hasNext() && toAdd > 0) {
            PrioritizedPartPos partPos = it.next();
            IEnergyStorage energyStorage = network.getEnergyStorage(partPos);
            if (energyStorage != null) {
                network.disablePosition(partPos.getPartPos());
                toAdd -= energyStorage.receiveEnergy(toAdd, simulate);
                network.enablePosition(partPos.getPartPos());
            }
        }
        if (!simulate) {
            network.setPositionIterator(it, this.channel);
        }
        return energy - toAdd;
    }

    @Override
    public int extractEnergy(int energy, boolean simulate) {
        energy = Math.min(energy, GeneralConfig.energyRateLimit);
        int toConsume = energy;
        IPositionedAddonsNetwork.PositionsIterator it = network.getPositionIterator(this.channel);
        while (it.hasNext() && toConsume > 0) {
            PrioritizedPartPos partPos = it.next();
            IEnergyStorage energyStorage = network.getEnergyStorage(partPos);
            if (energyStorage != null) {
                network.disablePosition(partPos.getPartPos());
                toConsume -= energyStorage.extractEnergy(toConsume, simulate);
                network.enablePosition(partPos.getPartPos());
            }
        }
        if (!simulate) {
            network.setPositionIterator(it, this.channel);
        }
        return energy - toConsume;
    }

    @Override
    public int getEnergyStored() {
        int energy = 0;
        for(PrioritizedPartPos partPos : network.getPositions(this.channel)) {
            IEnergyStorage energyStorage = network.getEnergyStorage(partPos);
            if (energyStorage != null) {
                network.disablePosition(partPos.getPartPos());
                energy = EnergyNetwork.addSafe(energy, energyStorage.getEnergyStored());
                network.enablePosition(partPos.getPartPos());
            }
        }
        return energy;
    }

    @Override
    public int getMaxEnergyStored() {
        int maxEnergy = 0;
        for(PrioritizedPartPos partPos : network.getPositions(this.channel)) {
            IEnergyStorage energyStorage = network.getEnergyStorage(partPos);
            if (energyStorage != null) {
                network.disablePosition(partPos.getPartPos());
                maxEnergy = EnergyNetwork.addSafe(maxEnergy, energyStorage.getMaxEnergyStored());
                network.enablePosition(partPos.getPartPos());
            }
        }
        return maxEnergy;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

}
