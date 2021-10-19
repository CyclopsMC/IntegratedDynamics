package org.cyclops.integrateddynamics.capability.energystorage;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import org.cyclops.integrateddynamics.block.BlockEnergyBatteryBase;
import org.cyclops.integrateddynamics.block.BlockEnergyBatteryConfig;
import org.cyclops.integrateddynamics.block.IEnergyContainerBlock;
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

        if (!this.itemStack.hasTag()) {
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
        IEnergyContainerBlock block = itemBlockEnergyContainer.get();
        return block instanceof BlockEnergyBatteryBase && ((BlockEnergyBatteryBase) block).isCreative();
    }

    protected int getEnergyStoredSingular() {
        if(isCreative()) return Integer.MAX_VALUE;
        CompoundNBT tag = itemStack.getOrCreateTag();
        return tag.getInt(itemBlockEnergyContainer.get().getEneryContainerNBTName());
    }

    @Override
    public int getEnergyStored() {
        return getEnergyStoredSingular() * this.itemStack.getCount();
    }

    public int getMaxEnergyStoredSingular() {
        if(isCreative()) return Integer.MAX_VALUE;
        CompoundNBT tag = itemStack.getOrCreateTag();
        if (!tag.contains(itemBlockEnergyContainer.get().getEneryContainerCapacityNBTName())) {
            return BlockEnergyBatteryConfig.capacity;
        }
        return tag.getInt(itemBlockEnergyContainer.get().getEneryContainerCapacityNBTName());
    }

    @Override
    public int getMaxEnergyStored() {
        return getMaxEnergyStoredSingular() * this.itemStack.getCount();
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
        CompoundNBT tag = itemStack.getOrCreateTag();
        tag.putInt(itemBlockEnergyContainer.get().getEneryContainerNBTName(), energy);
    }

    @Override
    public void setCapacity(int capacity) {
        CompoundNBT tag = itemStack.getOrCreateTag();
        if (capacity == BlockEnergyBatteryConfig.capacity) {
            tag.remove(itemBlockEnergyContainer.get().getEneryContainerCapacityNBTName());
        } else {
            tag.putInt(itemBlockEnergyContainer.get().getEneryContainerCapacityNBTName(), capacity);
        }
    }

    @Override
    public void setEnergy(int energy) {
        setItemStackEnergy(itemStack, energy);
    }
}
