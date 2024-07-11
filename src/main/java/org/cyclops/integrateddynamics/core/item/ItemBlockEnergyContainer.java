package org.cyclops.integrateddynamics.core.item;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.item.IInformationProvider;
import org.cyclops.cyclopscore.item.ItemBlockNBT;
import org.cyclops.integrateddynamics.blockentity.BlockEntityEnergyBattery;
import org.cyclops.integrateddynamics.capability.energystorage.EnergyStorageItemBlockEnergyContainer;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * {@link BlockItem} that can be used for blocks that implement the {@link IEnergyStorage} capability.
 * Instances of this will also keep it's energy level.
 * @author rubensworks
 *
 */
public class ItemBlockEnergyContainer extends ItemBlockNBT {

    private Block block;

    public ItemBlockEnergyContainer(Block block, Properties builder) {
        super(block, builder);
        this.block = block;
    }

    /**
     * @return The energy container.
     */
    public Block get() {
        return block;
    }

    public Optional<IEnergyStorage> getEnergyBattery(ItemStack itemStack) {
        return Optional.ofNullable(itemStack.getCapability(Capabilities.EnergyStorage.ITEM));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(itemStack, context, list, flag);
        getEnergyBattery(itemStack)
                .ifPresent(energyStorage -> {
                    long amount = ((EnergyStorageItemBlockEnergyContainer) energyStorage).getEnergyStoredLong();
                    long capacity = ((EnergyStorageItemBlockEnergyContainer) energyStorage).getMaxEnergyStoredLong();
                    String line = String.format(Locale.ROOT, "%,d", amount) + " / " + String.format(Locale.ROOT, "%,d", capacity) + " " + L10NHelpers.localize(L10NValues.GENERAL_ENERGY_UNIT);
                    list.add(Component.literal(IInformationProvider.ITEM_PREFIX + line));
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
                    return (int) Math.round(amount / capacity * 13);
                })
                .orElse(0);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return Mth.hsvToRgb(Math.max(0.0F, ((float) getBarWidth(stack)) / 13) / 3.0F, 1.0F, 1.0F);
    }

    public EnergyStorageItemBlockEnergyContainer createCapability(ItemStack itemStack) {
        return new EnergyStorageItemBlockEnergyContainer(this, itemStack) {
            @Override
            public int getRate() {
                return BlockEntityEnergyBattery.getEnergyPerTick(getMaxEnergyStored());
            }
        };
    }

}
