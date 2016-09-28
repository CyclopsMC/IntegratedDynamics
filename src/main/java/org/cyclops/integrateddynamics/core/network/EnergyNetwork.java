package org.cyclops.integrateddynamics.core.network;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.api.network.*;
import org.cyclops.integrateddynamics.api.part.PartPos;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * A network that can hold energy.
 * @author rubensworks
 */
public class EnergyNetwork extends FullNetworkListenerAdapter implements IEnergyNetwork {

    @Getter
    @Setter
    private INetwork network;
    private Set<PartPos> energyStoragePositions = Sets.newHashSet();
    private TreeSet<PrioritizedPartPos> energyStoragePositionsSorted = Sets.newTreeSet();

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

    protected synchronized Iterable<IEnergyStorage> getMaterializedEnergyBatteries() {
        return Iterables.transform(energyStoragePositionsSorted, new Function<PrioritizedPartPos, IEnergyStorage>() {
            @Nullable
            @Override
            public IEnergyStorage apply(PrioritizedPartPos pos) {
                return TileHelpers.getCapability(pos.getPartPos().getPos(), pos.getPartPos().getSide(), CapabilityEnergy.ENERGY);
            }

            @Override
            public boolean equals(@Nullable Object object) {
                return false;
            }
        });
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
    public boolean addEnergyBattery(PartPos pos, int priority) {
        IEnergyStorage energyStorage = TileHelpers.getCapability(pos.getPos(), pos.getSide(), CapabilityEnergy.ENERGY);
        if(energyStorage != null) {
            boolean contained = energyStoragePositions.contains(pos);
            if (energyStoragePositions.add(pos)) {
                energyStoragePositionsSorted.add(PrioritizedPartPos.of(pos, priority));
            }
            return !contained;
        }
        return false;
    }

    @Override
    public void removeEnergyBattery(PartPos pos) {
        energyStoragePositions.remove(pos);
        Iterator<PrioritizedPartPos> it = energyStoragePositionsSorted.iterator();
        while (it.hasNext()) {
            if (it.next().getPartPos().equals(pos)) {
                it.remove();
            }
        }
    }

    @Override
    public Set<PartPos> getEnergyBatteries() {
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

    @Data(staticConstructor = "of")
    public static class PrioritizedPartPos implements Comparable<PrioritizedPartPos> {
        private final PartPos partPos;
        private final int priority;

        @Override
        public int compareTo(PrioritizedPartPos o) {
            int compPriority = -Integer.compare(this.getPriority(), o.getPriority());
            if (compPriority == 0) {
                int compPos = this.getPartPos().getPos().compareTo(o.getPartPos().getPos());
                if (compPos == 0) {
                    EnumFacing thisSide = this.getPartPos().getSide();
                    EnumFacing otherSide = o.getPartPos().getSide();
                    return thisSide == otherSide ? 0 : (thisSide == null ? -1 : (otherSide == null ? 1 : thisSide.compareTo(otherSide)));
                }
            }
            return compPriority;
        }
    }
}
