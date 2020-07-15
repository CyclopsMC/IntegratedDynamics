package org.cyclops.integrateddynamics.capability.energystorage;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.cyclops.cyclopscore.helper.ItemStackHelpers;
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

        if (!this.itemStack.hasTagCompound()) {
            setEnergy(itemStack, 0);
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

    @Override
    public int getEnergyStored() {
        if(isCreative()) return Integer.MAX_VALUE;
        NBTTagCompound tag = ItemStackHelpers.getSafeTagCompound(itemStack);
        return tag.getInteger(itemBlockEnergyContainer.get().getEneryContainerNBTName());
    }

    @Override
    public int getMaxEnergyStored() {
        if(isCreative()) return Integer.MAX_VALUE;
        NBTTagCompound tag = ItemStackHelpers.getSafeTagCompound(itemStack);
        if (!tag.hasKey(itemBlockEnergyContainer.get().getEneryContainerCapacityNBTName())) {
            return BlockEnergyBatteryConfig.capacity;
        }
        return tag.getInteger(itemBlockEnergyContainer.get().getEneryContainerCapacityNBTName());
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
        energy = Math.min(energy, getRate());
        int stored = getEnergyStored();
        int energyReceived = Math.min(getMaxEnergyStored() - stored, energy);
        if(!simulate) {
            setEnergy(itemStack, stored + energyReceived);
        }
        return energyReceived;
    }

    @Override
    public int extractEnergy(int energy, boolean simulate) {
        if(isCreative()) return energy;
        energy = Math.min(energy, getRate());
        int stored = getEnergyStored();
        int newEnergy = Math.max(stored - energy, 0);
        if(!simulate) {
            setEnergy(itemStack, newEnergy);
        }
        return stored - newEnergy;
    }

    public void setEnergy(ItemStack itemStack, int energy) {
        if(isCreative()) return;
        NBTTagCompound tag = ItemStackHelpers.getSafeTagCompound(itemStack);
        tag.setInteger(itemBlockEnergyContainer.get().getEneryContainerNBTName(), energy);
    }

    @Override
    public void setCapacity(int capacity) {
        NBTTagCompound tag = ItemStackHelpers.getSafeTagCompound(itemStack);
        if (capacity == BlockEnergyBatteryConfig.capacity) {
            tag.removeTag(itemBlockEnergyContainer.get().getEneryContainerCapacityNBTName());
        } else {
            tag.setInteger(itemBlockEnergyContainer.get().getEneryContainerCapacityNBTName(), capacity);
        }
    }

    @Override
    public void setEnergy(int energy) {
        setEnergy(itemStack, energy);
    }
}
