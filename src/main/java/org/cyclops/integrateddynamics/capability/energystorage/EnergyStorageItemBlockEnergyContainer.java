package org.cyclops.integrateddynamics.capability.energystorage;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.energy.IEnergyStorage;
import org.cyclops.cyclopscore.helper.ItemStackHelpers;
import org.cyclops.integrateddynamics.block.BlockEnergyBatteryConfig;
import org.cyclops.integrateddynamics.core.item.ItemBlockEnergyContainer;

/**
 * Energy Battery implementation for ItemBlock's.
 * @author rubensworks
 */
public class EnergyStorageItemBlockEnergyContainer implements IEnergyStorage {

    private final ItemBlockEnergyContainer itemBlockEnergyContainer;
    private final ItemStack itemStack;

    public EnergyStorageItemBlockEnergyContainer(ItemBlockEnergyContainer itemBlockEnergyContainer, ItemStack itemStack) {
        this.itemBlockEnergyContainer = itemBlockEnergyContainer;
        this.itemStack = itemStack;
    }

    @Override
    public int getEnergyStored() {
        NBTTagCompound tag = ItemStackHelpers.getSafeTagCompound(itemStack);
        return tag.getInteger(itemBlockEnergyContainer.get().getEneryContainerNBTName());
    }

    @Override
    public int getMaxEnergyStored() {
        return BlockEnergyBatteryConfig.capacity;
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
        int stored = getEnergyStored();
        int newEnergy = Math.min(stored + energy, getMaxEnergyStored());
        if(!simulate) {
            setEnergy(itemStack, newEnergy);
        }
        return newEnergy - stored;
    }

    @Override
    public int extractEnergy(int energy, boolean simulate) {
        int stored = getEnergyStored();
        int newEnergy = Math.max(stored - energy, 0);
        if(!simulate) {
            setEnergy(itemStack, newEnergy);
        }
        return stored - newEnergy;
    }

    protected void setEnergy(ItemStack itemStack, int energy) {
        NBTTagCompound tag = ItemStackHelpers.getSafeTagCompound(itemStack);
        tag.setInteger(itemBlockEnergyContainer.get().getEneryContainerNBTName(), energy);
    }
}
