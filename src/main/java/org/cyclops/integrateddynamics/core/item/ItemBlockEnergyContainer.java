package org.cyclops.integrateddynamics.core.item;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.item.IInformationProvider;
import org.cyclops.cyclopscore.item.ItemBlockNBT;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityProvider;
import org.cyclops.integrateddynamics.block.IEnergyContainerBlock;
import org.cyclops.integrateddynamics.capability.energystorage.EnergyStorageItemBlockEnergyContainer;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.tileentity.TileEnergyBattery;

import java.util.List;
import java.util.Locale;

/**
 * {@link BlockItem} that can be used for blocks that implement the {@link IEnergyStorage} capability.
 * Instances of this will also keep it's energy level.
 * @author rubensworks
 *
 */
public class ItemBlockEnergyContainer extends ItemBlockNBT {

	private IEnergyContainerBlock block;

    public ItemBlockEnergyContainer(Block block, Properties builder) {
        super(block, builder);
        // Will crash if no valid instance of.
        this.block = (IEnergyContainerBlock) block;
    }
    
    /**
     * @return The energy container.
     */
    public IEnergyContainerBlock get() {
    	return block;
    }

    public LazyOptional<IEnergyStorage> getEnergyBattery(ItemStack itemStack) {
        if (CapabilityEnergy.ENERGY == null) {
            return LazyOptional.of(() -> this.createCapability(itemStack));
        }
        return itemStack.getCapability(CapabilityEnergy.ENERGY);
    }
	
    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack itemStack, World world, List<ITextComponent> list, ITooltipFlag flag) {
        super.appendHoverText(itemStack, world, list, flag);
        getEnergyBattery(itemStack)
                .ifPresent(energyStorage -> {
                    int amount = energyStorage.getEnergyStored();
                    int capacity = energyStorage.getMaxEnergyStored();
                    String line = String.format(Locale.ROOT, "%,d", amount) + " / " + String.format(Locale.ROOT, "%,d", capacity) + " " + L10NHelpers.localize(L10NValues.GENERAL_ENERGY_UNIT);
                    list.add(new StringTextComponent(IInformationProvider.ITEM_PREFIX + line));
                });
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack itemStack) {
        return getEnergyBattery(itemStack)
                .map(energyStorage -> {
                    double amount = energyStorage.getEnergyStored();
                    double capacity = energyStorage.getMaxEnergyStored();
                    return (capacity - amount) / capacity;
                })
                .orElse(0D);
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return MathHelper.hsvToRgb(Math.max(0.0F, 1 - (float) getDurabilityForDisplay(stack)) / 3.0F, 1.0F, 1.0F);
    }

    protected EnergyStorageItemBlockEnergyContainer createCapability(ItemStack itemStack) {
        return new EnergyStorageItemBlockEnergyContainer(this, itemStack) {
            @Override
            public int getRate() {
                return TileEnergyBattery.getEnergyPerTick(getMaxEnergyStored());
            }
        };
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
        return new DefaultCapabilityProvider<>(() -> CapabilityEnergy.ENERGY,
                LazyOptional.of(() -> this.createCapability(stack)));
    }

}
