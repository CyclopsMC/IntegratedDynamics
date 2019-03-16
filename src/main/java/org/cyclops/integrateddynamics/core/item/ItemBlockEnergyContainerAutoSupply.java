package org.cyclops.integrateddynamics.core.item;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.cyclops.cyclopscore.helper.L10NHelpers;

import java.util.List;

/**
 * @author rubensworks
 */
public class ItemBlockEnergyContainerAutoSupply extends ItemBlockEnergyContainer {
    /**
     * Make a new instance.
     *
     * @param block The blockState instance.
     */
    public ItemBlockEnergyContainerAutoSupply(Block block) {
        super(block);
    }

    @Override
    public void addInformation(ItemStack itemStack, World world, List<String> list, ITooltipFlag flag) {
        super.addInformation(itemStack, world, list, flag);
        L10NHelpers.addStatusInfo(list, isActivated(itemStack), getTranslationKey() + ".info.auto_supply");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        return new ActionResult<>(EnumActionResult.PASS, toggleActivation(player.getHeldItem(hand), world, player));
    }

    public static void autofill(IEnergyStorage source, World world, Entity entity) {
        if(entity instanceof EntityPlayer && !world.isRemote) {
            int tickAmount = source.extractEnergy(Integer.MAX_VALUE, true);
            if(tickAmount > 0) {
                EntityPlayer player = (EntityPlayer) entity;
                for (EnumHand hand : EnumHand.values()) {
                    ItemStack held = player.getHeldItem(hand);
                    ItemStack filled = tryFillContainerForPlayer(source, held, tickAmount, player);
                    if (filled != null) {
                        player.setHeldItem(hand, filled);
                    }
                }
            }
        }
    }

    public static ItemStack tryFillContainerForPlayer(IEnergyStorage source, ItemStack held, int tickAmount, EntityPlayer player) {
        if (held.hasCapability(CapabilityEnergy.ENERGY, null)) {
            IEnergyStorage target = held.getCapability(CapabilityEnergy.ENERGY, null);
            int moved = target.receiveEnergy(source.extractEnergy(target.receiveEnergy(tickAmount, true), false), false);
            if (moved > 0) {
                return held;
            }
        }
        return null;
    }

    @Override
    public void onUpdate(ItemStack itemStack, World world, Entity entity, int itemSlot, boolean par5) {
        if (isActivated(itemStack) && itemStack.hasCapability(CapabilityEnergy.ENERGY, null)) {
            autofill(itemStack.getCapability(CapabilityEnergy.ENERGY, null), world, entity);
        }
        super.onUpdate(itemStack, world, entity, itemSlot, par5);
    }

    public ItemStack toggleActivation(ItemStack itemStack, World world, EntityPlayer player) {
        if(player.isSneaking()) {
            if(!world.isRemote) {
                ItemStack activated = itemStack.copy();
                activated.setItemDamage(1 - activated.getItemDamage());
                return activated;
            }
            return itemStack;
        }
        return itemStack;
    }

    public boolean isActivated(ItemStack itemStack) {
        return itemStack.getItemDamage() == 1;
    }

    @Override
    public boolean hasEffect(ItemStack itemStack) {
        return isActivated(itemStack);
    }
}
