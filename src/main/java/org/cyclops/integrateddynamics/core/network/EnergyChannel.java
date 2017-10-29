package org.cyclops.integrateddynamics.core.network;

import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork.PrioritizedPartPos;

import net.minecraftforge.energy.IEnergyStorage;

public class EnergyChannel implements IEnergyStorage {
    final EnergyNetwork network;
    final int channel;

    EnergyChannel(EnergyNetwork network, int channel) {
        this.network = network;
        this.channel = channel;
    }

    @Override
    public int receiveEnergy(int energy, boolean simulate) {
        energy = Math.min(energy, GeneralConfig.energyRateLimit);
        int toAdd = energy;
        for(PrioritizedPartPos partPos : network.getPositions()) {
            if(partPos.getChannel() != channel) continue;
            IEnergyStorage energyStorage = network.getEnergyStorage(partPos);
            if (energyStorage != null) {
                network.disablePosition(partPos.getPartPos());
                toAdd -= energyStorage.receiveEnergy(toAdd, simulate);
                network.enablePosition(partPos.getPartPos());
            }
        }
        return energy - toAdd;
    }

    @Override
    public int extractEnergy(int energy, boolean simulate) {
        energy = Math.min(energy, GeneralConfig.energyRateLimit);
        int toConsume = energy;
        for(PrioritizedPartPos partPos : network.getPositions()) {
            if(partPos.getChannel() != channel) continue;
            IEnergyStorage energyStorage = network.getEnergyStorage(partPos);
            if (energyStorage != null) {
                network.disablePosition(partPos.getPartPos());
                toConsume -= energyStorage.extractEnergy(toConsume, simulate);
                network.enablePosition(partPos.getPartPos());
            }
        }
        return energy - toConsume;
    }

    @Override
    public int getEnergyStored() {
        int energy = 0;
        for(PrioritizedPartPos partPos : network.getPositions()) {
            if(partPos.getChannel() != channel) continue;
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
        for(PrioritizedPartPos partPos : network.getPositions()) {
            if(partPos.getChannel() != channel) continue;
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
