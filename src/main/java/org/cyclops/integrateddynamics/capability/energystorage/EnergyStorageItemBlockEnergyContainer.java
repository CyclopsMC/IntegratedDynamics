package org.cyclops.integrateddynamics.capability.energystorage;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.cyclops.cyclopscore.RegistryEntries;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.block.BlockEnergyBatteryBase;
import org.cyclops.integrateddynamics.block.BlockEnergyBatteryConfig;
import org.cyclops.integrateddynamics.core.item.ItemBlockEnergyContainer;

import javax.annotation.Nullable;

/**
 * Energy Battery implementation for ItemBlock's.
 * @author rubensworks
 */
public class EnergyStorageItemBlockEnergyContainer implements IEnergyStorageCapacity, IEnergyStorageMutable {

    private final ItemBlockEnergyContainer itemBlockEnergyContainer;
    private final ItemStack itemStack;
    private final ItemAccess itemAccess;
    private final int rate;
    private final Journal journal;

    public EnergyStorageItemBlockEnergyContainer(ItemBlockEnergyContainer itemBlockEnergyContainer, ItemStack itemStack, ItemAccess itemAccess, int rate) {
        this.itemBlockEnergyContainer = itemBlockEnergyContainer;
        this.itemStack = itemStack;
        this.itemAccess = itemAccess;
        this.rate = rate;
        this.journal = new Journal();

        if (!this.itemStack.has(RegistryEntries.COMPONENT_ENERGY_STORAGE)) {
            setItemStackEnergy(itemStack, 0, null);
        }
    }

    public EnergyStorageItemBlockEnergyContainer(ItemBlockEnergyContainer itemBlockEnergyContainer, ItemStack itemStack, ItemAccess itemAccess) {
        this(itemBlockEnergyContainer, itemStack, itemAccess, Integer.MAX_VALUE);
    }

    public int getRate() {
        return rate;
    }

    public boolean isCreative() {
        Block block = itemBlockEnergyContainer.get();
        return block instanceof BlockEnergyBatteryBase && ((BlockEnergyBatteryBase) block).isCreative();
    }

    protected int getEnergyStoredSingular() {
        if(isCreative()) return Integer.MAX_VALUE;
        return itemStack.get(RegistryEntries.COMPONENT_ENERGY_STORAGE);
    }

    @Override
    public int getAmountAsInt() {
        return IModHelpers.get().getBaseHelpers().multiplySafe(getEnergyStoredSingular(), this.itemStack.getCount());
    }

    @Override
    public long getAmountAsLong() {
        return ((long) getEnergyStoredSingular()) * this.itemStack.getCount();
    }

    public int getMaxEnergyStoredSingular() {
        if(isCreative()) return Integer.MAX_VALUE;
        if (!itemStack.has(RegistryEntries.COMPONENT_CAPACITY)) {
            return BlockEnergyBatteryConfig.capacity;
        }
        return itemStack.get(RegistryEntries.COMPONENT_CAPACITY);
    }

    @Override
    public int getCapacityAsInt() {
        return IModHelpers.get().getBaseHelpers().multiplySafe(getMaxEnergyStoredSingular(), this.itemStack.getCount());
    }

    @Override
    public long getCapacityAsLong() {
        return ((long) getMaxEnergyStoredSingular()) * this.itemStack.getCount();
    }

    @Override
    public int insert(int energy, TransactionContext transaction) {
        if(isCreative()) return 0;
        int stackSize = this.itemStack.getCount();
        if (stackSize == 0) return 0;
        energy /= stackSize;
        energy = Math.min(energy, getRate());
        int stored = getEnergyStoredSingular();
        int energyReceived = Math.min(getMaxEnergyStoredSingular() - stored, energy);
        this.journal.updateSnapshots(transaction);
        setItemStackEnergy(itemStack, stored + energyReceived, transaction);
        return energyReceived * stackSize;
    }

    @Override
    public int extract(int energy, TransactionContext transaction) {
        if(isCreative()) return energy;
        int stackSize = this.itemStack.getCount();
        if (stackSize == 0) return energy;
        energy /= stackSize;
        energy = Math.min(energy, getRate());
        int stored = getEnergyStoredSingular();
        int newEnergy = Math.max(stored - energy, 0);
        this.journal.updateSnapshots(transaction);
        setItemStackEnergy(itemStack, newEnergy, transaction);
        return (stored - newEnergy) * stackSize;
    }

    protected void setItemStackEnergy(ItemStack itemStack, int energy, @Nullable TransactionContext transaction) {
        if(isCreative()) return;
        itemStack.set(RegistryEntries.COMPONENT_ENERGY_STORAGE, energy);
        if (transaction != null) {
            itemAccess.exchange(ItemResource.of(itemStack), itemAccess.getAmount(), transaction);
        }
    }

    @Override
    public void setCapacity(int capacity) {
        if (capacity == BlockEnergyBatteryConfig.capacity) {
            itemStack.remove(RegistryEntries.COMPONENT_CAPACITY);
        } else {
            itemStack.set(RegistryEntries.COMPONENT_CAPACITY, capacity);
        }
    }

    @Override
    public void setEnergy(int energy) {
        setItemStackEnergy(itemStack, energy, null);
    }

    public class Journal extends SnapshotJournal<Integer> {

        @Override
        protected Integer createSnapshot() {
            return getEnergyStoredSingular();
        }

        @Override
        protected void revertToSnapshot(Integer snapshot) {
            setItemStackEnergy(itemStack, snapshot, null);
        }
    }
}
