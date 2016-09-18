package org.cyclops.integrateddynamics.capability.energybattery;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.cyclops.cyclopscore.helper.ItemStackHelpers;
import org.cyclops.integrateddynamics.api.block.IEnergyBattery;
import org.cyclops.integrateddynamics.block.BlockEnergyBatteryConfig;
import org.cyclops.integrateddynamics.core.item.ItemBlockEnergyContainer;

/**
 * Energy Battery implementation for ItemBlock's.
 * @author rubensworks
 */
public class EnergyBatteryItemBlockEnergyContainer implements IEnergyBattery {

    private final ItemBlockEnergyContainer itemBlockEnergyContainer;
    private final ItemStack itemStack;

    public EnergyBatteryItemBlockEnergyContainer(ItemBlockEnergyContainer itemBlockEnergyContainer, ItemStack itemStack) {
        this.itemBlockEnergyContainer = itemBlockEnergyContainer;
        this.itemStack = itemStack;
    }

    @Override
    public int getStoredEnergy() {
        NBTTagCompound tag = ItemStackHelpers.getSafeTagCompound(itemStack);
        return tag.getInteger(itemBlockEnergyContainer.get().getEneryContainerNBTName());
    }

    @Override
    public int getMaxStoredEnergy() {
        return BlockEnergyBatteryConfig.capacity;
    }

    @Override
    public int addEnergy(int energy, boolean simulate) {
        int stored = getStoredEnergy();
        int newEnergy = Math.min(stored + energy, getMaxStoredEnergy());
        if(!simulate) {
            setEnergy(itemStack, newEnergy);
        }
        return newEnergy - stored;
    }

    @Override
    public int consume(int energy, boolean simulate) {
        int stored = getStoredEnergy();
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
