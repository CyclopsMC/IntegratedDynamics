package org.cyclops.integrateddynamics.core.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.cyclops.cyclopscore.helper.L10NHelpers;

import java.util.List;

/**
 * @author rubensworks
 */
public class ItemBlockEnergyContainerAutoSupply extends ItemBlockEnergyContainer {

    public ItemBlockEnergyContainerAutoSupply(Block block, Properties builder) {
        super(block, builder);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Level world, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(itemStack, world, list, flag);
        L10NHelpers.addStatusInfo(list, isActivated(itemStack), getDescriptionId() + ".info.auto_supply");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        return new InteractionResultHolder<>(InteractionResult.PASS, toggleActivation(player.getItemInHand(hand), world, player));
    }

    public static void autofill(IEnergyStorage source, Level world, Entity entity) {
        if(entity instanceof Player && !world.isClientSide()) {
            int tickAmount = source.extractEnergy(Integer.MAX_VALUE, true);
            if(tickAmount > 0) {
                Player player = (Player) entity;
                for (InteractionHand hand : InteractionHand.values()) {
                    ItemStack held = player.getItemInHand(hand);
                    ItemStack filled = tryFillContainerForPlayer(source, held, tickAmount, player);
                    if (!filled.isEmpty()) {
                        player.setItemInHand(hand, filled);
                    }
                }
            }
        }
    }

    public static ItemStack tryFillContainerForPlayer(IEnergyStorage source, ItemStack held, int tickAmount, Player player) {
        return held.getCapability(CapabilityEnergy.ENERGY, null)
                .map(target -> {
                    int moved = target.receiveEnergy(source.extractEnergy(target.receiveEnergy(tickAmount, true), false), false);
                    if (moved > 0) {
                        return held;
                    }
                    return ItemStack.EMPTY;
                }).orElse(ItemStack.EMPTY);
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level world, Entity entity, int itemSlot, boolean par5) {
        if (isActivated(itemStack)) {
            itemStack.getCapability(CapabilityEnergy.ENERGY, null)
                    .ifPresent(energyStorage -> autofill(energyStorage, world, entity));
        }
        super.inventoryTick(itemStack, world, entity, itemSlot, par5);
    }

    public ItemStack toggleActivation(ItemStack itemStack, Level world, Player player) {
        if(player.isSecondaryUseActive()) {
            if(!world.isClientSide()) {
                ItemStack activated = itemStack.copy();
                activated.setDamageValue(1 - activated.getDamageValue());
                return activated;
            }
            return itemStack;
        }
        return itemStack;
    }

    public boolean isActivated(ItemStack itemStack) {
        return itemStack.getDamageValue() == 1;
    }

    @Override
    public boolean isFoil(ItemStack itemStack) {
        return isActivated(itemStack);
    }
}
