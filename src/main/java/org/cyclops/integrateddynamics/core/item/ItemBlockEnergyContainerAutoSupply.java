package org.cyclops.integrateddynamics.core.item;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
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
    public void addInformation(ItemStack itemStack, World world, List<ITextComponent> list, ITooltipFlag flag) {
        super.addInformation(itemStack, world, list, flag);
        L10NHelpers.addStatusInfo(list, isActivated(itemStack), getTranslationKey() + ".info.auto_supply");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        return new ActionResult<>(ActionResultType.PASS, toggleActivation(player.getHeldItem(hand), world, player));
    }

    public static void autofill(IEnergyStorage source, World world, Entity entity) {
        if(entity instanceof PlayerEntity && !world.isRemote()) {
            int tickAmount = source.extractEnergy(Integer.MAX_VALUE, true);
            if(tickAmount > 0) {
                PlayerEntity player = (PlayerEntity) entity;
                for (Hand hand : Hand.values()) {
                    ItemStack held = player.getHeldItem(hand);
                    ItemStack filled = tryFillContainerForPlayer(source, held, tickAmount, player);
                    if (!filled.isEmpty()) {
                        player.setHeldItem(hand, filled);
                    }
                }
            }
        }
    }

    public static ItemStack tryFillContainerForPlayer(IEnergyStorage source, ItemStack held, int tickAmount, PlayerEntity player) {
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
    public void inventoryTick(ItemStack itemStack, World world, Entity entity, int itemSlot, boolean par5) {
        if (isActivated(itemStack)) {
            itemStack.getCapability(CapabilityEnergy.ENERGY, null)
                    .ifPresent(energyStorage -> autofill(energyStorage, world, entity));
        }
        super.inventoryTick(itemStack, world, entity, itemSlot, par5);
    }

    public ItemStack toggleActivation(ItemStack itemStack, World world, PlayerEntity player) {
        if(player.isSecondaryUseActive()) {
            if(!world.isRemote()) {
                ItemStack activated = itemStack.copy();
                activated.setDamage(1 - activated.getDamage());
                return activated;
            }
            return itemStack;
        }
        return itemStack;
    }

    public boolean isActivated(ItemStack itemStack) {
        return itemStack.getDamage() == 1;
    }

    @Override
    public boolean hasEffect(ItemStack itemStack) {
        return isActivated(itemStack);
    }
}
