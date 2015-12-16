package org.cyclops.integrateddynamics.core.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.ItemStackHelpers;
import org.cyclops.cyclopscore.item.IInformationProvider;
import org.cyclops.cyclopscore.item.ItemBlockNBT;
import org.cyclops.integrateddynamics.api.block.IEnergyContainer;
import org.cyclops.integrateddynamics.api.block.IEnergyContainerBlock;
import org.cyclops.integrateddynamics.block.BlockEnergyBatteryConfig;

import java.util.List;

/**
 * {@link ItemBlock} that can be used for blocks that implement {@link IEnergyContainer}.
 * Instances of this will also keep it's energy level.
 * @author rubensworks
 *
 */
public class ItemBlockEnergyContainer extends ItemBlockNBT implements IEnergyContainer {

	private IEnergyContainerBlock block;

    /**
     * Make a new instance.
     * @param block The blockState instance.
     */
    public ItemBlockEnergyContainer(Block block) {
        super(block);
        this.setHasSubtypes(false);
        // Will crash if no valid instance of.
        this.block = (IEnergyContainerBlock) block;
    }
    
    /**
     * @return The energy container.
     */
    public IEnergyContainerBlock get() {
    	return block;
    }
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List list, boolean par4) {
        super.addInformation(itemStack, entityPlayer, list, par4);
        int amount = getStoredEnergy(itemStack);
        int capacity = getMaxStoredEnergy(itemStack);
        String line = String.format("%,d", amount) + " / " + String.format("%,d", capacity);
        list.add(IInformationProvider.ITEM_PREFIX + line);
    }

    @Override
    public int getStoredEnergy(ItemStack itemStack) {
        NBTTagCompound tag = ItemStackHelpers.getSafeTagCompound(itemStack);
        return tag.getInteger(get().getEneryContainerNBTName());
    }

    @Override
    public int getMaxStoredEnergy(ItemStack itemStack) {
        return BlockEnergyBatteryConfig.capacity;
    }

    protected void setEnergy(ItemStack itemStack, int energy) {
        NBTTagCompound tag = ItemStackHelpers.getSafeTagCompound(itemStack);
        tag.setInteger(get().getEneryContainerNBTName(), energy);
    }

    @Override
    public void addEnergy(ItemStack itemStack, int energy) {
        int newEnergy = getStoredEnergy(itemStack) + energy;
        setEnergy(itemStack, Math.min(newEnergy, getMaxStoredEnergy(itemStack)));
    }

    @Override
    public int consume(ItemStack itemStack, int energy, boolean simulate) {
        int stored = getStoredEnergy(itemStack);
        int newEnergy = Math.max(stored - energy, 0);
        if(!simulate) {
            setEnergy(itemStack, newEnergy);
        }
        return stored - newEnergy;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack itemStack) {
        double amount = getStoredEnergy(itemStack);
        double capacity = getMaxStoredEnergy(itemStack);
        return (capacity - amount) / capacity;
    }
}
