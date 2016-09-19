package org.cyclops.integrateddynamics.core.network;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.api.network.*;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A network that can hold energy.
 * @author rubensworks
 */
public class EnergyNetwork extends FullNetworkListenerAdapter implements IEnergyNetwork {

    @Getter
    @Setter
    private INetwork network;
    private Set<DimPos> energyStoragePositions = Sets.newHashSet();

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

    protected synchronized List<IEnergyStorage> getMaterializedEnergyBatteries() {
        return ImmutableList.copyOf(Iterables.transform(energyStoragePositions, new Function<DimPos, IEnergyStorage>() {
            @Nullable
            @Override
            public IEnergyStorage apply(DimPos dimPos) {
                return TileHelpers.getCapability(dimPos, null, CapabilityEnergy.ENERGY);
            }

            @Override
            public boolean equals(@Nullable Object object) {
                return false;
            }
        }));
    }

    protected int addSafe(int a, int b) {
        int add = a + b;
        if(add < a || add < b) return Integer.MAX_VALUE;
        return add;
    }

    @Override
    public synchronized int getEnergyStored() {
        int energy = 0;
        for(IEnergyStorage energyStorage : getMaterializedEnergyBatteries()) {
            energy = addSafe(energy, energyStorage.getEnergyStored());
        }
        return energy;
    }

    @Override
    public synchronized int getMaxEnergyStored() {
        int maxEnergy = 0;
        for(IEnergyStorage energyStorage : getMaterializedEnergyBatteries()) {
            maxEnergy = addSafe(maxEnergy, energyStorage.getMaxEnergyStored());
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
        for(IEnergyStorage energyStorage : getMaterializedEnergyBatteries()) {
            int maxAdd = Math.min(energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored(), toAdd);
            if(maxAdd > 0) {
                energyStorage.receiveEnergy(maxAdd, simulate);
            }
            toAdd -= maxAdd;
        }
        return energy - toAdd;
    }

    @Override
    public synchronized int extractEnergy(int energy, boolean simulate) {
        int toConsume = energy;
        for(IEnergyStorage energyStorage : getMaterializedEnergyBatteries()) {
            int consume = Math.min(energyStorage.getEnergyStored(), toConsume);
            if(consume > 0) {
                toConsume -= energyStorage.extractEnergy(consume, simulate);
            }
        }
        return energy - toConsume;
    }

    @Override
    public boolean addEnergyBattery(DimPos dimPos) {
        IEnergyStorage energyStorage = TileHelpers.getCapability(dimPos, null, CapabilityEnergy.ENERGY);
        if(energyStorage != null) {
            boolean contained = energyStoragePositions.contains(dimPos);
            energyStoragePositions.add(dimPos);
            return !contained;
        }
        return false;
    }

    @Override
    public void removeEnergyBattery(DimPos pos) {
        energyStoragePositions.remove(pos);
    }

    @Override
    public Set<DimPos> getEnergyBatteries() {
        return Collections.unmodifiableSet(energyStoragePositions);
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
}
