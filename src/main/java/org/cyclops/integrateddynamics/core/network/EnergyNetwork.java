package org.cyclops.integrateddynamics.core.network;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.api.block.IEnergyBattery;
import org.cyclops.integrateddynamics.api.network.*;
import org.cyclops.integrateddynamics.capability.energybattery.EnergyBatteryConfig;

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
    private Set<DimPos> energyBatteryPositions = Sets.newHashSet();

    @Override
    public boolean canUpdate(INetworkElement element) {
        if(!(element instanceof IEnergyConsumingNetworkElement)) return true;
        int multiplier = GeneralConfig.energyConsumptionMultiplier;
        if(multiplier == 0) return true;
        int consumptionRate = ((IEnergyConsumingNetworkElement) element).getConsumptionRate() * multiplier;
        return consume(consumptionRate, true) == consumptionRate;
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
                consume(consumptionRate, false);
            }
            ((IEnergyConsumingNetworkElement) element).postUpdate(getNetwork(), true);
        }
    }

    protected synchronized List<IEnergyBattery> getMaterializedEnergyBatteries() {
        return ImmutableList.copyOf(Iterables.transform(energyBatteryPositions, new Function<DimPos, IEnergyBattery>() {
            @Nullable
            @Override
            public IEnergyBattery apply(DimPos dimPos) {
                return TileHelpers.getCapability(dimPos, null, EnergyBatteryConfig.CAPABILITY);
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
    public synchronized int getStoredEnergy() {
        int energy = 0;
        for(IEnergyBattery energyBattery : getMaterializedEnergyBatteries()) {
            energy = addSafe(energy, energyBattery.getStoredEnergy());
        }
        return energy;
    }

    @Override
    public synchronized int getMaxStoredEnergy() {
        int maxEnergy = 0;
        for(IEnergyBattery energyBattery : getMaterializedEnergyBatteries()) {
            maxEnergy = addSafe(maxEnergy, energyBattery.getMaxStoredEnergy());
        }
        return maxEnergy;
    }

    @Override
    public int addEnergy(int energy, boolean simulate) {
        int toAdd = energy;
        for(IEnergyBattery energyBattery : getMaterializedEnergyBatteries()) {
            int maxAdd = Math.min(energyBattery.getMaxStoredEnergy() - energyBattery.getStoredEnergy(), toAdd);
            if(maxAdd > 0) {
                energyBattery.addEnergy(maxAdd, simulate);
            }
            toAdd -= maxAdd;
        }
        return energy - toAdd;
    }

    @Override
    public synchronized int consume(int energy, boolean simulate) {
        int toConsume = energy;
        for(IEnergyBattery energyBattery : getMaterializedEnergyBatteries()) {
            int consume = Math.min(energyBattery.getStoredEnergy(), toConsume);
            if(consume > 0) {
                toConsume -= energyBattery.consume(consume, simulate);
            }
        }
        return energy - toConsume;
    }

    @Override
    public boolean addEnergyBattery(DimPos dimPos) {
        IEnergyBattery energyBattery = TileHelpers.getCapability(dimPos, null, EnergyBatteryConfig.CAPABILITY);
        if(energyBattery != null) {
            boolean contained = energyBatteryPositions.contains(dimPos);
            energyBatteryPositions.add(dimPos);
            return !contained;
        }
        return false;
    }

    @Override
    public void removeEnergyBattery(DimPos pos) {
        energyBatteryPositions.remove(pos);
    }

    @Override
    public Set<DimPos> getEnergyBatteries() {
        return Collections.unmodifiableSet(energyBatteryPositions);
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
