package org.cyclops.integrateddynamics.core.item;

import cofh.api.energy.IEnergyContainerItem;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.item.IInformationProvider;
import org.cyclops.cyclopscore.item.ItemBlockNBT;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityProvider;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.block.IEnergyContainerBlock;
import org.cyclops.integrateddynamics.capability.energystorage.EnergyStorageItemBlockEnergyContainer;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import java.util.List;

/**
 * {@link ItemBlock} that can be used for blocks that implement the {@link IEnergyStorage} capability.
 * Instances of this will also keep it's energy level.
 * @author rubensworks
 *
 */
@Optional.Interface(iface = "cofh.api.energy.IEnergyContainerItem", modid = Reference.MOD_RF_API, striprefs = true)
public class ItemBlockEnergyContainer extends ItemBlockNBT implements IEnergyContainerItem {

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

    protected IEnergyStorage getEnergyBattery(ItemStack itemStack) {
        return itemStack.getCapability(CapabilityEnergy.ENERGY, null);
    }
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List list, boolean par4) {
        super.addInformation(itemStack, entityPlayer, list, par4);
        IEnergyStorage energyStorage = getEnergyBattery(itemStack);
        int amount = energyStorage.getEnergyStored();
        int capacity = energyStorage.getMaxEnergyStored();
        String line = String.format("%,d", amount) + " / " + String.format("%,d", capacity) + " " + L10NHelpers.localize(L10NValues.GENERAL_ENERGY_UNIT);
        list.add(IInformationProvider.ITEM_PREFIX + line);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack itemStack) {
        IEnergyStorage energyStorage = getEnergyBattery(itemStack);
        double amount = energyStorage.getEnergyStored();
        double capacity = energyStorage.getMaxEnergyStored();
        return (capacity - amount) / capacity;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
        return new DefaultCapabilityProvider<>(CapabilityEnergy.ENERGY, new EnergyStorageItemBlockEnergyContainer(this, stack));
    }

    /*
     * ------------------ RF API ------------------
     */

    @Optional.Method(modid = Reference.MOD_RF_API)
    @Override
    public int receiveEnergy(ItemStack container, int maxReceive, boolean simulate) {
        return getEnergyBattery(container).receiveEnergy(maxReceive, simulate);
    }

    @Optional.Method(modid = Reference.MOD_RF_API)
    @Override
    public int extractEnergy(ItemStack container, int maxExtract, boolean simulate) {
        return getEnergyBattery(container).extractEnergy(maxExtract, simulate);
    }

    @Optional.Method(modid = Reference.MOD_RF_API)
    @Override
    public int getEnergyStored(ItemStack container) {
        return getEnergyBattery(container).getEnergyStored();
    }

    @Optional.Method(modid = Reference.MOD_RF_API)
    @Override
    public int getMaxEnergyStored(ItemStack container) {
        return getEnergyBattery(container).getMaxEnergyStored();
    }
}
