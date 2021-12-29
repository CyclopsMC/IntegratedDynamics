package org.cyclops.integrateddynamics.core.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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
import org.cyclops.integrateddynamics.blockentity.BlockEntityEnergyBattery;
import org.cyclops.integrateddynamics.capability.energystorage.EnergyStorageItemBlockEnergyContainer;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

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
    public void appendHoverText(ItemStack itemStack, Level world, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(itemStack, world, list, flag);
        getEnergyBattery(itemStack)
                .ifPresent(energyStorage -> {
                    int amount = energyStorage.getEnergyStored();
                    int capacity = energyStorage.getMaxEnergyStored();
                    String line = String.format(Locale.ROOT, "%,d", amount) + " / " + String.format(Locale.ROOT, "%,d", capacity) + " " + L10NHelpers.localize(L10NValues.GENERAL_ENERGY_UNIT);
                    list.add(new TextComponent(IInformationProvider.ITEM_PREFIX + line));
                });
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack itemStack) {
        return getEnergyBattery(itemStack)
                .map(energyStorage -> {
                    double amount = energyStorage.getEnergyStored();
                    double capacity = energyStorage.getMaxEnergyStored();
                    return (int) Math.round((capacity - amount) / capacity * 13);
                })
                .orElse(0);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return Mth.hsvToRgb(Math.max(0.0F, 1 - ((float) getBarWidth(stack)) / 13) / 3.0F, 1.0F, 1.0F);
    }

    protected EnergyStorageItemBlockEnergyContainer createCapability(ItemStack itemStack) {
        return new EnergyStorageItemBlockEnergyContainer(this, itemStack) {
            @Override
            public int getRate() {
                return BlockEntityEnergyBattery.getEnergyPerTick(getMaxEnergyStored());
            }
        };
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        return new DefaultCapabilityProvider<>(() -> CapabilityEnergy.ENERGY,
                LazyOptional.of(() -> this.createCapability(stack)));
    }

}
