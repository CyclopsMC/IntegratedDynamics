package org.cyclops.integrateddynamics.core.network;

import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.api.network.*;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.path.IPathElement;

import java.util.Set;

/**
 * A network that can hold energy.
 * @author rubensworks
 */
public class EnergyNetwork extends PositionedAddonsNetwork implements IEnergyNetwork, IFullNetworkListener {

    @Getter
    @Setter
    private INetwork network;

    @Override
    public boolean addNetworkElement(INetworkElement element, boolean networkPreinit) {
        return true;
    }

    @Override
    public boolean removeNetworkElementPre(INetworkElement element) {
        return true;
    }

    @Override
    public void removeNetworkElementPost(INetworkElement element) {

    }

    @Override
    public void kill() {

    }

    @Override
    public void update() {

    }

    @Override
    public boolean removePathElement(IPathElement pathElement) {
        return true;
    }

    @Override
    public void afterServerLoad() {

    }

    @Override
    public void beforeServerStop() {

    }

    @Override
    public boolean canUpdate(INetworkElement element) {
        if(!(element instanceof IEnergyConsumingNetworkElement)) return true;
        int multiplier = GeneralConfig.energyConsumptionMultiplier;
        if(multiplier == 0) return true;
        int consumptionRate = ((IEnergyConsumingNetworkElement) element).getConsumptionRate() * multiplier;
        return extractEnergy(consumptionRate, true) == consumptionRate;
    }

    @Override
    public void onSkipUpdate(INetworkElement element) {
        if(element instanceof IEnergyConsumingNetworkElement) {
            ((IEnergyConsumingNetworkElement) element).postUpdate(getNetwork(), false);
        }
    }

    @Override
    public void postUpdate(INetworkElement element) {
        if(element instanceof IEnergyConsumingNetworkElement) {
            int multiplier = GeneralConfig.energyConsumptionMultiplier;
            if (multiplier > 0) {
                int consumptionRate = ((IEnergyConsumingNetworkElement) element).getConsumptionRate() * multiplier;
                extractEnergy(consumptionRate, false);
            }
            ((IEnergyConsumingNetworkElement) element).postUpdate(getNetwork(), true);
        }
    }

    protected int addSafe(int a, int b) {
        int add = a + b;
        if(add < a || add < b) return Integer.MAX_VALUE;
        return add;
    }

    @Override
    public int getEnergyStored() {
        int energy = 0;
        for(PrioritizedPartPos partPos : getPositionsSorted()) {
            IEnergyStorage energyStorage = getEnergyStorage(partPos);
            if (energyStorage != null) {
                energy = addSafe(energy, energyStorage.getEnergyStored());
            }
        }
        return energy;
    }

    @Override
    public int getMaxEnergyStored() {
        int maxEnergy = 0;
        for(PrioritizedPartPos partPos : getPositionsSorted()) {
            IEnergyStorage energyStorage = getEnergyStorage(partPos);
            if (energyStorage != null) {
                maxEnergy = addSafe(maxEnergy, energyStorage.getMaxEnergyStored());
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

    @Override
    public int receiveEnergy(int energy, boolean simulate) {
        int toAdd = energy;
        for(PrioritizedPartPos partPos : getPositionsSorted()) {
            IEnergyStorage energyStorage = getEnergyStorage(partPos);
            if (energyStorage != null) {
                toAdd -= energyStorage.receiveEnergy(toAdd, simulate);
            }
        }
        return energy - toAdd;
    }

    @Override
    public int extractEnergy(int energy, boolean simulate) {
        int toConsume = energy;
        for(PrioritizedPartPos partPos : getPositionsSorted()) {
            IEnergyStorage energyStorage = getEnergyStorage(partPos);
            if (energyStorage != null) {
                toConsume -= energyStorage.extractEnergy(toConsume, simulate);
            }
        }
        return energy - toConsume;
    }

    @Override
    public boolean addEnergyBattery(PartPos pos, int priority) {
        IEnergyStorage energyStorage = TileHelpers.getCapability(pos.getPos(), pos.getSide(), CapabilityEnergy.ENERGY);
        return energyStorage != null && addPosition(pos, priority);
    }

    @Override
    public void removeEnergyBattery(PartPos pos) {
        removePosition(pos);
    }

    @Override
    public Set<PartPos> getEnergyBatteries() {
        return getPositions();
    }

    @Override
    public int getConsumptionRate() {
        int multiplier = GeneralConfig.energyConsumptionMultiplier;
        if(multiplier == 0) return 0;
        int consumption = 0;
        for(INetworkElement element : getNetwork().getElements()) {
            consumption += ((IEnergyConsumingNetworkElement) element).getConsumptionRate() * multiplier;
        }
        return consumption;
    }

    protected static IEnergyStorage getEnergyStorage(PrioritizedPartPos pos) {
        return TileHelpers.getCapability(pos.getPartPos().getPos(), pos.getPartPos().getSide(), CapabilityEnergy.ENERGY);
    }
}
