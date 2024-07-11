package org.cyclops.integrateddynamics.capability.energystorage;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.cyclops.cyclopscore.RegistryEntries;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.block.BlockEnergyBatteryBase;
import org.cyclops.integrateddynamics.block.BlockEnergyBatteryConfig;
import org.cyclops.integrateddynamics.core.item.ItemBlockEnergyContainer;

/**
 * Energy Battery implementation for ItemBlock's.
 * @author rubensworks
 */
public class EnergyStorageItemBlockEnergyContainer implements IEnergyStorageCapacity, IEnergyStorageMutable {

    private final ItemBlockEnergyContainer itemBlockEnergyContainer;
    private final ItemStack itemStack;
    private final int rate;

    public EnergyStorageItemBlockEnergyContainer(ItemBlockEnergyContainer itemBlockEnergyContainer, ItemStack itemStack, int rate) {
        this.itemBlockEnergyContainer = itemBlockEnergyContainer;
        this.itemStack = itemStack;
        this.rate = rate;

        if (!this.itemStack.has(RegistryEntries.COMPONENT_ENERGY_STORAGE)) {
            setItemStackEnergy(itemStack, 0);
        }
    }

    public EnergyStorageItemBlockEnergyContainer(ItemBlockEnergyContainer itemBlockEnergyContainer, ItemStack itemStack) {
        this(itemBlockEnergyContainer, itemStack, Integer.MAX_VALUE);
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
    public int getEnergyStored() {
        return Helpers.multiplySafe(getEnergyStoredSingular(), this.itemStack.getCount());
    }

    public long getEnergyStoredLong() {
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
    public int getMaxEnergyStored() {
        return Helpers.multiplySafe(getMaxEnergyStoredSingular(), this.itemStack.getCount());
    }

    public long getMaxEnergyStoredLong() {
        return ((long) getMaxEnergyStoredSingular()) * this.itemStack.getCount();
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
        if(isCreative()) return 0;
        int stackSize = this.itemStack.getCount();
        if (stackSize == 0) return 0;
        energy /= stackSize;
        energy = Math.min(energy, getRate());
        int stored = getEnergyStoredSingular();
        int energyReceived = Math.min(getMaxEnergyStoredSingular() - stored, energy);
        if(!simulate) {
            setItemStackEnergy(itemStack, stored + energyReceived);
        }
        return energyReceived * stackSize;
    }

    @Override
    public int extractEnergy(int energy, boolean simulate) {
        if(isCreative()) return energy;
        int stackSize = this.itemStack.getCount();
        if (stackSize == 0) return energy;
        energy /= stackSize;
        energy = Math.min(energy, getRate());
        int stored = getEnergyStoredSingular();
        int newEnergy = Math.max(stored - energy, 0);
        if(!simulate) {
            setItemStackEnergy(itemStack, newEnergy);
        }
        return (stored - newEnergy) * stackSize;
    }

    protected void setItemStackEnergy(ItemStack itemStack, int energy) {
        if(isCreative()) return;
        itemStack.set(RegistryEntries.COMPONENT_ENERGY_STORAGE, energy);
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
        setItemStackEnergy(itemStack, energy);
    }
}
